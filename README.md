# Light Food 轻食点单平台

一个面向轻食餐饮场景的前后端分离点单系统，参考黑马苍穹外卖的分层架构实现，包含 **管理端 Web**、**用户端移动 H5** 和 **Spring Boot 后端**。

项目覆盖从菜单维护、用户点餐、购物车、地址管理到订单状态流转的完整链路，适合作为 Java Web / Vue 全栈课程作业。

---

## 1. 项目功能

### 管理端

- 管理员账号密码登录
- 管理员账号注册（演示用）
- 分类查询、新增、编辑、删除
- 菜品分页查询、新增、编辑、删除
- 菜品上架/下架
- 菜品图片模拟路径展示
- 订单列表、状态统计与筛选
- 订单详情查看
- 管理员接单、取消订单

### 用户端

- 手机号/账号 + 密码登录
- 用户账号注册（用户名 + 密码 + 确认密码）
- 浏览分类和上架菜品
- 查看菜品详情
- 人气菜品推荐
- 购物车增加、修改数量、删除、清空
- 收货地址新增、修改、删除、设默认
- 提交订单、模拟支付
- 取消待付款订单
- 确认完成已接单订单
- 查看自己的订单列表和详情

### 后端与基础设施

- JWT 登录鉴权
- 管理端 `ADMIN` / 用户端 `USER` 角色隔离
- BCrypt 密码加密
- 统一响应结构、统一异常处理
- MyBatis XML 持久层
- Redis 缓存分类和人气菜品（不可用时自动降级）
- 订单状态机与订单明细快照
- 数据库外键、唯一约束和 CHECK 约束

---

## 2. 技术栈

### 后端

| 技术 | 版本/说明 |
|---|---|
| JDK | 17 |
| Spring Boot | 3.3.5 |
| Maven | 多模块工程 |
| MyBatis | 3.0.3 + XML |
| MySQL | 8.0.16+ |
| Redis | Spring Data Redis，可选 |
| JWT | jjwt 0.12.6 |
| 密码加密 | BCrypt |
| 参数校验 | Jakarta Validation |

### 前端

| 端 | 技术 |
|---|---|
| 管理端 | Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router + Axios |
| 用户端 | Vue 3 + Vite 5 + Vant 4 + Pinia + Vue Router + Axios |
| 包管理 | npm workspaces |

---

## 3. 目录结构

```text
LightFood_Project_Submission/
├── backend/                       # Spring Boot 后端
│   ├── pom.xml                    # Maven 父工程
│   ├── light-food-common/         # 通用响应、异常、JWT、常量、上下文
│   ├── light-food-pojo/           # 实体、DTO、VO
│   └── light-food-server/         # Controller、Service、Mapper、配置、拦截器
│       └── src/main/resources/
│           ├── application.yml    # 数据库、Redis、JWT 配置
│           └── mapper/            # MyBatis XML
├── frontend/                      # 前端 npm workspaces
│   ├── package.json
│   ├── package-lock.json
│   ├── admin-web/                 # 管理端，Element Plus
│   └── user-h5/                   # 用户端，Vant 4
├── sql/
│   └── init.sql                   # 建库、建表、索引、约束、演示数据
├── .gitignore
└── README.md
```

后端分层：

```text
controller -> service -> mapper -> MySQL
                |
                └-> Redis / 本地降级缓存
```

---

## 4. 环境准备

| 软件 | 要求 |
|---|---|
| JDK | 17 |
| Maven | 3.9+ |
| MySQL | 8.0.16+ |
| Node.js | 20+ |
| npm | 10+ |
| Redis | 5+，可选 |

默认端口：

| 服务 | 端口 |
|---|---:|
| MySQL | 3306 |
| Redis | 6379 |
| 后端 | 8080 |
| 管理端 | 5173 |
| 用户端 | 5174 |

---

## 5. 创建数据库

### 5.1 执行初始化脚本

在项目根目录执行。

Windows PowerShell 不支持 `<` 重定向，使用：

```powershell
cmd /c "mysql -u root -p < sql\init.sql"
```

Git Bash / WSL / macOS：

```bash
mysql -u root -p < sql/init.sql
```

脚本会：

- 创建 `light_food` 数据库；
- 删除并重建业务表；
- 创建 8 张表、外键、唯一约束和 CHECK 约束；
- 写入演示管理员、用户、分类、菜品和默认地址。

> 脚本包含 `DROP TABLE`，请确认目标库没有需要保留的数据。

### 5.2 校验数据库

