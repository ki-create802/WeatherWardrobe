package com.example.myapplication;

public class ClothingRecommendation {
    public static String getClothingRecommendation(WeatherResponse weather) {
        float temp = weather.main.temp;  // 当前气温
//        String description = weather.weather.get(0).description;  // 天气描述
//        float windSpeed = weather.wind.speed;  // 风速
//        float humidity = weather.main.humidity;  // 湿度
        String recommendation = "舒适穿搭";//推荐穿搭

        // 针对气温判断穿着
        if (temp < 0) {
            recommendation = "羽绒服、厚毛衣、厚裤子 + 围巾 、帽子、手套 + 厚靴子";  // 严寒天气需要保暖措施
        } else if (temp >= 0 && temp < 10) {
            recommendation = "羽绒服、加厚外套、羊毛衫、保暖内衣 + 厚围巾、帽子、手套 + 保暖靴";

        } else if (temp >= 10 && temp < 15) {
            recommendation = "外套、夹克、毛衣、长裤 + 围巾、帽子、手套 + 靴子";

        }else if (temp >= 15 && temp < 20) {
            recommendation = "中等厚度的外套、长袖衬衫或毛衣、牛仔裤 + 舒适穿搭 + 运动鞋";

        }else if (temp >= 20 && temp < 25) {
            recommendation = "T恤、薄外套、牛仔裤 + 舒适穿搭 + 运动鞋、洞洞鞋";
        } else if (temp >= 25) {
            recommendation = "短袖、短裤 + 太阳镜（晴天）、遮阳帽（晴天） + 凉鞋、洞洞鞋";  // 适合高温时穿着轻便的衣物
        }

        // 默认推荐舒适穿搭
        return recommendation;
    }

    public static String getWarningRecommendation(WeatherResponse weather){
        float temp = weather.main.temp;  // 当前气温
        String description = weather.weather.get(0).description;  // 天气描述
        float windSpeed = weather.wind.speed;  // 风速
        float humidity = weather.main.humidity;  // 湿度

        String Warning = "";

        // 检查天气描述来进行进一步判断
        if (description.contains("雨")) {
            Warning +=  "今天有雨，请记得带好雨伞，必要时配备雨衣雨鞋，注意安全。";  // 雨天推荐雨具装备
        }

        if (description.contains("雪")) {
            Warning +=  "今天有雪，注意出行安全。";
        }

        if (description.contains("晴") ) {
            Warning +=  "今天是个好天气!";
        }

        // 如果风速大于10m/s，建议穿防风外套
        if (windSpeed > 10) {
            if (temp < 15) {
                Warning += "今日风大且气温较低，建议您出门时备好防风外套，避免着凉。";
            } else {
                Warning += "今日风大，建议您出门备好防风外套，保持舒适。";
            }
        }

        // 湿度过高时，适合穿透气和干爽的衣物
        if (humidity > 80) {
            if (temp > 25) {
                Warning += "今日湿度较大，建议穿搭透气、轻便衣物以保持干爽。";
            } else {
                Warning += "今日湿度较大，建议穿透气衣物以应对潮湿天气。";
            }
        }

        // 针对温度较低的情况加上保暖提醒
        if (temp < 0) {
            Warning += "今天气温很低，请穿上羽绒服和保暖衣物，保持温暖。";
        } else if (temp >= 0 && temp < 15) {
            Warning += "气温较低，请穿上外套和长袖衣物以保暖。";
        }

        if(Warning.isEmpty()){
            Warning = "祝您今天也有好心情!";
        }
        return Warning;
    }
}
