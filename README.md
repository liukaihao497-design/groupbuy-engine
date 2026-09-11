# 拼团营销服务

面向电商拼团场景的 Java 后端项目，围绕活动试算、开团参团、订单锁定、支付结算和成团通知组织业务。采用 DDD 多模块结构，将营销规则与数据库、缓存、消息通知等基础设施分离。

## 核心功能

| 业务环节 | 当前实现 |
| --- | --- |
| 活动试算 | 根据活动和优惠配置计算营销价格，支持多种优惠计算策略 |
| 交易校验 | 通过责任链组织下单规则，包括用户参与次数与组队库存校验 |
| 开团与参团 | 区分首次开团和加入已有团队，通过 Redis 抢占组队名额 |
| 支付结算 | 校验外部交易单及订单状态，推进拼团结算流程 |
| 成团通知 | 提供通知执行逻辑及定时任务入口，包含 RabbitMQ 通知相关实现与测试 |
| 配置与日志 | 提供 DCC 配置更新入口、请求 TraceId 和监控部署配置 |

## 技术栈与模块

Java 8、Spring Boot 2.7.12、MyBatis、MySQL、Redis / Redisson、RabbitMQ、Maven。

| 模块后缀 | 职责 |
| --- | --- |
| `api` | 对外接口与请求响应对象 |
| `trigger` | HTTP 接口、定时任务等调用入口 |
| `domain` | 活动、交易、人群标签等领域逻辑 |
| `infrastructure` | 仓储实现、MyBatis 映射、缓存与外部通知 |
| `types` | 通用类型、异常、枚举与工具 |
| `app` | 应用启动、配置装配与测试 |

业务调用关系：HTTP 请求 → 规则校验 / 领域服务 → 仓储与基础设施 → 返回业务结果。

## 源码阅读入口

- [活动试算](my-group-buy-market-domain/src/main/java/com/lkh/domain/activity/service/trial)：查看优惠计算策略及试算流程。
- [锁单规则链](my-group-buy-market-domain/src/main/java/com/lkh/domain/trade/service/lock)：查看参与次数、组队库存和锁单逻辑。
- [交易结算](my-group-buy-market-domain/src/main/java/com/lkh/domain/trade/service/settlement)：查看结算校验与通知处理。
- [HTTP 接口](my-group-buy-market-trigger/src/main/java/com/lkh/trigger/http)：从接口向下追踪完整业务调用。
- [业务测试](my-group-buy-market-app/src/test/java/com/lkh/test)：包含活动试算、交易结算与通知链路等测试代码。

## 本地运行

1. 准备 JDK 8、Maven，以及 MySQL、Redis、RabbitMQ。
2. 查看 [数据库脚本](docs/dev-ops/mysql/sql)，按环境初始化业务表；消息通知相关变更见 `2-17-rabbitmq-notify.sql`。
3. 在应用模块 `src/main/resources` 下检查 `application.yml` 与对应 profile 配置，将连接地址改为本地服务地址，并提供配置中引用的环境变量。公开仓库不包含可用的密码和密钥。
4. 在项目根目录执行 `mvn -DskipTests package`，随后执行：

```bash
java -jar my-group-buy-market-app/target/my-group-buy-market-app.jar
```

构建时跳过测试仅用于打包；集成测试需要先准备外部服务及业务数据。本说明未给出生产压测或部署验收结论。

## 主要接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/v1/gbm/index/query_group_buy_market_config` | 查询营销配置与试算结果 |
| POST | `/api/v1/gbm/trade/lock_market_pay_order` | 锁定拼团订单 |
| POST | `/api/v1/gbm/trade/settlement_market_pay_order` | 支付结算 |

请求字段与返回结构以 `api` 模块和控制器为准。

## 项目来源

本项目基于小傅哥拼团营销教程及 DDD 脚手架进行学习实践与扩展，保留源码中的原作者署名。本文介绍当前仓库实现，不将教程基础实现等同于个人独立贡献。
