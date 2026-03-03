# CodeLocatorPRO Workspace

CodeLocatorPRO 是一个由 `core + plugin + viewer + mcp` 组成的工作区，用于 Android UI 抓取、定位与可视化分析。

## 1 分钟快速开始

```bash
cd /Users/yebingyue/code/baron/CodeLocatorPRO/mcp
./gradlew installDist --no-daemon

cd /Users/yebingyue/code/baron/CodeLocatorPRO
./start_viewer.sh 49622
```

启动后会自动打开浏览器，默认地址为 `http://127.0.0.1:49622/`。

## 目录结构

- `core`
  - Android 侧抓取、分析、协议模型与动作执行核心逻辑。
- `plugin`
  - Android Studio / IntelliJ 插件实现。
- `viewer`
  - Web Viewer 前端资源（页面静态文件）。
- `mcp`
  - Adapter CLI + MCP(stdio) + 本地 Viewer HTTP 服务。
- `start_viewer.sh`
  - 一键启动本地 Viewer（会先检查 adapter 可执行文件是否存在）。

## 构建 Adapter

```bash
cd /Users/yebingyue/code/baron/CodeLocatorPRO/mcp
./gradlew installDist --no-daemon
```

产物路径：

```bash
/Users/yebingyue/code/baron/CodeLocatorPRO/mcp/build/install/CodeLocatorMCPAdapter/bin/CodeLocatorMCPAdapter
```

## CLI 常用命令

```bash
BIN="/Users/yebingyue/code/baron/CodeLocatorPRO/mcp/build/install/CodeLocatorMCPAdapter/bin/CodeLocatorMCPAdapter"

# 实时抓取（可选 --device-serial）
$BIN grab live --json

# 从文件导入抓取（可选 --path）
$BIN grab file --json

# 列出抓取记录
$BIN grabs list --json

# 打开 Viewer（指定 grab_id）
$BIN viewer open --grab-id <grab_id> --json

# 启动 Viewer 服务
$BIN viewer serve --port 49622

# 启动 MCP stdio 服务
$BIN mcp
```

## `start_viewer.sh` 参数

```bash
./start_viewer.sh [port] [grab_id]
```

- `port`：可选，默认 `49622`
- `grab_id`：可选，传入后自动打开对应抓取记录页面

示例：

```bash
# 默认端口启动
./start_viewer.sh

# 指定端口
./start_viewer.sh 17333

# 指定端口并直达某次抓取
./start_viewer.sh 49622 20260303_101530_xxx
```

## 常见问题

- 报错 `Viewer binary not found`
  - 说明未构建 adapter，先执行：
    - `cd /Users/yebingyue/code/baron/CodeLocatorPRO/mcp && ./gradlew installDist --no-daemon`
- 端口被占用
  - `start_viewer.sh` 会尝试自动释放目标端口上已监听进程。
