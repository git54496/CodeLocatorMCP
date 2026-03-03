# AGENTS Rules

## Basic Rule

- 收到用户任何纠正后，立即新增或更新防错规则，防止同类错误再次发生。

## Build/Run Order Rule

- 涉及前端资源打包与运行验证时，必须先完成构建，再启动服务；禁止并行执行 build 与 run，避免加载旧产物。
