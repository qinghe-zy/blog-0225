// 注意：我帮你删除了原有的 import './assets/main.css'，以免它干扰我们新组件的漂亮样式
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 引入 Element Plus UI 框架及其样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus) // 让整个项目都能使用 Element Plus 组件

app.mount('#app')