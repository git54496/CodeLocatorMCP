# CodeLocator MCP + Skill 实施进展

更新时间：2026-03-03

## 1. 当前阶段判断

当前已进入 **Phase 5（部署与使用闭环）联调收尾阶段**：
- `MCP + Skill + CLI + Viewer` 主体代码已落地。
- 文档已从旧方案切换为 `MCP + Skill 一体化`。
- 构建与核心冒烟命令可跑通。
- 剩余工作主要是提交同步与端到端稳定性补验。

## 2. 已完成项（按方案 Phase 对齐）

### Phase 0 文档替换
- 已完成：`/Users/yebingyue/code/baron/CodeLocatorMCP/doc/tech_plan.md` 已替换为 MCP+Skill 一体化版本。

### Phase 1 统一内核模块
- 已完成：新增模块 `/Users/yebingyue/code/baron/CodeLocatorMCP/CodeLocatorMCPAdapter`。
- 已完成：CLI 子命令实现（`mcp / grab live / grab file / grabs list / viewer open / inspect *`）。
- 已完成：抓取协议链路（adb broadcast、Base64/GZIP、FP/data 分支）与 `.codeLocator` 文件解析。
- 已完成：本地存储协议 `~/.codeLocator_mcp/grabs/<grab_id>/`。
- 已完成：最近成功设备策略与 `device_notice`，并触发 macOS 通知。

### Phase 2 MCP Server
- 已完成：stdio MCP server 与 7 个只读工具实现。
- 已完成：统一 `ToolResult`/`McpError` 返回结构。
- 已完成：工具链返回 `grab_id` 约束。

### Phase 3 Skill 落地
- 已完成：
  - `/Users/yebingyue/.codex/skills/codelocator-ui-diagnose/SKILL.md`
  - `/Users/yebingyue/.codex/skills/codelocator-ui-diagnose/references/commands.md`
  - `/Users/yebingyue/.codex/skills/codelocator-ui-diagnose/scripts/diagnose_live.sh`
  - `/Users/yebingyue/.codex/skills/codelocator-ui-diagnose/scripts/diagnose_file.sh`
- 已完成：Skill 约束“优先 MCP，失败降级 CLI”。

### Phase 4 Web Viewer（MVP）
- 已完成：截图展示、节点树、搜索、选中高亮、属性面板、复制 memAddr。
- 已完成：按 grab_id 加载快照与切换。

### Phase 5 部署与使用闭环
- 已完成：两条流程在代码与 README 维度打通（手动抓取回放、LLM 实时抓取）。
- 已完成：`README` 给出命令入口和 MCP 配置示例。

## 3. 已验证结果（本地执行）

- 构建与测试：
  - 在 `CodeLocatorMCPAdapter` 执行 `./gradlew test installDist --no-daemon`，`BUILD SUCCESSFUL`。
- CLI 冒烟：
  - `grabs list --json`：成功返回历史抓取列表。
  - `grab file --json`：成功加载本地历史 `.codeLocator`，返回新 `grab_id`。
  - `viewer open --grab-id <id> --json`：成功返回本地 viewer URL。

## 4. 待完成与风险点

1. `~/.codex` 仓库存在未提交改动（包含新 skill）。按规则需执行 `push-skill-sync` 完成提交推送。
2. 需做一次完整端到端验收：
   - MCP 客户端真实调用 7 工具。
   - 真机 `grab live -> viewer -> inspect` 全链路稳定性验证。
3. Viewer 在不同运行上下文（前台/守护化）还需补充一轮稳定性回归。

## 5. 结论

从实施状态看，主体开发已完成，当前不是“方案设计阶段”，而是“**联调收尾 + 提交同步 + 最终验收**”阶段。
