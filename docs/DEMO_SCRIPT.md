# 两分钟演示脚本

本脚本用于录制本地作品演示，不代表生产部署或真实支付宝开放平台联调。

## 录制前准备

1. 在项目根目录执行 `./scripts/start-local.ps1`。
2. 确认脚本输出 `result=PASS`，并保持Docker服务运行。
3. 准备终端、浏览器API响应和项目README三个画面。

## 推荐镜头

1. 展示README中的业务闭环和架构图。
2. 执行 `docker compose ps`，展示MySQL、Redis、RabbitMQ、Java服务和Nginx健康状态。
3. 运行 `./scripts/verify-compose.ps1`，展示登录、报名、签到、积分到账、记录查询和重复签到拦截结果。
4. 展示 `mvn test` 的测试汇总。
5. 简要打开 `CheckinService`、`OutboxPublisher` 和 `RedisGuards`，说明数据库唯一约束、Outbox、消费者幂等和Redis Lua限流。
6. 最后说明边界：尚未完成真实支付宝授权、真机NFC和生产压测。
