# 融小通平台 - 前端

Vue 3 + Vite + Element Plus 前端项目。

## 启动

```bash
npm install     # 首次安装依赖
npm run dev     # 启动开发服务器，访问 http://localhost:5173
```

- 所有 `/api` 请求会通过 Vite 代理转发到后端（http://localhost:8080）
- 需先启动后端（`../backend` 目录执行 `mvn spring-boot:run`）

## 目录说明

| 目录 | 内容 |
|------|------|
| `src/api/` | axios 封装 + 所有接口定义 |
| `src/router/` | 路由表 + 登录守卫 |
| `src/stores/` | Pinia 状态（登录信息） |
| `src/layouts/` | 前台布局、银行后台布局 |
| `src/views/` | 各模块页面（home / finance / expert / market / user / bank） |

更多信息见项目根目录 [README](../README.md)。
