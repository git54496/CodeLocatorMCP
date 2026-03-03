# CodeLocator MCP + Skill 一体化实施方案（替换现有方案并直接进入实施）

## 摘要
本次改造按已确认决策执行：
1. 同时落地 `MCP` 与 `Skill`。
2. 采用 `统一 CLI 内核`，MCP 与 Skill 共用同一套抓取、解析、存储、Viewer 能力。
3. Skill 安装到 `~/.codex/skills`，并按仓库规则执行 `push-skill-sync`。
4. Web Viewer 按 `MVP 可用对齐` 实现：截图、树、memAddr 搜索定位、高亮、属性面板、grab_id 切换。
5. 多设备默认最近成功设备，在命令行场景落地为 `macOS 通知 + 返回字段 device_notice`。

## 对外接口与类型变更

### 1. 统一可执行入口
目标产物：`codelocator-adapter`，Kotlin JVM。

命令接口：
1. `codelocator-adapter mcp`
2. `codelocator-adapter grab live --device-serial <optional> --json`
3. `codelocator-adapter grab file --path <.codeLocator> --json`
4. `codelocator-adapter grabs list --json`
5. `codelocator-adapter viewer open --grab-id <id> --json`
6. `codelocator-adapter inspect view-data --grab-id <id> --mem-addr <addr> --json`
7. `codelocator-adapter inspect class-info --grab-id <id> --mem-addr <addr> --json`
8. `codelocator-adapter inspect touch --grab-id <id> --json`

### 2. MCP 工具集
1. `grab_ui_state`
2. `load_local_grab`
3. `list_grabs`
4. `open_web_viewer`
5. `get_view_data`
6. `get_view_class_info`
7. `trace_touch`

### 3. 新增统一数据模型
1. `GrabMeta`：`grab_id`、`source`、`device_serial`、`package`、`activity`、`grab_time`、`device_notice`
2. `GrabSnapshot`：`meta`、`ui_tree`、`screenshot_ref`、`indexes`
3. `ViewNodeDto`：语义字段包含 `mem_addr`、位置、可见性、文本、类名
4. `ToolResult<T>`：`success`、`data`、`error`、`grab_id`
5. `McpError`：`code`、`message`、`details`

### 4. 本地存储协议
目录：`~/.codeLocator_mcp/grabs/<grab_id>/`

文件：
1. `meta.json`
2. `snapshot.json`
3. `screenshot.png`
4. `index.json`

## 实施计划

### Phase 0 文档替换
1. 覆盖本文件为 `MCP + Skill 一体化`版本。
2. 双入口架构：MCP 调用与 Skill 命令调用，共用统一内核。
3. 部署与使用章节明确两条流程：手动抓取回放、LLM 实时抓取。

### Phase 1 统一内核模块
新增独立模块 `CodeLocatorMCPAdapter`，不依赖 IntelliJ UI 组件。

子模块：
1. `adapter-core`
2. `adapter-cli`
3. `adapter-mcp`
4. `adapter-viewer`

实现要点：
1. 复用协议：`adb am broadcast + codeLocator_shell_args + Base64 + GZIP + FP/data`
2. 独立 `.codeLocator` 文件解析器，兼容插件历史文件。
3. 设备选择策略：优先最近成功设备，命中后返回 `device_notice` 并触发 macOS 通知。

### Phase 2 MCP Server 接入
1. 标准 MCP stdio 工具协议。
2. 工具参数与返回统一 `ToolResult`。
3. 所有工具返回 `grab_id`。

### Phase 3 Skill 落地
位置：`/Users/yebingyue/.codex/skills/codelocator-ui-diagnose`

内容：
1. `SKILL.md`
2. `references/commands.md`
3. `scripts/`

策略：
1. 默认优先 MCP。
2. MCP 不可用时降级 CLI。
3. 输出必须包含 `grab_id + viewer_url + 证据链步骤`。

合规动作：
1. 修改 `~/.codex` 白名单后执行 `push-skill-sync`。

### Phase 4 Web Viewer
本轮功能：
1. 截图画布和高亮
2. 左侧树
3. memAddr class text 搜索
4. 树与画布同步选中
5. 右侧属性面板
6. grab_id 切换
7. 复制 memAddr

不在本轮：
1. 编辑能力
2. 原生 IDE 跳转
3. 插件全部高级交互一比一复刻

### Phase 5 部署与使用闭环
#### A. 手动抓取回放
1. 用户在 Android Studio 插件抓取
2. LLM Skill 调 `grab file`
3. 生成 `grab_id`
4. 调 `viewer open`
5. 基于同一 `grab_id` 完成分析

#### B. LLM 实时抓取
1. 调 `grab live`
2. 返回 `grab_id`
3. 调 `viewer open`
4. 调 `inspect` 工具补充证据
5. 输出现象、证据、根因、建议

## 测试与验收场景

### 单元测试
1. Base64 URL Safe 与 GZIP 解码
2. FP 文件分支与 data 分支解析
3. `.codeLocator` 文件读取与容错
4. `get_view_data` JSON 探测与结构化返回

### 契约测试
1. MCP 七个工具入参校验
2. 错误码映射一致性
3. 所有工具返回 `grab_id`

### 集成测试
1. fake adb 回放
2. 多设备选择与最近设备通知
3. Viewer 加载指定 `grab_id` 数据正确性

### 冒烟验收
1. 真机和 SDK App：实时抓取到 viewer 到 trace 到 class info
2. 插件历史文件：读取 `~/.codeLocator_main/historyFile/*.codeLocator` 并可视化
3. Skill：自然语言触发后稳定执行并输出证据链

验收门槛：
1. 两条使用流程都可跑通
2. LLM 输出包含 `grab_id`
3. 用户可通过 Viewer 肉眼核对结论

## 假设与默认值
1. 本地 macOS，不做远程暴露
2. MCP 使用 stdio
3. Viewer 绑定 `127.0.0.1` 随机端口
4. 首版只读
5. 目标应用已集成 CodeLocator SDK
6. `grab_id` 作为跨工具证据主键
7. Skill 安装在 `~/.codex/skills` 并执行 `push-skill-sync`
