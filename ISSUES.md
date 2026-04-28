# JdCookie 问题清单

## 优先级 P0 - 用户直接可感知的问题

### 1. 图标黑背景
- **文件**: `res/mipmap-anydpi-v26/ic_launcher.xml`, `ic_launcher_round.xml`
- **原因**: Adaptive Icon 缺少 `<background>` 元素，系统默认用黑色填充
- **修复**: 添加 background 引用 `@drawable/ic_launcher_background`

### 2. Token 缓存逻辑缺陷 + 空指针崩溃
- **文件**: `QingLong.kt`
- **问题 A**: `getToken()` 中 `tokenInfo["token"] != ""` 判断不严谨，cleared 后返回 null 而非 ""
- **问题 B**: 构造函数 `config["baseUrl"] as String` 无 null 保护，配置不完整时 NPE 崩溃
- **问题 C**: baseUrl 末尾斜杠未处理，拼接出双斜杠 URL
- **修复**: 统一 null 检查，添加安全转换，baseUrl 去尾斜杠

### 3. OkHttp 无超时配置
- **文件**: `HttpHelper.kt`
- **原因**: OkHttpClient 使用默认超时（connect 10s, read 10s, write 10s），网络异常时可能无限等待
- **修复**: 显式配置 connect/read/write 超时

## 优先级 P1 - 用户体验问题

### 4. WebView 无返回键处理
- **文件**: `MainActivity.kt`
- **原因**: 未重写 `onBackPressed`，按返回键直接退出而非回退 WebView 页面
- **修复**: 重写返回键，有历史记录时回退，无历史时退出

### 5. Secret Key 明文显示
- **文件**: `ConfigActivity` + `activity_config.xml`
- **原因**: EditText 未设置 `inputType="textPassword"` 或 `textVisiblePassword`
- **修复**: 添加密码输入模式，支持显示/隐藏切换

### 6. WebView 无网络错误处理
- **文件**: `MainActivity.kt`
- **原因**: WebView 加载失败时白屏无提示
- **修复**: 添加 `onReceivedError` 回调，显示错误提示

## 优先级 P2 - 功能增强

### 7. 日志面板
- **现状**: 所有日志只通过 `Log.d()` 输出到 logcat，用户无法查看
- **目标**: 新增日志面板 Activity，记录 API 请求/响应、Token 状态、推送结果
- **方案**: 创建 `LogHelper` 工具类 + `LogActivity` 展示界面
