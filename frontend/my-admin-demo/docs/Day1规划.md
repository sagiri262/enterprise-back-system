这是一个为了构建**可上线（Production Ready）**的第一版（MVP）前端系统的七天规划。与演示 Demo 不同，这个规划更注重**代码规范、目录结构、环境配置和错误处理**，确保每一行代码都是为了最终部署而写。

---

### **📅 可上线版本：七天开发规划**

#### **第一天：工程化基建与视觉框架**
*   **目标**：搭建符合企业级标准的项目骨架，配置环境变量，完成登录页 UI。
*   **产出**：项目跑通，目录结构定型，能看到漂亮的登录页和主布局骨架。
*   **关键点**：Vite 配置、SCSS 全局变量、Element Plus 全局配置、登录页响应式布局。

#### **第二天：认证中心与网络层封装**
*   **目标**：打通“登录-存储Token-请求拦截-路由守卫”的闭环。
*   **产出**：能够真实登录，Token 自动持久化，Token 失效自动登出，Axios 统一错误处理。
*   **关键点**：`request.js` 拦截器（处理 401/403/500）、Pinia `user` store、NProgress 进度条。

#### **第三天：动态权限引擎 (核心)**
*   **目标**：实现基于后端数据的动态菜单渲染。
*   **产出**：不同账号登录看到不同菜单，刷新页面权限不丢失。
*   **关键点**：`permission.js` 路由守卫逻辑、递归侧边栏组件、动态路由 `addRoute` 实现。

#### **第四天：布局系统完善与交互**
*   **目标**：完成 Layout 的所有交互功能。
*   **产出**：侧边栏折叠/展开、面包屑导航、下拉菜单（登出/个人中心）、移动端适配。
*   **关键点**：Layout 状态管理、Transition 路由动画、图标组件统一管理。

#### **第五天：通用 CRUD 业务组件**
*   **目标**：封装高复用的表格搜索组件，拒绝重复造轮子。
*   **产出**：一个标准的用户管理页面，支持服务端分页、排序、搜索。
*   **关键点**：Hook 封装（`useTable`）、自定义列模板、Loading 状态精细化控制。

#### **第六天：表单业务与反馈机制**
*   **目标**：完成数据的增删改，并提供友好的用户反馈。
*   **产出**：新增/编辑弹窗，表单校验规则，删除二次确认，操作成功/失败的消息提示。
*   **关键点**：Dialog 封装、Form `validate`、`ElMessage` / `ElMessageBox` 的标准调用。

#### **第七天：生产环境优化与部署**
*   **目标**：代码这一块的收尾，以及部署配置。
*   **产出**：构建包（dist），Nginx 配置文件，404/500 错误页。
*   **关键点**：`.env.production` 配置、Gzip 压缩（可选）、Nginx 反向代理配置生成、Sourcemap 移除。

---

### **🚀 第一天工作内容：基建与视觉**

第一天我们将完成项目的“地基”。代码将严格按照《项目综述》中的目录结构进行组织。

#### **1. 项目初始化**

请在终端执行：

```bash
# 1. 创建项目 (不选 TS, 不选 ESlint 以简化演示，实际项目建议开启)
npm create vue@latest my-admin-v1
cd my-admin-v1

# 2. 安装生产依赖
npm install element-plus axios pinia vue-router path-browserify @element-plus/icons-vue normalize.css

# 3. 安装开发依赖
npm install -D sass @vitejs/plugin-vue vite
```

#### **2. 目录结构创建**
请手动创建或调整 `src` 目录，使其符合以下结构（这非常重要）：

```text
src/
├── api/             # 空文件夹，待用
├── assets/          # 静态资源
├── components/      # 全局组件
├── layouts/         # 布局文件
├── router/          # 路由配置
├── stores/          # 状态管理
├── styles/          # 全局样式
│   ├── index.scss   # 样式入口
│   └── variables.scss # 变量
├── utils/           # 工具库
├── views/           # 页面
│   ├── login/       # 登录页
│   ├── dashboard/   # 首页
│   └── error/       # 错误页
├── App.vue
└── main.js
```

#### **3. 核心配置文件**

**`vite.config.js`** (配置别名与代理)
```javascript
import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      open: true,
      proxy: {
        // 开发环境代理配置
        [env.VITE_API_BASE_URL]: {
          target: 'http://localhost:8080', // 后端接口地址
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${env.VITE_API_BASE_URL}`), '')
        }
      }
    }
  }
})
```

**`.env.development`** (项目根目录)
```properties
VITE_API_BASE_URL=/api
```

**`.env.production`** (项目根目录)
```properties
VITE_API_BASE_URL=/prod-api
```

#### **4. 全局样式系统**

**`src/styles/variables.scss`**
```scss
// 定义全局变量，方便后续换肤
$bg-color: #2d3a4b;
$dark-gray: #889aa4;
$light-gray: #eee;
$menu-text: #bfcbd9;
$menu-active-text: #409eff;
$menu-bg: #304156;
```

**`src/styles/index.scss`**
```scss
@import 'normalize.css/normalize.css'; // 标准化 CSS
@import './variables.scss';

body {
  height: 100%;
  -moz-osx-font-smoothing: grayscale;
  -webkit-font-smoothing: antialiased;
  font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif;
}