```powershell
mysql -u root -p -e "USE light_food; SHOW TABLES;"
```

应看到：

```text
admin
category
dish
order_detail
orders
shopping_cart
user
user_address
```

### 5.3 数据表说明

| 表 | 说明 |
|---|---|
| `admin` | 管理员账号 |
| `user` | 用户账号 |
| `category` | 菜品分类 |
| `dish` | 菜品 |
| `user_address` | 用户收货地址 |
| `shopping_cart` | 购物车 |
| `orders` | 订单主表 |
| `order_detail` | 订单明细快照 |

订单状态：

```text
0 待付款
1 待接单
2 已接单
3 已完成
4 已取消
```

---

## 6. 配置后端

配置文件：

```text
backend/light-food-server/src/main/resources/application.yml
```

推荐通过环境变量覆盖默认配置。

### PowerShell 示例

```powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/light_food?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = '你的MySQL密码'
$env:JWT_SECRET = '请替换为至少32字节的随机字符串'
$env:SERVER_PORT = '8080'
$env:REDIS_HOST = '127.0.0.1'
$env:REDIS_PORT = '6379'
```

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_URL` | 本机 `light_food` | JDBC 地址 |
| `DB_USERNAME` | `root` | 数据库用户 |
| `DB_PASSWORD` | 空 | 数据库密码 |
| `JWT_SECRET` | 开发默认值 | 生产环境必须替换 |
| `JWT_EXPIRE_HOURS` | `12` | Token 有效期 |
| `SERVER_PORT` | `8080` | 后端端口 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `CACHE_REDIS_ENABLED` | `true` | 是否优先使用 Redis |

### Redis 说明

有 Redis 时保持 `CACHE_REDIS_ENABLED=true`，分类和人气菜品会写入：

```text
lightfood:menu:categories:v1
lightfood:menu:hot-dishes:v1
```

没有 Redis 时可设置：

```powershell
$env:CACHE_REDIS_ENABLED = 'false'
```

系统会使用进程内降级缓存，功能仍可正常使用。

---

## 7. 启动后端

```powershell
cd backend
mvn clean package
java -jar light-food-server/target/light-food-server-1.0.0.jar
```

启动成功标志：

```text
Tomcat started on port 8080
Started LightFoodApplication
```

如果没有命令行 Maven，可在 IntelliJ IDEA 中打开 `backend/pom.xml`，使用 IDEA 的 Maven 面板执行 `package`。

### 验证后端

```powershell
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/health'
```

预期：

```text
status
------
ok
```

浏览器也可以直接打开：

```text
http://127.0.0.1:8080/api/health
http://127.0.0.1:8080/
```

两者都返回：

```json
{"status":"ok"}
```

---

## 8. 启动前端

### 8.1 安装依赖

```powershell
cd frontend
npm install
```

项目使用 npm workspaces，一次安装覆盖管理端和用户端。

### 8.2 启动管理端

```powershell
npm run dev:admin
```

访问：

```text
http://127.0.0.1:5173
```

### 8.3 启动用户端

另开一个终端：

```powershell
cd frontend
npm run dev:user
```

访问：

```text
http://127.0.0.1:5174
```

### 8.4 Vite 代理

两个前端都已配置代理：

```text
/admin -> http://127.0.0.1:8080
/user  -> http://127.0.0.1:8080
/api   -> http://127.0.0.1:8080
/mock  -> http://127.0.0.1:8080
```

所以浏览器只需要访问前端端口，不需要单独配置跨域。

### 8.5 生产构建

```powershell
cd frontend
npm run build
```

产物：

```text
frontend/admin-web/dist
frontend/user-h5/dist
```

---

## 9. 局域网/手机访问

两个 Vite 项目均已配置：

```js
server: {
  host: '0.0.0.0',
  port: 5173 // 用户端为 5174
}
```

查看电脑局域网 IPv4：

```powershell
ipconfig
```

在同一 Wi-Fi / 校园网下，手机访问：

```text
管理端：http://<电脑局域网IP>:5173
用户端：http://<电脑局域网IP>:5174
```

注意事项：

- 手机和电脑必须在同一可互访网络；
- 手机流量无法访问内网 IP；
- 校园网若开启 AP 隔离/客户端隔离，设备之间无法互访；
- Windows 防火墙需要允许 Node.js 或 TCP 5173/5174。

管理员 PowerShell 放行端口示例：

```powershell
New-NetFirewallRule -DisplayName "LightFood 5173" -Direction Inbound -Protocol TCP -LocalPort 5173 -Action Allow -Profile Any
New-NetFirewallRule -DisplayName "LightFood 5174" -Direction Inbound -Protocol TCP -LocalPort 5174 -Action Allow -Profile Any
```

---

## 10. 默认账号

| 端 | 账号 | 密码 |
|---|---|---|
| 管理端 | `admin` | `admin123` |
| 用户端 | `13800000000` | `admin123` |
| 用户端 | `testuser` | `admin123` |

用户端同时支持手机号和账号登录。

登录页和管理端登录页均提供注册入口；注册成功后自动登录。注册密码使用 BCrypt 哈希写入数据库，不保存明文。

---

## 11. 主要接口

### 公共接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/health` | 健康检查 |
| GET | `/` | 健康检查 |
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/admin/register` | 管理员注册 |
| POST | `/user/login` | 用户登录 |
| POST | `/admin/login` | 管理员登录 |

### 用户端接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/user/categories` | 分类列表 |
| GET | `/user/dishes?categoryId=` | 菜品列表 |
| GET | `/user/dishes/hot` | 人气菜品 |
| GET | `/user/dishes/{id}` | 菜品详情 |
| GET/POST/PUT/DELETE | `/user/cart/**` | 购物车 |
| GET/POST/PUT/DELETE | `/user/addresses/**` | 收货地址 |
| POST | `/user/orders` | 提交订单 |
| GET | `/user/orders` | 我的订单 |
| GET | `/user/orders/{id}` | 订单详情 |
| PUT | `/user/orders/{id}/pay` | 模拟支付 |
| PUT | `/user/orders/{id}/cancel` | 取消待付款订单 |
| PUT | `/user/orders/{id}/complete` | 确认完成 |

