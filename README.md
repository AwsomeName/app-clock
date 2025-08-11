# 闹钟应用 (Clock App)

这是一个使用 Kotlin 和 Jetpack Compose 开发的 Android 闹钟应用。

## 功能特性

- ✅ 添加、编辑和删除闹钟
- ✅ 设置重复日期（周一到周日）
- ✅ 自定义铃声选择
- ✅ 震动设置
- ✅ 贪睡功能
- ✅ 音量控制
- ✅ 一次性闹钟和特定日期闹钟
- ✅ 闹钟响铃界面
- ✅ 后台服务和通知

## 技术栈

- **Kotlin** - 主要编程语言
- **Jetpack Compose** - 现代 UI 工具包
- **Room Database** - 本地数据存储
- **Hilt** - 依赖注入
- **WorkManager** - 后台任务调度
- **Navigation Compose** - 导航管理
- **Material Design 3** - UI 设计系统

## 项目结构

```
app/src/main/java/com/clockapp/
├── alarm/                  # 闹钟管理相关
│   ├── ClockAlarmManager.kt
│   └── AlarmReceiver.kt
├── data/                   # 数据层
│   ├── dao/
│   ├── entity/
│   ├── repository/
│   └── AlarmDatabase.kt
├── di/                     # 依赖注入
│   └── AppModule.kt
├── service/                # 后台服务
│   └── AlarmService.kt
├── ui/                     # UI 层
│   ├── alarm/
│   ├── main/
│   └── navigation/
├── utils/                  # 工具类
│   └── AlarmScheduler.kt
└── ClockApp.kt            # 应用入口
```

## 编译状态

✅ **编译成功** - 项目已成功编译，所有依赖项和代码错误已修复。

## 如何运行

1. **环境要求**
   - Android Studio Arctic Fox 或更高版本
   - Android SDK API 21+ (Android 5.0+)
   - Kotlin 1.8.22

2. **编译项目**
   ```bash
   ./gradlew assembleDebug
   ```

3. **安装到设备**
   ```bash
   ./gradlew installDebug
   ```
   注意：需要连接 Android 设备或启动模拟器

4. **在 Android Studio 中运行**
   - 打开项目
   - 选择设备或模拟器
   - 点击运行按钮

## 权限说明

应用需要以下权限：
- `SCHEDULE_EXACT_ALARM` - 精确闹钟调度
- `USE_EXACT_ALARM` - 使用精确闹钟
- `VIBRATE` - 震动功能
- `WAKE_LOCK` - 保持设备唤醒
- `RECEIVE_BOOT_COMPLETED` - 开机自启动

## 最近修复的问题

1. ✅ 修复了 `AlarmManager.kt` 删除后的引用问题
2. ✅ 统一了闹钟调度方法名称 (`scheduleAlarm` → `setAlarm`)
3. ✅ 修复了 `Alarm` 数据类属性不匹配问题
4. ✅ 移除了 Gson 依赖，使用原生字符串处理
5. ✅ 修复了 Compose 编译器版本兼容性问题
6. ✅ 修复了 WorkManager 配置问题
7. ✅ 修复了数据库引用路径问题

## 开发说明

- 使用 Hilt 进行依赖注入
- 遵循 MVVM 架构模式
- 使用 Repository 模式管理数据
- 采用 Compose 声明式 UI 开发
- 支持 Material Design 3 设计规范

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目。