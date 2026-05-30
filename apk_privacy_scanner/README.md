# Android APK 隐私安全扫描器

> 静态分析 Android APK 文件，检测隐私和安全风险

## 功能

7 项自动化检测，覆盖 APK 隐私安全的核心维度：

| # | 模块 | 检测内容 |
|---|------|---------|
| 1 | **权限分析** | 敏感权限申请 + 高危权限组合检测（间谍软件/短信劫持/完全监控等） |
| 2 | **组件暴露分析** | 导出的 Activity/Service/Receiver/Provider |
| 3 | **SDK/追踪器分析** | 识别 Firebase/友盟/穿山甲/广告/分析等 60+ 种第三方 SDK |
| 4 | **网络地址分析** | 硬编码 URL/IP/域名，检测可疑域名 |
| 5 | **签名信息** | 证书 SHA1、签名者、是否调试证书 |
| 6 | **敏感API检测** | 获取 IMEI/IMSI/位置/联系人等隐私 API 调用 |
| 7 | **综合风险评估** | 加权评分 + 风险等级（低/中/较高/高）+ 建议 |

## 快速开始

### 安装依赖

```bash
pip install androguard
```

### 运行

```bash
# 方式 1：直接运行
python apk_privacy_scanner.py your-app.apk

# 方式 2：使用批处理（Windows）
run_scanner.bat your-app.apk
```

### 示例输出

```
  风险评分: 15 / 100+
  风险等级: [LOW] 中等风险
  建议: 存在一定隐私风险，建议谨慎授权

  风险项汇总:
    > 大量硬编码域名(35个)
    > 敏感API: 获取WiFi BSSID
    > 敏感API: 获取WiFi SSID
```

## 技术架构

```
apk_privacy_scanner.py
│
├── androguard (APK 解析引擎)
│   ├── APK 解析 → AndroidManifest.xml / DEX / 资源
│   ├── 权限提取 → get_permissions()
│   ├── 组件提取 → get_activities() / get_services() 等
│   ├── 证书提取 → get_certificates()
│   └── DEX 分析 → 字符串提取 / 类名提取
│
├── 检测引擎（自建）
│   ├── 危险权限库（70+ 权限定义）
│   ├── 高危权限组合（6 种风险模式）
│   ├── SDK/追踪器库（60+ 第三方 SDK 签名）
│   ├── 敏感 API 模式（18 种隐私 API）
│   └── URL/IP 正则提取
│
└── 评分引擎
    ├── 加权评分（权限 10 分 / 组合 30 分 / SDK 3-20 分 / ...）
    ├── 风险等级（低 / 中 / 较高 / 高）
    └── 风险项汇总
```

## 风险评分规则

| 检测项 | 单次扣分 |
|--------|---------|
| 危险权限 | +10 分 |
| 高危权限组合 | +30 分 |
| 高风险 SDK | +20 分 |
| 中风险 SDK | +10 分 |
| 低风险 SDK | +3 分 |
| 可疑域名 | +15 分 |
| 敏感 API 调用 | +5 分 |
| 调试证书 | +5 分 |
| 大量组件/域名 | +5 分 |

**风险等级：** ≤10 低风险 / ≤30 中等 / ≤60 较高 / >60 高

## 识别 SDK 清单（部分）

| 类别 | SDK |
|------|-----|
| 广告 | Google AdMob、穿山甲/Pangle、优量汇、快手、Unity Ads、AppLovin、ironSource |
| 分析 | Firebase、友盟、百度统计、AppsFlyer、Adjust、Amplitude、Mixpanel |
| 社交 | Facebook SDK、Twitter SDK、微信 SDK、微博 SDK |
| 推送 | 个推、极光、小米/华为/OPPO/vivo 推送 |
| 支付 | 支付宝、微信支付、银联、PayPal、Stripe |
| 地图 | 高德、百度、Google Maps |

## 与业界工具对比

| 特性 | 本工具 | Exodus Privacy | PCAPdroid | MobSF |
|------|--------|---------------|-----------|-------|
| 运行环境 | PC(Python) | 手机 App/网站 | Android 手机 | PC(Web) |
| APK 静态分析 | ✅ | ✅ | ❌ | ✅ |
| 实时流量监控 | ❌ | ❌ | ✅ | ❌ |
| 危险权限组合检测 | ✅ | ❌ | ❌ | ❌ |
| 敏感 API 检测 | ✅ | ❌ | ❌ | ✅ |
| SDK/追踪器识别 | ✅ (60+) | ✅ (300+) | ❌ | ✅ |
| 综合风险评分 | ✅ | ❌ | ❌ | ✅ |
| 中文本地化 | ✅ | 部分 | ✅ | 部分 |
| 安装复杂度 | 轻量(pip) | 手机安装 | 手机安装 | 较重(Docker) |

## License

MIT
