package com.example.myapplication;

public class ClothingRecommendation {
    public static String getClothingRecommendation(WeatherResponse weather) {
        float temp = weather.main.temp;
        String description = weather.weather.get(0).description;
        float windSpeed = weather.wind.speed;

        if (temp < 0) {
            return "羽绒服 + 毛衣 + 围巾";
        } else if (temp >= 0 && temp < 15) {
            return "外套 + 长袖 + 长裤";
        } else if (temp >= 15 && temp < 25) {
            return "T恤 + 薄外套 + 牛仔裤";
        } else if (temp >= 25) {
            return "短袖 + 短裤 + 凉鞋";
        }

        if (description.contains("雨")) {
            return "雨伞 + 雨鞋";
        }

        if (windSpeed > 10) {
            return "防风外套";
        }

        return "舒适穿搭";
    }
}