### 管理端接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT/DELETE | `/admin/categories/**` | 分类维护 |
| GET/POST/PUT/DELETE | `/admin/dishes/**` | 菜品维护 |
| PUT | `/admin/dishes/{id}/status` | 上下架 |
| GET | `/admin/orders` | 订单分页 |
| GET | `/admin/orders/{id}` | 订单详情 |
| PUT | `/admin/orders/{id}/accept` | 接单 |
| PUT | `/admin/orders/{id}/cancel` | 取消订单 |

统一成功响应：

```json
{
  "code": 1,
  "message": "success",
  "data": {}
}
```

业务失败：

```json
{
  "code": 0,
  "message": "具体错误原因",
  "data": null
}
```

---

## 12. 测试

### 后端单元测试

```powershell
cd backend
mvn test
```

### 前端构建检查

```powershell
cd frontend
npm run build
```

### 手工主流程

1. 管理端登录；
2. 新建分类和菜品；
3. 用户端注册或登录；
4. 浏览菜品、加入购物车；
5. 新增地址、提交订单、模拟支付；
6. 管理端接单；
7. 用户端确认完成；
8. 再下一笔待付款订单并取消；
9. 查看订单列表、详情和数据库状态。

---

## 13. 常见问题

| 现象 | 处理 |
|---|---|
| `Unknown database 'light_food'` | 未执行 `sql/init.sql` |
| `Access denied for user 'root'` | 检查 `DB_PASSWORD` |
| 后端启动报端口占用 | 修改 `SERVER_PORT` 和 Vite 代理 |
| 前端接口 404 | 确认后端 8080 已启动 |
| 登录返回 401 | Token 缺失或过期，重新登录 |
| Redis 连接告警 | 启动 Redis，或设置 `CACHE_REDIS_ENABLED=false` |
| 手机打不开前端 | 检查同网段、防火墙、校园网 AP 隔离 |
| 菜品图片显示占位图 | 当前使用模拟图片路径，属于设计范围 |

---

## 14. 说明

- 用户端是移动 H5，采用 Vant 4；管理端是 PC Web，采用 Element Plus，两者都是 Vue 3。
- 手机号登录为密码模拟登录，不发送短信验证码。
- 支付为模拟支付，不对接真实支付渠道。
- Redis 为可选依赖，不可用时自动降级。
- 生产部署前必须修改 JWT 密钥、数据库密码和默认管理员密码。

---

## 15. 提交说明

本目录只包含课程作业所需的源码：

- `frontend/`：两个 Vue 3 前端
- `backend/`：Spring Boot 多模块后端
- `sql/`：数据库初始化脚本
- `.gitignore`：忽略依赖、构建产物、IDE 和本地配置
- `README.md`：项目说明和启动指南

未包含 `node_modules`、`target`、`dist`、IDE 配置、运行日志和本地缓存。
