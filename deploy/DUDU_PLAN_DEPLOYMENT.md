# Dudu Plan 后端发布说明

## 1. 发布前备份

备份当前 JAR、Nginx 配置和 MySQL 数据库。新服务仍使用原端口 `9191`，不新增公网端口。

## 2. 创建聊天表

在现有业务数据库执行：

```bash
mysql -u <数据库用户> -p <数据库名> < deploy/dudu-plan-chat.sql
```

该迁移只新增 `dudu_chat_message`，不会修改旧网站表。

## 3. 账号与签名配置

账号密码和签名密钥直接配置在 `src/main/resources/application.yml`：

```yaml
dudu-plan:
  owner-password: <嘟嘟账号密码>
  observer-password: <肚肚账号密码>
  token-secret: <至少 32 字节的签名密钥>
```

修改 `token-secret` 会立即使所有现有 Access Token 和 Refresh Token 失效。

## 4. 构建

当前网络环境建议使用项目内的 Maven Central 配置：

```powershell
.\mvnw.cmd -s .mvn\settings-central.xml clean package
```

发布文件：

```text
target/XYJHStart-Web-0.0.1-SNAPSHOT.jar
```

## 5. Nginx

继续复用现有 `/api/` 反向代理，并确认 `/api/dudu-plan/realtime` 传递 Upgrade 头、关闭代理缓冲并保留长连接超时。外部地址为：

```text
https://kakaweb.ltd/api/dudu-plan
wss://kakaweb.ltd/api/dudu-plan/realtime
```

## 6. 发布后检查

先检查公开健康接口：

```bash
curl https://kakaweb.ltd/api/dudu-plan/health
```

然后分别验证嘟嘟、肚肚登录，确认聊天历史、未读、送达、已读、撤回和 WebSocket 双端在线同步。后端验证完成后再发布新 APK；在新 APK 双机验收通过前，不删除旧 Cloudflare 或 Firebase 资源。