html {
  height: 100%;
  box-sizing: border-box;
}

#app {
  height: 100%;
}

*, *:before, *:after {
  box-sizing: inherit;
}

// 简单的过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.28s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
```

#### **5. 路由与入口配置**

**`src/router/index.js`**
```javascript
import { createRouter, createWebHistory } from 'vue-router'

// 静态路由表
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404.vue'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/layouts/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: 'Dashboard', icon: 'Dashboard' }
      }
    ]
  },
  // 404 页面必须放在最后 (后续动态路由添加时要注意顺序，这里先占位)
  { path: '/:pathMatch(.*)*', redirect: '/404', hidden: true }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

export default router
```

**`src/main.js`**
```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn' // 国际化
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import '@/styles/index.scss' // 全局样式

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn, // 设置中文
  size: 'default' // 全局尺寸
})

app.mount('#app')
```

**`src/App.vue`**
```vue
<template>
  <router-view />
</template>
```

#### **6. 页面开发 (Login & Layout 骨架)**

**`src/views/login/index.vue`** (高保真登录页)
```vue
<template>
  <div class="login-container">
    <el-form 
      ref="loginFormRef" 
      :model="loginForm" 
      :rules="loginRules" 
      class="login-form" 
      autocomplete="on" 
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">企业级后台管理系统</h3>
      </div>

      <el-form-item prop="username">
        <el-input 
          v-model="loginForm.username" 
          placeholder="用户名" 
          type="text" 
          tabindex="1" 
          autocomplete="on"
          prefix-icon="User"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input 
          v-model="loginForm.password" 
          type="password" 
          placeholder="密码" 
          tabindex="2" 
          autocomplete="on" 
          show-password
          prefix-icon="Lock"
          @keyup.enter="handleLogin"
        />
      </el-form-item>

      <el-button 
        :loading="loading" 
        type="primary" 
        style="width:100%; margin-bottom:30px;" 
        @click="handleLogin"
      >
        登 录
      </el-button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123'
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
}

const handleLogin = () => {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      // 模拟登录，明天接真实逻辑
      setTimeout(() => {
        loading.value = false
        router.push('/')
        ElMessage.success('欢迎回来')
      }, 1000)
    }
  })
}
</script>

<style lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;

.login-container {
  min-height: 100%;
  width: 100%;
  background-color: $bg;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;

  .login-form {
    position: relative;
    width: 520px;
    max-width: 100%;
    padding: 160px 35px 0;
    margin: 0 auto;
    overflow: hidden;
    background: #fff; // 为了更现代的感觉，这里可以用卡片式
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  }

  .title-container {
    position: relative;
    .title {
      font-size: 26px;
      color: $bg;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }
  }
}
</style>
```

**`src/layouts/index.vue`** (主布局骨架)
```vue
<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <el-scrollbar>
        <el-menu
          active-text-color="#409EFF"
          background-color="#304156"
          class="el-menu-vertical-demo"
          default-active="1"
          text-color="#bfcbd9"
        >
          <el-menu-item index="1">
            <el-icon><Menu /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="2-1">用户管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <div class="fixed-header">
        <div class="navbar">
          <div class="hamburger">
             <el-icon :size="20"><Fold /></el-icon>
          </div>
          <div class="right-menu">
            <el-dropdown>
              <span class="el-dropdown-link">
                Admin <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <section class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: $menu-bg;
  overflow: hidden;
  transition: width 0.28s;
  
  // 消除右侧边框
  :deep(.el-menu) {
    border-right: none;
  }
}

.main-container {
  flex: 1;
  min-height: 100%;
  transition: margin-left .28s;
  position: relative;
  background-color: #f0f2f5;
}

.fixed-header {
  position: sticky;
  top: 0;
  z-index: 9;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;

  .hamburger {
    cursor: pointer;
    display: flex;
    align-items: center;
  }
  
  .right-menu {
    cursor: pointer;
  }
}

.app-main {
  min-height: calc(100vh - 50px);
  padding: 20px;
  position: relative;
  overflow: hidden;
}
</style>
```

**`src/views/dashboard/index.vue`**
```vue
<template>
  <div class="dashboard-container">
    <el-card>
      <h2>Dashboard</h2>
      <p>欢迎进入系统。今日：第一天构建。</p>
    </el-card>
  </div>
</template>
```

**`src/views/error/404.vue`**
```vue
<template>
  <div style="text-align:center; padding-top: 50px;">
    <h1>404</h1>
    <p>Page Not Found</p>
  </div>
</template>
```

---

### **✅ 第一天验收标准**

1.  执行 `npm run dev` 没有任何红色报错。
2.  访问 `http://localhost:5173/login`，显示深色背景的登录框，UI 不丑陋。
3.  输入任意内容点击登录，按钮出现 Loading 状态，1秒后跳转。
4.  跳转后进入 Dashboard，看到左侧深色菜单栏，顶部白色导航栏，中间灰色内容区。
5.  检查浏览器控制台 (F12)，网络请求（虽然还没发真实请求）没有 404 资源错误。

完成这一步，你就拥有了一个**结构标准、样式规范**的可上线系统雏形，为第二天接入复杂的鉴权逻辑打下了坚实基础。