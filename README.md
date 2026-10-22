# 交大自由行（BJTU Self Service）

> 面向北京交通大学学生的 Android 校园服务客户端，将课表、成绩、考试、作业、课件、邮件、教室占用和校历等常用功能集中在一个应用中。
<!-- # test -->
[![Latest Release](https://img.shields.io/github/v/release/AndrewYuZhenYu/BJTUselfService?style=flat-square&label=最新版本)](https://github.com/AndrewYuZhenYu/BJTUselfService/releases/latest)
[![Build Debug APK](https://github.com/AndrewYuZhenYu/BJTUselfService/actions/workflows/debug.yml/badge.svg)](https://github.com/AndrewYuZhenYu/BJTUselfService/actions/workflows/debug.yml)
[![Android](https://img.shields.io/badge/Android-9.0%2B-brightgreen?style=flat-square&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/github/license/AndrewYuZhenYu/BJTUselfService?style=flat-square)](LICENSE)

本仓库由 [HFDLYS/BJTUselfService](https://github.com/HFDLYS/BJTUselfService) Fork 并持续开发，完整保留原项目历史。项目不是北京交通大学官方应用；课程、成绩、教室和校历等信息最终以学校系统为准。

## 当前版本

当前稳定版本为 **[v1.10.0 功能精进版](https://github.com/AndrewYuZhenYu/BJTUselfService/releases/tag/v1.10.0)**。

- 支持 Android 9.0（API 28）及以上版本。
- 当前 Release APK 仅提供 `arm64-v8a` 架构，适用于绝大多数近年 Android 手机。
- [直接下载 APK](https://github.com/AndrewYuZhenYu/BJTUselfService/releases/download/v1.10.0/app-arm64-v8a-release.apk)
- APK SHA-256：`85fb1c81c4da5cea9776e6c51e29dbee12146eb7a86dfae1f634d95fc9dcc390`

## 功能全景

### 首页

- 邮件、成绩、作业三个快捷入口，并显示对应变动数量。
- 当前教学周和一周日期概览。
- “本日课程”按节次显示课程名称、时间、地点和教师。
- “其他通知”折叠收纳校园卡余额、校园网余额和课表变动等低频信息。
- 支持下拉刷新并同步相关校园数据。

### 课程表

- 提供“本周”和“总览”两种模式。
- 支持全部周及第 1 至 26 周切换，并可自动跟随教务系统同步的当前教学周。
- 星期表头、时间列和 7 列课程网格严格对齐。
- 一屏优先呈现约六个半节次，兼顾常见手机比例与纵向浏览。
- 课程卡片使用高对比度配色，主界面突出课程名称与简化地点，不展示低频课程编号和教师信息。
- 教学楼与教室分行显示；常见长楼名按约定简写，同时保留具体教室方位与编号。
- 同一时间存在单双周或多门课程时，在同一格内分别展示并可逐项查看。
- 点击课程后显示完整原始信息，包括课程编号、课程名称、教师、上课时间和上课地点。

### 课前系统通知

- 可在“设置 > 课前提醒”中开启或关闭。
- 支持提前 **5、10、15、30 分钟**提醒，默认提前 10 分钟。
- 通知正文仅保留课程名称和简化后的上课地点，点击通知可直接进入课程表。
- 依据本地 Room 中的课程、教务系统确认的本周周数、单双周规则和课程节次生成提醒。
- 同一时间的不同课程分别处理，并通过事件键避免重复提醒。
- 设备重启、应用更新、系统时间或时区变化后会重新整理提醒。
- 应用退出后仍可由 Android 系统触发。

提醒正常工作需要满足：

1. 本周课表和当前教学周已在本周内通过登录同步确认。
2. Android 13 及以上已允许通知权限。
3. Android 12 及以上已允许“闹钟和提醒”精确调度权限。
4. 系统没有关闭“课前提醒”通知渠道。

这里的 `AlarmManager` 只负责在指定时刻唤醒通知任务，不会在系统时钟应用中创建可见闹钟。

### 教室占用

数据来自北京交通大学教学支撑平台的教室使用查询页面，并重新设计为移动端界面。

- 按教学周、具体日期、星期和节次查看。
- 日期选择器同时展示日期与教学周，降低看错周次的概率。
- 本周默认定位到今天，日期条会自动滚动到当前日期附近。
- 楼宇选择使用学校页面提供的完整名称和完整列表。
- 支持按教室名称搜索。
- 支持“空闲”和“全部”两种筛选模式。
- 可查看排课、调课、考试、实验及其他安排等占用类型。
- 查询结果短时缓存 2 分钟，手动刷新可跳过缓存。

该功能依赖学校网站结构和当前登录状态。学校页面调整、会话失效或网络异常都可能导致暂时无法加载。

### 校历查看

- 读取北京交通大学本科生院校历数据。
- 支持切换网站提供的多个学期。
- 将原网页内容转换为常规的周一至周日横向月历。
- 在日期格中标记教学周，并展示校历备注。
- 支持调用 Android 打印服务打印，或保存为 PDF。

### 学业与校园服务

| 功能 | 当前能力 |
| --- | --- |
| 成绩 | 按学期筛选、查看详情、计算加权平均分，并支持自选课程参与计算 |
| 考试安排 | 查看考试课程、时间和地点 |
| 作业 | 查看与筛选作业、截止提醒、提交作业、下载附件和查看批改状态 |
| 课件 | 按课程浏览资源树，下载文件、文件夹和教学日历，并查看下载进度 |
| 校内邮件 | 复用登录会话在应用内打开校内邮箱 |
| 成绩单 | 下载中文或英文成绩单 |
| 作业提醒订阅 | 可使用现有第三方订阅服务，界面会明确提示其第三方属性与隐私风险 |
| 教室人数评估 | 保留原项目的教室人数评估入口 |
| 桌面组件 | 使用 Jetpack Glance 展示课程表组件 |
| 个性化 | 支持浅色、深色、跟随系统、动态配色和自定义背景 |
| 数据同步 | 可分别控制成绩、作业、课表和考试的自动同步 |
| 应用更新 | 启动检查或手动检查 GitHub 最新版本 |

## 安装与使用

### 直接安装 APK

1. 打开 [Releases](https://github.com/AndrewYuZhenYu/BJTUselfService/releases) 并选择最新稳定版本。
2. 下载 `app-arm64-v8a-release.apk`。
3. 在手机文件管理器中打开 APK；首次安装时，按系统提示允许该文件来源安装未知应用。
4. 启动“交大自由行”，输入北京交通大学统一身份认证所需的账号信息。
5. 登录并完成一次同步后，首页、课表和其他功能会显示已保存的数据。

若系统提示“应用未安装”，请先确认设备 CPU 支持 `arm64-v8a`，并检查是否存在由不同签名证书安装的同包名旧版本。卸载旧版本会同时清除其本地数据，请先自行确认。

### 首次使用课前提醒

1. 打开应用并确认课表页面能够正确显示本周课程和教学周。
2. 进入“设置 > 课前提醒”，开启“课前通知”。
3. 选择提前分钟数。
4. 根据系统提示授予通知权限和精确提醒权限。
5. 不需要保持应用常驻；系统会在计划时间发送通知。

## 项目演进复盘

本 Fork 在原项目 `v1.7.1` 的完整功能和历史之上，按最小范围、复用现有架构的原则完成了三个稳定阶段。

### v1.8.0：UI 美化版

对应提交：[`4c2b0db`](https://github.com/AndrewYuZhenYu/BJTUselfService/commit/4c2b0db5a491fd2fe559ce3a123e8e6561405142)

- 集中重构 `CourseScheduleScreen.kt`，未修改课程实体、数据库结构或网络接口。
- 将课表入口统一为“本周/总览”，整理学期与周数控制区。
- 重做表头、时间列和课程网格的尺寸关系。
- 优化课程卡片配色、长文本、地点格式、多课程同格展示和详情弹窗。

### v1.9.0：优化稳定版

对应提交：[`6af4f1a`](https://github.com/AndrewYuZhenYu/BJTUselfService/commit/6af4f1a923077bb14791be640ce5a246128ed877)

- 基于现有 Room 课程数据和 DataStore 设置增加课前系统通知。
- 复用教务系统同步得到的当前教学周，不自行推算包含节假日的完整校历周次。
- 增加周次解析、地点简化、提醒规划、精确调度、去重和重启恢复。
- 重构首页信息层级，加入通知快捷入口、教学周、本日课程和折叠的其他通知。
- 增加提醒规划与地点格式单元测试。

### v1.10.0：功能精进版

对应提交：[`7781b93`](https://github.com/AndrewYuZhenYu/BJTUselfService/commit/7781b9394a6849e4e47e61933f5d858c94d8eb12)

- 新增移动端教室占用查询，复用现有认证会话访问学校页面。
- 加入完整楼宇筛选、日期与教学周联动、教室搜索、空闲筛选和短时缓存。
- 将原“校历下载”升级为原生校历查看，并保留打印或保存 PDF 能力。
- 调整首页图标和应用入口排序。
- 增加教室占用和校历 HTML 解析测试。

三个版本均使用独立的 Annotated Tag 和 GitHub Release，且准确指向各自稳定提交，没有改写原作者历史。

## 技术架构

```text
Compose UI / Navigation
        │
        ▼
ViewModel + StateFlow
        │
        ├── NetworkRepository / 页面专用 Repository
        │        ├── OkHttp
        │        └── Jsoup / Gson / Kotlin Serialization
        │
        ├── DatabaseRepository
        │        └── Room + DAO
        │
        └── DataStoreRepository
                 └── 用户设置、同步状态、提醒记录

Android system services
        ├── AlarmManager + BroadcastReceiver + NotificationManager
        ├── PrintManager + WebView
        └── Glance AppWidget
```

### 主要技术

| 分类 | 实现 |
| --- | --- |
| 语言与 UI | Kotlin、Jetpack Compose、Material 3 |
| 状态与架构 | ViewModel、StateFlow、Repository |
| 本地数据库 | Room |
| 偏好设置 | DataStore Preferences |
| 网络 | OkHttp 4 |
| 页面与数据解析 | Jsoup、Gson、Moshi、Kotlin Serialization |
| 依赖注入 | Dagger Hilt |
| 验证码识别 | PyTorch Android，本地模型推理 |
| 日历 | Kizitonwose Calendar 与原生日历布局 |
| 桌面组件 | Jetpack Glance |
| 图片处理 | uCrop |

### 关键目录

```text
app/src/main/
├── java/team/bjtuss/bjtuselfservice/
│   ├── screen/          # Compose 页面
│   ├── viewmodel/       # 页面状态、同步与业务协调
│   ├── repository/      # 网络、本地数据和页面解析
│   ├── entity/          # Room 实体
│   ├── dao/             # Room DAO
│   ├── database/        # 数据库定义与迁移
│   ├── notification/    # 课前提醒规划、调度与 Receiver
│   ├── utils/           # 周次、地点、下载等通用逻辑
│   ├── widget/          # 课程表桌面组件
│   └── web/             # MIS 与教室人数相关旧模块
├── assets/              # 本地验证码模型等资源
└── res/                 # 图标、主题、字符串与组件配置

app/src/test/             # JVM 单元测试
app/src/androidTest/      # Android 仪器测试
.github/workflows/        # Debug 与 Release 构建流程
```

### 重点文件

| 文件 | 作用 |
| --- | --- |
| `MainActivity.kt` | 应用入口、导航、主题与更新检查 |
| `HomeScreen.kt` | 首页通知、教学周与本日课程 |
| `CourseScheduleScreen.kt` | 课程表网格、周数切换与课程详情 |
| `CourseScheduleViewModel.kt` | 课表数据流与同步入口 |
| `CourseReminderPlanner.kt` | 将课程、周次、星期和节次转换为提醒事件 |
| `CourseReminderScheduler.kt` | 精确调度、通知发送、去重与恢复 |
| `RoomOccupancyRepository.kt` | 教室占用页面请求、解析、分页与缓存 |
| `RoomOccupancyScreen.kt` | 教室占用移动端筛选与展示 |
| `SchoolCalendarRepository.kt` | 学校校历数据解析 |
| `SchoolCalendarScreen.kt` | 月历展示、学期切换与打印 |
| `DataStoreRepository.kt` | 用户偏好、当前教学周和提醒状态 |
| `AppDatabase.kt` | Room 数据库和实体注册 |

## 本地开发

### 环境要求

- Android Studio（建议使用当前稳定版）
- JDK 17，用于与 GitHub Actions 构建环境保持一致
- Android SDK 34
- Git
- Android 9.0 及以上真机或模拟器

项目的 Java/Kotlin 字节码目标为 Java 11，但 Gradle 和 CI 使用 JDK 17 运行。

### 获取与构建

```bash
git clone https://github.com/AndrewYuZhenYu/BJTUselfService.git
cd BJTUselfService

# macOS / Linux
chmod +x gradlew
./gradlew test
./gradlew assembleDebug
```

Debug APK 默认输出到：

```text
app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

安装到已连接设备：

```bash
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

也可以直接用 Android Studio 打开项目，等待 Gradle Sync 完成后选择设备并点击 Run。成功标志是设备上出现“交大自由行”登录界面；登录后可进入首页、应用和设置三个主导航页面。

### 测试

```bash
# JVM 单元测试
./gradlew test

# 连接设备或启动模拟器后运行仪器测试
./gradlew connectedAndroidTest
```

当前专项单元测试覆盖：

- 课前提醒时间规划与周次过滤。
- 课程地点简化规则。
- 教室占用 HTML 解析。
- 校历页面数据解析。
- 成绩自选计算与状态清理。

### CI 与发布

- `debug.yml`：推送到 `main` 或手动触发时构建 Debug APK。
- `release.yml`：推送 `v*` Tag 时构建、签名并创建 Release。
- Release 工作流需要配置 `RELEASE_KEYSTORE_BASE64`、`KEY_ALIAS`、`KEYSTORE_PASSWORD` 和 `KEY_PASSWORD` Secrets。
- 创建新版本前应同时核对 Git Tag、`versionName`、`versionCode` 和 Release Notes，避免应用内部版本与发布标签不一致。

本仓库保留原作者历史 Tag；本 Fork 的稳定版本从 `v1.8.0` 开始。

## 数据、权限与隐私

- 账号和密码由应用保存在本机 DataStore 中，用于恢复登录；当前实现不是独立的端到端加密密码库。
- 成绩、课程、考试和作业等结构化数据保存在本机 Room 数据库中。
- 验证码识别使用 APK 内置 PyTorch 模型在设备本地执行。
- 应用会访问北京交通大学相关系统及 GitHub Release API；“作业提醒订阅”明确属于第三方服务。
- 通知权限用于显示课前提醒。
- 精确提醒权限用于按所选提前量调度课程通知。
- 开机完成权限用于设备重启后恢复提醒。
- 存储或媒体相关权限用于背景图片、作业附件、课件及文档下载，具体授权行为会因 Android 版本而异。

不要在 Issues、日志截图或公开文件中提交学号、密码、Cookie、Token、签名文件或其他个人信息。

## 已知限制

- 学校系统没有稳定公开 API，多项功能依赖登录后的网页或接口；页面结构变化可能导致解析暂时失效。
- 当前教学周以教务系统同步结果为准。没有在本周成功确认教学周时，课前提醒不会自行猜测周次。
- Android 厂商的后台限制、通知渠道设置和省电策略可能影响通知准时性。
- 当前仅构建 `arm64-v8a` APK，不支持仅有 32 位 ABI 的设备。
- 教室占用与校历需要联网读取学校页面，不是永久离线数据。
- Room 数据库启用了未提供迁移时的破坏性回退，未来调整实体结构时必须谨慎补充迁移方案。

## 常见问题

### 登录后没有数据

确认网络能够访问学校系统，然后在首页下拉刷新。若登录状态失效，应用会重新进入登录流程。

### 课程表正确，但没有课前通知

依次检查课前通知开关、当前周是否在本周同步、通知权限、精确提醒权限和“课前提醒”通知渠道。修改设置后重新打开应用会触发提醒整理。

### 教室占用加载失败或非常慢

先确认学校网站可以访问及账号会话有效，再点击页面右上角刷新。选择“全部楼宇”时需要读取更多分页数据，按具体楼宇查询通常更快。

### 校历无法显示

校历依赖本科生院页面。网络异常或网站改版会导致解析失败，可稍后刷新重试。

### APK 无法覆盖安装

通常是设备架构不匹配，或手机上已有相同包名但签名不同的版本。若决定卸载旧版，请先了解卸载会删除应用本地数据库和设置。

## Git 远程与版本历史

推荐保留以下远程关系，便于继续开发并同步原项目：

```bash
git remote -v

# origin   https://github.com/AndrewYuZhenYu/BJTUselfService.git
# upstream https://github.com/HFDLYS/BJTUselfService.git
```

同步原项目时应先检查工作区和分支关系，不使用强制推送改写已有历史。

## 致谢

感谢原项目作者与所有历史贡献者。本 Fork 保留原项目 Commit 和 Tag 历史，主要贡献记录可通过 GitHub Contributors 与提交历史查看。

- [HFDLYS](https://github.com/HFDLYS)：原项目维护者
- [optsimauth](https://github.com/optsimauth)：架构重构与后续维护
- [guh0613](https://github.com/guh0613)：自动构建与发布
- [carolyn-sun](https://github.com/carolyn-sun)：工作流优化
- [NAPHthalene130](https://github.com/NAPHthalene130)：问题修复
- [B-Silva20](https://github.com/B-Silva20)：成绩自选课程计算
- [wangxiaobo1747](https://github.com/wangxiaobo1747)：自定义壁纸与桌面课程表组件

当前 Fork 维护者：[AndrewYuZhenYu](https://github.com/AndrewYuZhenYu)

## License

项目使用 [MIT License](LICENSE)。二次开发和分发时请保留许可证与原作者信息。
