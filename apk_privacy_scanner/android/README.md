# APK Privacy Scanner Android

一个 Android 原生 APK 隐私安全静态扫描工具 MVP。

## 当前版本

```text
v1.0.0-mvp-debug
```

当前版本已完成手机端 MVP 验证：

- App 能打开。
- 能选择 APK。
- 能完成扫描。
- 结果页能展示风险结果。
- Markdown 报告分享入口可用。

## 功能

当前支持：

- 通过系统文件选择器选择 APK。
- 本地读取 APK。
- APK ZIP entry 结构扫描。
- DEX 字符串提取。
- Tracker / SDK 规则匹配。
- 敏感 API 规则匹配。
- URL / IP / 域名 / 可疑域名线索提取。
- 初版风险评分。
- 结果页展示。
- Markdown 文本报告分享。

## 权限边界

当前版本不声明任何 Android `uses-permission`。

工具不会：

- 申请全盘文件权限。
- 申请网络权限。
- 上传 APK。
- 安装被扫描 APK。
- 执行 APK 内代码。

文件读取方式：

```text
系统文件选择器 + 用户授权 Uri
```

## 构建方式

```bash
./gradlew assembleDebug
```

Debug APK 输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 当前限制

当前暂不支持：

- 选择已安装 App 进行分析。
- Manifest 权限正式解析。
- 当前权限授权状态读取。
- App 类型与权限合理性评分。
- 权限变化监控。
- 实时权限获取记录监控。
- HTML 报告生成。
- 扫描历史记录。
- 报告保存为文件。

## 后续计划

建议按小版本推进：

```text
v1.0.1：MVP 收尾与稳定版归档
v1.1.0：已安装 App 列表入口
v1.1.1：已安装 App APK 扫描
v1.2.0：权限申请与当前授权状态读取
v1.2.1：评分体系第一轮升级
v1.3.0：App 类型与权限合理性
v2.0：权限变化监控 / 准实时提醒
```

## 说明

本工具基于静态规则进行隐私安全初筛，不等同完整安全审计结论。
