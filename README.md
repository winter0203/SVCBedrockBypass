# SVCBedrockBypass

一个轻量级 Paper 插件，用于解决 **Simple Voice Chat 开启 `force_voice_chat=true` 后，Geyser 基岩版玩家也被强制检查并踢出** 的问题。

## 功能

- Java 玩家仍然受 Simple Voice Chat 的强制安装检查约束。
- 通过 Floodgate API 或 Geyser API 识别基岩版玩家。
- 只取消匹配 Simple Voice Chat 强制检查提示的踢出事件。
- 基岩版玩家因封禁、反作弊或其他插件原因被踢出时，不会被本插件放行。
- Geyser/Floodgate API 通过反射调用，不会把可选 API 打包进插件。

> 本插件只绕过“必须安装客户端模组”的踢出检查，不会让基岩版客户端获得 Simple Voice Chat 原生语音功能。

## 运行要求

- Paper 或兼容 Paper API 的服务端（Purpur 通常兼容）
- Java 21 或更高版本
- Simple Voice Chat Bukkit/Paper 插件
- Geyser-Spigot 或 Floodgate（推荐安装 Floodgate）

## 构建

需要 JDK 21 和 Gradle 8.8 或更高版本：

```bash
gradle clean build
```

产物位于 `build/libs/SVCBedrockBypass-1.0.0.jar`。

## 自动构建与发布

仓库已配置 GitHub Actions：

- 在 Actions 页面手动运行 **Build and Release** 可构建并保存 JAR Artifact。
- 推送以 `v` 开头的标签（例如 `v1.0.0`）会自动构建，并创建 GitHub Release，将 JAR 附加到 Release。

## 安装

1. 确保服务器安装 Simple Voice Chat、Geyser 和 Floodgate（推荐）。
2. 将 `SVCBedrockBypass-1.0.0.jar` 放入服务器的 `plugins/` 文件夹。
3. 启动服务器一次，生成 `plugins/SVCBedrockBypass/config.yml`。
4. 确认 `enabled: true`，然后重启服务器。
5. 在 Simple Voice Chat 的 `plugins/voicechat/voicechat-server.properties` 中按需设置：

```properties
force_voice_chat=true
login_timeout=10000
```

## 配置

默认配置：

```yaml
enabled: true

bedrock-detection:
  floodgate: true
  geyser: true

kick-reason-patterns:
  - '(?i)^You need .+ to play on this server$'

debug: false
```

默认规则匹配 Simple Voice Chat 的英文提示：

```text
You need <插件名> <版本> to play on this server
```

如果修改了 Simple Voice Chat 的 `force_voicechat_kick_message`，请将自定义踢出文本转换为 Java 正则表达式，添加到 `kick-reason-patterns`。

## 验证

1. Java 客户端未安装 Simple Voice Chat：应被踢出。
2. Java 客户端已安装 Simple Voice Chat：应正常进入。
3. Geyser 基岩版客户端：应绕过强制安装检查。
4. 基岩版玩家因封禁或其他原因被踢出：仍应正常被踢出。

## 实现原理

插件监听 Bukkit `PlayerKickEvent`。只有当玩家被 Floodgate/Geyser API 识别为基岩版，且踢出原因匹配配置规则时，才取消踢出事件。

## 许可证

MIT License
