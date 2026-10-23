# 接口说明

除登录和健康检查外，请求均携带 `Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/v1/auth/login` | 开发授权码登录 |
| GET | `/api/v1/meetings` | 会议列表 |
| GET | `/api/v1/meetings/{id}` | 会议、点位及入口令牌 |
| POST | `/api/v1/meetings/{id}/registrations` | 报名 |
| GET | `/api/v1/meetings/{id}/registration` | 报名状态 |
| POST | `/api/v1/check-ins` | 二维码或NFC令牌签到 |
| GET | `/api/v1/me/check-ins` | 我的签到记录 |
| GET | `/api/v1/me/points` | 积分余额 |
| GET | `/api/v1/me/points/ledger` | 积分流水 |

响应统一包含 `code`、`message`、`traceId`、`data`。
