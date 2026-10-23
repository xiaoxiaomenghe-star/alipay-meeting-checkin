# 部署说明

1. 准备Java 21、Maven 3.8+，并启动Docker Desktop Linux引擎。
2. 在项目根目录执行 `./scripts/start-local.ps1`。
3. 首次运行会依据 `.env.example` 生成被Git忽略的 `.env` 和随机本地密钥。
4. 脚本构建并启动五个服务，等待健康检查后自动执行端到端业务验证。
5. 通过Nginx入口访问 `http://localhost:8088`。
6. RabbitMQ管理页为 `http://localhost:15673`。
7. 使用 `./scripts/stop-local.ps1` 停止容器，默认保留MySQL演示数据卷。

`.env.example`中的值仅为占位符；本地生成的 `.env` 不得提交。该流程用于作品演示，不代表生产部署方案。
