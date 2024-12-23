package com.example.myapplication;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements LocationListener {
    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvDescription;

    // 定义双精度类型的经纬度
    private Double longitude,latitude;
    private String adr;
    // 定义位置管理器
    private LocationManager locationManager;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvDescription = view.findViewById(R.id.tvDescription);

        // 初始化权限请求
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        // 权限被授予，获取位置
                        getLocation();
                    } else {
                        // 权限被拒绝，提示用户
                        Toast.makeText(requireContext(), "需要定位权限才能获取天气", Toast.LENGTH_SHORT).show();
                    }
                });


        // 判断当前是否拥有使用GPS的权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            // 权限已授予，获取位置
            getLocation();
        }
        //原先布局

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        // 获取当前位置管理器
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        // 启动位置请求
        // LocationManager.GPS_PROVIDER GPS定位
        // LocationManager.NETWORK_PROVIDER 网络定位
        // LocationManager.PASSIVE_PROVIDER 被动接受定位信息
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    // 当位置改变时执行，除了移动设置距离为 0时
    @Override
    public void onLocationChanged(@NonNull Location location) {

        // 获取当前纬度
        latitude = location.getLatitude();
        // 获取当前经度
        longitude = location.getLongitude();

        // 定义位置解析
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // 使用异步方法
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String province = address.getAdminArea();
                        String city = address.getLocality();
                        String district = address.getSubLocality();

                        String info = province + " " + city + " " + district;
//                        nowAddress.setText(info);
                        adr = info;
                        tvCityName.setText(info);
                    }
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    Log.e("Geocoder", "地理编码错误: " + errorMessage);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "无法获取位置信息", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            // 对于 API 33 以下版本，改用异步线程手动封装
            fetchGeocodeAsync(geocoder, latitude, longitude);
        }


        //获取天气数据
        fetchWeatherData(latitude,longitude);
        // 移除位置管理器
        // 需要一直获取位置信息可以去掉这个
        locationManager.removeUpdates(this);
    }

    // 手动封装异步地理编码方法（避免直接调用过时的 getFromLocation）
    private void fetchGeocodeAsync(Geocoder geocoder, double latitude, double longitude) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // 不推荐直接调用，但在低版本兼容
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String province = address.getAdminArea();
                    String city = address.getLocality();
                    String district = address.getSubLocality();

                    String info = province + " " + city + " " + district;
                    requireActivity().runOnUiThread(() -> {
//                        nowAddress.setText(info);
                        adr = info;
                        tvCityName.setText(info);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "无法获取位置信息", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    // 任意定位提供者启动执行
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.e("onProviderEnabled", provider);
    }

    // 任意定位提供者关闭执行
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.e("onProviderDisabled", provider);
    }


    private void fetchWeatherData(double latitude, double longitude) {//通过经纬度获取天气信息

//        Log.d("WeatherApp", "开始获取天气数据，城市: " + city);
        Log.d("WeatherApp", "开始获取天气数据，经纬度: " + latitude + ", " + longitude);
        Log.d("WeatherApp", "初始化 Retrofit");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("WeatherApp", "Retrofit 实例创建成功");

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        Call<WeatherResponse> call = weatherApi.getWeatherByCoordinates(
                latitude, // 纬度
                longitude, // 经度
                "f51209fc4a7e69d7187e7042005ac98c",//我的OpenWeatherMap API 密钥
                "zh_cn", //简体中文
                "metric"  //摄氏度
        );
        //调试
        Log.d("WeatherApp", "天气请求参数: " +
                "纬度 = " + latitude + ", " +
                "经度 = " + longitude + ", " +
                "API Key = f51209fc4a7e69d7187e7042005ac98c");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                Log.d("WeatherApp", "请求天气数据返回，代码: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateUI(weather);
                } else {
                    // 打印错误信息
                    Log.e("WeatherApp", "请求失败，错误代码: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("WeatherApp", "错误信息: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e("WeatherApp", "无法解析错误信息", e);
                    }

                    Toast.makeText(requireContext(), "获取天气失败", Toast.LENGTH_SHORT).show();
                }
                Log.d("WeatherApp", "响应内容: " + response.body());
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherApp", "网络请求失败: " + t.getMessage());
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                // 添加网络错误的具体信息 //调试
                if (t instanceof IOException) {
                    Log.e("WeatherApp", "网络连接错误: " + t.getMessage());
                } else {
                    Log.e("WeatherApp", "非网络错误: " + t.getMessage());
                }
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
//        tvCityName.setText(weather.name);
        Log.d("WeatherApp", "更新UI，天气数据: " + weather);//调试
        tvCityName.setText(adr);
        tvTemperature.setText("当前气温: " + weather.main.temp + "°C");
        tvDescription.setText("天气: " + weather.weather.get(0).description);
//        tvRecommendation.setText("推荐穿搭: " + ClothingRecommendation.getClothingRecommendation(weather));

        // 推荐穿搭信息
        String recommendation = ClothingRecommendation.getClothingRecommendation(weather);

        // 填充表格
        TableLayout tableRecommendation = requireView().findViewById(R.id.tableRecommendation);
        populateClothingRecommendation(requireContext(), tableRecommendation, recommendation);
    }

    // 动态填充推荐穿搭信息到表格中
    public void populateClothingRecommendation(Context context, TableLayout tableLayout, String recommendation) {
        // 将推荐信息按 "+" 分割成不同类别
        String[] items = recommendation.split("\\+");
        String clothes = "", accessories = "", shoes = "";

        // 简单分类：衣物、配饰和鞋具
        if (items.length >= 1) clothes = items[0].trim();
        if (items.length >= 2) accessories = items[1].trim();
        if (items.length >= 3) shoes = items[2].trim();

        // 找到静态添加的 TextView
        TextView tvClothes = tableLayout.findViewById(R.id.tvClothes);
        TextView tvAccessories = tableLayout.findViewById(R.id.tvAccessories);
        TextView tvShoes = tableLayout.findViewById(R.id.tvShoes);

        // 设置每行最多 5 个字符，并自动换行
        int maxCharsPerLine = 5;
        tvClothes.setFilters(new InputFilter[] { new LineBreakInputFilter(maxCharsPerLine) });
        tvAccessories.setFilters(new InputFilter[] { new LineBreakInputFilter(maxCharsPerLine) });
        tvShoes.setFilters(new InputFilter[] { new LineBreakInputFilter(maxCharsPerLine) });

        // 动态填充内容
        tvClothes.setText(clothes);
        tvAccessories.setText(accessories);
        tvShoes.setText(shoes);

        // 设置 TextView 的最大行数为自动换行
        tvClothes.setMaxLines(Integer.MAX_VALUE);
        tvAccessories.setMaxLines(Integer.MAX_VALUE);
        tvShoes.setMaxLines(Integer.MAX_VALUE);
    }

    // 自定义 InputFilter，用于限制每行的字符数并自动换行
    public static class LineBreakInputFilter implements InputFilter {
        private final int maxCharsPerLine;

        public LineBreakInputFilter(int maxCharsPerLine) {
            this.maxCharsPerLine = maxCharsPerLine;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            int lineCharCount = 0;

            for (int i = 0; i < dest.length(); i++) {
                char c = dest.charAt(i);
                if (c == '\n') {
                    lineCharCount = 0;
                } else {
                    lineCharCount++;
                }

                if (lineCharCount > maxCharsPerLine) {
                    builder.append('\n');
                    lineCharCount = 0;
                }
                builder.append(c);
            }

            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (c == '\n') {
                    lineCharCount = 0;
                } else {
                    lineCharCount++;
                }

                if (lineCharCount > maxCharsPerLine) {
                    builder.append('\n');
                    lineCharCount = 0;
                }
                builder.append(c);
            }

            return builder.toString();
        }
    }

}