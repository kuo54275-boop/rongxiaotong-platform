import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

app.use(createPinia())                            // 状态管理（保存登录信息等）
app.use(router)                                   // 页面路由
app.use(ElementPlus, { locale: zhCn })            // Element Plus 组件库（中文）
app.mount('#app')
