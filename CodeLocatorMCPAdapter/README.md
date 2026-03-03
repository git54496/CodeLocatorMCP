# CodeLocatorMCPAdapter

本模块提供 `CodeLocator` 的统一 CLI 内核、MCP stdio 服务和本地 Web Viewer。

## 构建

```bash
cd /Users/yebingyue/code/baron/CodeLocatorMCP/CodeLocatorMCPAdapter
./gradlew installDist
```

产物入口：

`/Users/yebingyue/code/baron/CodeLocatorMCP/CodeLocatorMCPAdapter/build/install/CodeLocatorMCPAdapter/bin/CodeLocatorMCPAdapter`

可软链为 `codelocator-adapter`：

```bash
ln -sf /Users/yebingyue/code/baron/CodeLocatorMCP/CodeLocatorMCPAdapter/build/install/CodeLocatorMCPAdapter/bin/CodeLocatorMCPAdapter /usr/local/bin/codelocator-adapter
```

## 命令

```bash
codelocator-adapter mcp
codelocator-adapter grab live --device-serial <optional> --json
codelocator-adapter grab file --path <optional> --json
codelocator-adapter grabs list --json
codelocator-adapter viewer open --grab-id <id> --json
codelocator-adapter inspect view-data --grab-id <id> --mem-addr <addr> --json
codelocator-adapter inspect class-info --grab-id <id> --mem-addr <addr> --json
codelocator-adapter inspect touch --grab-id <id> --json
```

## MCP 客户端配置

示例：

```json
{
  "mcpServers": {
    "codelocator": {
      "command": "/Users/yebingyue/code/baron/CodeLocatorMCP/CodeLocatorMCPAdapter/build/install/CodeLocatorMCPAdapter/bin/CodeLocatorMCPAdapter",
      "args": ["mcp"]
    }
  }
}
```

## 部署与使用流程

### 手动抓取回放

1. 在 Android Studio CodeLocator 插件抓取。
2. 执行 `codelocator-adapter grab file --json`。
3. 获取返回 `grab_id`。
4. 执行 `codelocator-adapter viewer open --grab-id <grab_id> --json`。
5. 使用 Viewer 和 MCP 工具完成分析。

### LLM 实时抓取

1. 执行 `codelocator-adapter grab live --json`。
2. 获取 `grab_id`。
3. 执行 `codelocator-adapter viewer open --grab-id <grab_id> --json`。
4. 根据 `mem_addr` 执行 inspect 工具。

## 本地数据目录

`~/.codeLocator_mcp/grabs/<grab_id>/`

- `meta.json`
- `snapshot.json`
- `screenshot.png`
- `index.json`
