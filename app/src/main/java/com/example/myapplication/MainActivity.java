package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class MainActivity extends AppCompatActivity implements LocationListener{
    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvDescription;

    // 定义双精度类型的经纬度
    private Double longitude,latitude;
    private String adr;
    // 定义位置管理器
    private LocationManager locationManager;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    // 自定义 InputFilter，用于限制每行的字符数并自动换行
    public static class LineBreakInputFilter implements InputFilter {
        private final int maxCharsPerLine;

        public LineBreakInputFilter(int maxCharsPerLine) {
            this.maxCharsPerLine = maxCharsPerLine;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
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


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);

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
                        Toast.makeText(this, "需要定位权限才能获取天气", Toast.LENGTH_SHORT).show();
                    }
                });


        // 判断当前是否拥有使用GPS的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            // 权限已授予，获取位置
            getLocation();
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        // 获取当前位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 启动位置请求
        // LocationManager.GPS_PROVIDER GPS定位
        // LocationManager.NETWORK_PROVIDER 网络定位
        // LocationManager.PASSIVE_PROVIDER 被动接受定位信息
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    // 当位置改变时执行，除了移动设置距离为 0时
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String province = address.getAdminArea();
                        String city = address.getLocality();
                        String district = address.getSubLocality();

                        adr = province + " " + city + " " + district;
                        tvCityName.setText(adr);
                    }
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    Log.e("Geocoder", "地理编码错误: " + errorMessage);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "无法获取位置信息", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            fetchGeocodeAsync(geocoder, latitude, longitude);
        }

        fetchWeatherData(latitude, longitude);
        locationManager.removeUpdates(this);
    }


    private void fetchGeocodeAsync(Geocoder geocoder, double latitude, double longitude) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String province = address.getAdminArea();
                    String city = address.getLocality();
                    String district = address.getSubLocality();

                    adr = province + " " + city + " " + district;
                    runOnUiThread(() -> tvCityName.setText(adr));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "无法获取位置信息", Toast.LENGTH_SHORT).show());
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

    private void fetchWeatherData(double latitude, double longitude) {
        Log.d("WeatherApp", "开始获取天气数据，经纬度: " + latitude + ", " + longitude);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        Call<WeatherResponse> call = weatherApi.getWeatherByCoordinates(
                latitude,
                longitude,
                "f51209fc4a7e69d7187e7042005ac98c",
                "zh_cn",
                "metric"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateUI(weather);
                } else {
                    Log.e("WeatherApp", "请求失败，错误代码: " + response.code());
                    Toast.makeText(MainActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherApp", "网络请求失败: " + t.getMessage());
                Toast.makeText(MainActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
        Log.d("WeatherApp", "更新UI，天气数据: " + weather);
        tvCityName.setText(adr);
        tvTemperature.setText("当前气温: " + weather.main.temp + "°C");
        tvDescription.setText("天气: " + weather.weather.get(0).description);

        // 推荐穿搭信息和 提示信息
        String recommendation = ClothingRecommendation.getClothingRecommendation(weather);
        String warningWords = ClothingRecommendation.getWarningRecommendation(weather);//***********************

                // 填充表格
        TableLayout tableRecommendation = findViewById(R.id.tableRecommendation);
        if (tableRecommendation == null) {
            Log.e("WeatherApp", "tableRecommendation 为 null");
            return;
        }
        CardView scrollingTextCard = findViewById(R.id.scrollingTextCard);
        if (scrollingTextCard == null) {
            Log.e("WeatherApp", "scrollingTextCard 为 null");
            return;
        }
        populateClothingRecommendation(this, tableRecommendation, recommendation,scrollingTextCard, warningWords);//********************************8

        // 根据天气类型设置背景图片
        String weatherDescription = weather.weather.get(0).description;
        int backgroundDrawableId = R.drawable.moren;

        for (Map.Entry<String, Integer> entry : weatherBackgroundMap.entrySet()) {
            if (weatherDescription.contains(entry.getKey())) {// 找到匹配的关键词后，跳出循环
                backgroundDrawableId = entry.getValue();
                break;
            }
        }
//        findViewById(R.id.rootLayout).setBackgroundResource(backgroundDrawableId);
        View rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) {
            rootLayout.setBackgroundResource(backgroundDrawableId);
        } else {
            Log.e("WeatherApp", "rootLayout 为 null");
        }
    }
    // 动态填充推荐穿搭信息到表格中以及提示栏中****************************************************
    public void populateClothingRecommendation(Context context, TableLayout tableLayout, String recommendation,CardView scrollingTextCard, String warningWords) {
        //填写状态信息
        String[] items = recommendation.split("\\+");
        String clothes = "", accessories = "", shoes = "";

        if (items.length >= 1) clothes = items[0].trim();
        if (items.length >= 2) accessories = items[1].trim();
        if (items.length >= 3) shoes = items[2].trim();

        TextView tvClothes = tableLayout.findViewById(R.id.tvClothes);
        TextView tvAccessories = tableLayout.findViewById(R.id.tvAccessories);
        TextView tvShoes = tableLayout.findViewById(R.id.tvShoes);

        // 设置每行最多 5 个字符，并自动换行
        int maxCharsPerLine = 5;
        tvClothes.setFilters(new InputFilter[] { new LineBreakInputFilter(maxCharsPerLine) });
        tvAccessories.setFilters(new InputFilter[] { new LineBreakInputFilter(maxCharsPerLine) });
        tvShoes.setFilters(new InputFilter[] { new LineBreakInputFilter(maxCharsPerLine) });


        tvClothes.setText(clothes);
        tvAccessories.setText(accessories);
        tvShoes.setText(shoes);

        // 设置 TextView 的最大行数为自动换行
        tvClothes.setMaxLines(Integer.MAX_VALUE);
        tvAccessories.setMaxLines(Integer.MAX_VALUE);
        tvShoes.setMaxLines(Integer.MAX_VALUE);

//        //填入提示栏**********************************************************
        TextView tvScrollingText = scrollingTextCard.findViewById(R.id.tvScrollingText);
        tvScrollingText.setText(warningWords);
    }

    //根据天气设置不同的背景
    private static final Map<String, Integer> weatherBackgroundMap = new HashMap<String, Integer>() {{
        put("晴", R.drawable.sunny); // 晴天
//        put("少云", R.drawable.cloudy); // 少云
        put("云", R.drawable.cloudy); // 多云
        put("阴", R.drawable.yintain); // 阴天
        put("雨", R.drawable.rainy); // 小雨
//        put("中雨", R.drawable.rainy); // 中雨
//        put("大雨", R.drawable.rainy); // 大雨
        put("暴雨", R.drawable.rainy); // 暴雨
        put("雪", R.drawable.snowy); // 小雪
//        put("中雪", R.drawable.snowy); // 中雪
//        put("大雪", R.drawable.snowy); // 大雪
        put("雷", R.drawable.lei); // 雷阵雨
        put("雾", R.drawable.wu); // 雾
        put("薄雾", R.drawable.wu); // 薄雾
    }};
}
