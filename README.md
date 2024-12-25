# WeatherWardrobe 天气衣橱

## 一、项目简介

本项目是一个基于 Android 平台的根据天气推送穿搭的应用，旨在为用户提供实时的天气信息以及根据天气情况推荐的穿搭建议。应用通过定位功能以及网络请求来获取当前位置和对应的天气信息。系统通过调用 OpenWeatherMap API 获取天气数据，并根据天气情况动态调整界面背景和提供穿搭建议。

## 二、代码结构

### 1. androidTest
- 存放测试代码。

### 2. main
- **java**
  - `com.example.myapplication`：存放核心代码。
    - `ClothingRecommendation.java`：生成穿搭建议。
    - `MainActivity.java`：主界面逻辑。
    - `WeatherApi.java`：定义天气 API 接口。
    - `WeatherResponse.java`：定义天气数据模型。
- **res**
  - `drawable`：存放天气背景图。
  - `layout`：存放布局文件（如 `activity_main.xml`）。
  - `mipmap-*`：存放应用图标资源。
  - `values`：存放字符串、颜色、样式等资源。
  - `values-night`：存放夜间模式资源。
  - `xml`：存放其他 XML 配置文件。
- `AndroidManifest.xml`：定义应用的基本信息和组件。
