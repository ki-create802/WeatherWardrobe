Weather Wardrobe天气衣橱
一、项目简介
    本项目是一个基于Android平台的根据天气推送穿搭的应用，旨在为用户提供实时的天气信息以及根据天气情况推荐的穿搭建议。应用通过定位功能以及网络请求来获取当前位置和对应的天气信息。系统通过调用OpenWeatherMap API获取天气数据，并根据天气情况动态调整界面背景和提供穿搭建议。
二、代码结构
WeatherWardrobe [Android Project]
├── androidTest
│   └── com.example.myapplication
│       └── (测试代码)
├── main
│   ├── java
│   │   └── com.example.myapplication
│   │       ├── ClothingRecommendation.java
│   │       ├── MainActivity.java
│   │       ├── WeatherApi.java
│   │       └── WeatherResponse.java
│   └── res
│       ├── drawable
│       │   └── (天气背景图等资源)
│       ├── layout
│       │   └── activity_main.xml
│       ├── mipmap-*
│       │   └── (应用图标资源)
│       ├── values
│       │   └── (字符串、颜色、样式等资源)
│       ├── values-night
│       │   └── (夜间模式资源)
│       ├── xml
│       │   └── (其他 XML 配置文件)
│       └── AndroidManifest.xml
└── (其他项目文件)

