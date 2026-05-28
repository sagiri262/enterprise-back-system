<script setup>
import { computed, reactive, ref } from 'vue'

const apiBase = import.meta.env.VITE_API_BASE_URL || ''
const token = ref(localStorage.getItem('admin_token') || '')
const currentUser = ref(localStorage.getItem('admin_user') || '')
const activePage = ref(token.value ? 'home' : 'login')
const loading = ref(false)
const message = ref('')
const dataMode = ref(localStorage.getItem('admin_data_mode') || 'api')

const loginForm = reactive({ username: 'admin', password: '123456a' })
const userForm = reactive({ username: '', password: '', nickname: '', email: '', status: 1 })
const roleForm = reactive({ name: '', code: '', description: '' })
const permissionForm = reactive({ parentId: 0, name: '', code: '', type: 1, path: '' })
const authzForm = reactive({ roleId: 1, permissionIds: '1,2,3', userId: 1, roleIds: '1' })

const userList = ref([])
const roleList = ref([])
const permissionList = ref([])

const fallbackUsers = [
  { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@example.com', status: 1 },
  { id: 2, username: 'hr_lead', nickname: '人力负责人', email: 'hr@example.com', status: 1 },
  { id: 3, username: 'finance_lead', nickname: '会计负责人', email: 'finance@example.com', status: 1 },
]

const fallbackRoles = [
  { id: 1, name: '超级管理员', code: 'admin', description: '拥有系统全部权限' },
  { id: 2, name: '部门负责人', code: 'manager', description: '管理部门员工权限和流程' },
  { id: 3, name: '基层员工', code: 'employee', description: '可访问个人工作台和基础 OA' },
]

const fallbackPermissions = [
  { id: 1, name: '用户管理', code: 'user:list', type: 1, path: '/users' },
  { id: 2, name: '新增用户', code: 'user:create', type: 2, path: '' },
  { id: 3, name: '分配角色', code: 'user:assign-role', type: 2, path: '' },
  { id: 4, name: '角色管理', code: 'role:list', type: 1, path: '/roles' },
  { id: 5, name: '分配权限', code: 'role:assign-permission', type: 2, path: '' },
  { id: 6, name: '权限管理', code: 'permission:list', type: 1, path: '/permissions' },
]

userList.value = [...fallbackUsers]
roleList.value = [...fallbackRoles]
permissionList.value = [...fallbackPermissions]

const navItems = [
  { key: 'home', label: '首页' },
  { key: 'org', label: '组织架构' },
  { key: 'workbench', label: '工作台' },
  { key: 'security', label: '安全中心' },
  { key: 'finance', label: '费用中心' },
  { key: 'stats', label: '数据统计' },
]

const rightLinks = ref([
  { title: '公司组织架构介绍', desc: '查看部门关系、负责人和联系权限' },
  { title: '消息中心', desc: '审批提醒、系统通知、待办状态' },
  { title: '公司OA系统', desc: '出差、报销、入离职、调岗流程入口' },
])

const orgGroups = ref([
  {
    name: '管理层',
    people: [
      { name: '老板', role: '决策审批', contact: '仅管理层可联系', locked: true },
      { name: '带班经理', role: '排班协调', contact: '部门负责人可联系', locked: true },
      { name: '部门经理', role: '跨部门审批', contact: '部门负责人可联系', locked: true },
    ],
  },
  {
    name: '部门负责人',
    people: [
      { name: '人力负责人', role: '入职、离职、升迁', contact: '员工可联系', locked: false },
      { name: '会计负责人', role: '报销、薪水发放', contact: '员工可联系', locked: false },
      { name: '工作部门负责人', role: '项目调度', contact: '员工可联系', locked: false },
    ],
  },
  {
    name: '基层员工',
    people: [
      { name: '业务员工', role: '项目执行', contact: '联系部门负责人', locked: false },
      { name: '实习员工', role: '辅助执行', contact: '联系直属负责人', locked: false },
    ],
  },
])

const workApps = ref([
  { title: '项目审批', status: '8 个待处理', owner: '工作部门负责人', tone: 'blue' },
  { title: '人员出差登记', status: '2 个进行中', owner: '人力负责人', tone: 'green' },
  { title: '新入职登记', status: '3 个草稿', owner: '人力负责人', tone: 'amber' },
  { title: '部门人事调动', status: '1 个待确认', owner: '部门经理', tone: 'red' },
  { title: '离职登记', status: '0 个逾期', owner: '人力负责人', tone: 'green' },
  { title: '升迁登记', status: '4 个待评审', owner: '管理层', tone: 'blue' },
])

const financeItems = ref([
  { label: '差旅报销', value: '¥ 12,680', trend: '+8.4%' },
  { label: '办公采购', value: '¥ 4,320', trend: '-2.1%' },
  { label: '薪水发放', value: '本月已归档', trend: '100%' },
])

const stats = ref([
  { label: '在线员工', value: '42' },
  { label: '本月审批', value: '128' },
  { label: '权限变更', value: '16' },
  { label: '审计记录', value: '持续写入' },
])

const notices = ref([
  { title: '权限变更审批', desc: '部门负责人可以为基层员工申请临时访问权限。', level: '安全' },
  { title: '费用报销提醒', desc: '本周五前提交差旅票据，逾期进入下月周期。', level: '费用' },
  { title: 'OA 流程同步', desc: '入职登记、离职登记、调岗流程已纳入工作台。', level: 'OA' },
])

const contactRules = ref([
  { role: '基层员工', scope: '可联系部门负责人', lock: '管理层灰锁' },
  { role: '部门负责人', scope: '可联系管理层并管理下属权限', lock: '绿锁' },
  { role: '管理层', scope: '全局审批与授权', lock: '无锁' },
])

const financeFlows = ref([
  { title: '差旅费报销', desc: '员工提交票据，部门负责人初审，会计负责人复核。', steps: '3 步' },
  { title: '薪水发放', desc: '人力确认考勤，会计生成发放批次，管理层最终审批。', steps: '4 步' },
])

const isLoggedIn = computed(() => Boolean(token.value))
const activeTitle = computed(() => navItems.find((item) => item.key === activePage.value)?.label || '首页')

const authHeaders = () => {
  const headers = { 'Content-Type': 'application/json' }
  if (token.value) {
    headers.Authorization = token.value.startsWith('Bearer ') ? token.value : `Bearer ${token.value}`
  }
  return headers
}

const request = async (url, options = {}) => {
  loading.value = true
  message.value = ''

  try {
    const resp = await fetch(`${apiBase}${url}`, options)
    const json = await resp.json().catch(() => null)

    if (!resp.ok) {
      throw new Error(json?.message || makeHttpError(resp.status))
    }

    if (json?.code !== 0) {
      throw new Error(json?.message || '请求失败')
    }

    return json
  } finally {
    loading.value = false
  }
}

const makeHttpError = (status) => {
  if (status === 500) {
    return '后端接口返回 500。若后端未启动，请先启动 8080 服务；若已启动，请查看后端控制台异常。'
  }
  if (status === 401) return '未登录或登录已过期'
  if (status === 403) return '当前账号无权限访问该功能'
  return `请求失败：${status}`
}

const enterDemoMode = (reason = '已进入本地演示模式') => {
  token.value = 'demo-token'
  currentUser.value = loginForm.username || 'admin'
  dataMode.value = 'demo'
  localStorage.setItem('admin_token', token.value)
  localStorage.setItem('admin_user', currentUser.value)
  localStorage.setItem('admin_data_mode', dataMode.value)
  activePage.value = 'home'
  message.value = reason
}

const loadPortalOverview = async () => {
  if (dataMode.value === 'demo') return

  try {
    const json = await request('/api/portal/overview', { headers: authHeaders() })
    const data = json.data || {}
    stats.value = data.metrics || stats.value
    notices.value = data.notices || notices.value
    contactRules.value = data.contactRules || contactRules.value
    orgGroups.value = data.orgGroups || orgGroups.value
    workApps.value = data.workApps || workApps.value
    financeItems.value = data.financeItems || financeItems.value
    financeFlows.value = data.financeFlows || financeFlows.value
    rightLinks.value = data.rightLinks || rightLinks.value
    message.value = '后台门户数据已同步'
  } catch (error) {
    message.value = `门户数据加载失败：${error.message}`
  }
}

const login = async () => {
  try {
    const json = await request('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginForm),
    })

    token.value = json.data.token
    currentUser.value = json.data.username
    localStorage.setItem('admin_token', token.value)
    localStorage.setItem('admin_user', currentUser.value)
    localStorage.setItem('admin_data_mode', dataMode.value)
    activePage.value = 'home'
    message.value = `登录成功：${currentUser.value}`
    await loadPortalOverview()
  } catch (error) {
    if (import.meta.env.DEV) {
      enterDemoMode(`后端暂不可用，已进入本地演示模式：${error.message}`)
      return
    }
    message.value = `登录失败：${error.message}`
  }
}

const logout = () => {
  token.value = ''
  currentUser.value = ''
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_user')
  localStorage.removeItem('admin_data_mode')
  activePage.value = 'login'
  message.value = '已退出登录'
}

const switchPage = (key) => {
  activePage.value = key
}

const loadUsers = async () => {
  if (dataMode.value === 'demo') {
    userList.value = [...fallbackUsers]
    message.value = `本地演示用户已加载，共 ${userList.value.length} 条`
    return
  }

  try {
    const json = await request('/api/users?page=1&size=10', { headers: authHeaders() })
    userList.value = json.data.records || []
    message.value = `用户加载成功，共 ${userList.value.length} 条`
  } catch (error) {
    message.value = `用户加载失败：${error.message}`
  }
}

const saveUser = async () => {
  if (dataMode.value === 'demo') {
    const nextId = Math.max(...userList.value.map((item) => item.id), 0) + 1
    userList.value = [
      ...userList.value,
      { id: nextId, username: userForm.username, nickname: userForm.nickname, email: userForm.email, status: userForm.status },
    ]
    message.value = '本地演示用户已创建'
    return
  }

  try {
    await request('/api/users', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(userForm),
    })
    message.value = '用户创建成功'
    await loadUsers()
  } catch (error) {
    message.value = `用户创建失败：${error.message}`
  }
}

const loadRoles = async () => {
  if (dataMode.value === 'demo') {
    roleList.value = [...fallbackRoles]
    message.value = `本地演示角色已加载，共 ${roleList.value.length} 条`
    return
  }

  try {
    const json = await request('/api/roles?page=1&size=10', { headers: authHeaders() })
    roleList.value = json.data.records || []
    message.value = `角色加载成功，共 ${roleList.value.length} 条`
  } catch (error) {
    message.value = `角色加载失败：${error.message}`
  }
}

const saveRole = async () => {
  if (dataMode.value === 'demo') {
    const nextId = Math.max(...roleList.value.map((item) => item.id), 0) + 1
    roleList.value = [...roleList.value, { id: nextId, ...roleForm }]
    message.value = '本地演示角色已创建'
    return
  }

  try {
    await request('/api/roles', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(roleForm),
    })
    message.value = '角色创建成功'
    await loadRoles()
  } catch (error) {
    message.value = `角色创建失败：${error.message}`
  }
}

const loadPermissions = async () => {
  if (dataMode.value === 'demo') {
    permissionList.value = [...fallbackPermissions]
    message.value = `本地演示权限已加载，共 ${permissionList.value.length} 条`
    return
  }

  try {
    const json = await request('/api/permissions?page=1&size=10', { headers: authHeaders() })
    permissionList.value = json.data.records || []
    message.value = `权限加载成功，共 ${permissionList.value.length} 条`
  } catch (error) {
    message.value = `权限加载失败：${error.message}`
  }
}

const savePermission = async () => {
  if (dataMode.value === 'demo') {
    const nextId = Math.max(...permissionList.value.map((item) => item.id), 0) + 1
    permissionList.value = [...permissionList.value, { id: nextId, ...permissionForm }]
    message.value = '本地演示权限已创建'
    return
  }

  try {
    await request('/api/permissions', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(permissionForm),
    })
    message.value = '权限创建成功'
    await loadPermissions()
  } catch (error) {
    message.value = `权限创建失败：${error.message}`
  }
}

const parseIds = (value) => value
  .split(',')
  .map((item) => Number(item.trim()))
  .filter((item) => Number.isFinite(item) && item > 0)

const batchGrant = async () => {
  if (dataMode.value === 'demo') {
    message.value = `本地演示授权成功：用户 ${authzForm.userId} -> 角色 [${authzForm.roleIds}]，角色 ${authzForm.roleId} -> 权限 [${authzForm.permissionIds}]`
    return
  }

  try {
    await request(`/api/auth/roles/${Number(authzForm.roleId)}/permissions`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ permissionIds: parseIds(authzForm.permissionIds) }),
    })

    await request(`/api/auth/users/${Number(authzForm.userId)}/roles`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ roleIds: parseIds(authzForm.roleIds) }),
    })

    message.value = '批量授权成功'
  } catch (error) {
    message.value = `批量授权失败：${error.message}`
  }
}

const openLegal = (type) => {
  const titles = {
    agreement: '用户协议',
    privacy: '隐私政策',
    terms: '服务条款',
  }
  const win = window.open('', '_blank')
  if (!win) return

  win.document.write(`
    <title>${titles[type]}</title>
    <main style="max-width:720px;margin:48px auto;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,sans-serif;line-height:1.8;color:#1f2937">
      <h1>${titles[type]}</h1>
      <p>这是企业后台管理系统开发测试版本的${titles[type]}页面，用于展示登录页底部独立标签页跳转能力。</p>
      <p>系统会在登录、权限变更和关键操作时记录审计日志。正式环境应接入 HTTPS、完善隐私条款并进行安全评审。</p>
    </main>
  `)
  win.document.close()
}
</script>

<template>
  <main v-if="!isLoggedIn" class="login-screen">
    <section class="login-panel">
      <div class="brand-block">
        <p class="eyebrow">TEST</p>
        <h1>企业后台管理系统</h1>
        <p>统一管理组织、工作流、权限、费用与统计数据。</p>
      </div>

      <form class="login-form" @submit.prevent="login">
        <label>
          账户
          <input v-model="loginForm.username" autocomplete="username" placeholder="请输入账户" />
        </label>
        <label>
          密码
          <input v-model="loginForm.password" autocomplete="current-password" type="password" placeholder="请输入密码" />
        </label>
        <button type="submit" :disabled="loading">{{ loading ? '登陆中...' : '登陆' }}</button>
        <p v-if="message" class="form-message">{{ message }}</p>
      </form>
    </section>

    <footer class="login-footer">
      <span>© 2026 开发测试 co-author: Codex 版权所有</span>
      <button type="button" @click="openLegal('agreement')">用户协议</button>
      <button type="button" @click="openLegal('privacy')">隐私政策</button>
      <button type="button" @click="openLegal('terms')">服务条款</button>
    </footer>
  </main>

  <main v-else class="admin-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-mark">E</span>
        <div>
          <strong>Enterprise</strong>
          <small>Back System</small>
        </div>
      </div>

      <nav class="nav-list" aria-label="主菜单">
        <button
          v-for="item in navItems"
          :key="item.key"
          type="button"
          :class="{ active: activePage === item.key }"
          @click="switchPage(item.key)"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">当前页面</p>
          <h1>{{ activeTitle }}</h1>
        </div>
        <div class="user-box">
          <span>{{ loading ? '请求中...' : message || '系统就绪' }}</span>
          <em class="mode-pill">{{ dataMode === 'demo' ? '演示模式' : '接口模式' }}</em>
          <strong>{{ currentUser }}</strong>
          <button type="button" @click="logout">退出</button>
        </div>
      </header>

      <div class="content-layout">
        <section class="main-content">
          <section v-if="activePage === 'home'" class="view-stack">
            <div class="hero-band">
              <div>
                <p class="eyebrow">企业后端管理功能</p>
                <h2>从权限到流程的最小管理台</h2>
                <p>首页汇总在线人员、审批、权限变更和审计写入情况，后续可以继续接入微服务监控、告警和在线文档统计。</p>
              </div>
              <button type="button" @click="activePage = 'security'">进入安全中心</button>
            </div>

            <div class="metrics-grid">
              <article v-for="item in stats" :key="item.label" class="metric-card">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </article>
            </div>

            <div class="split-grid">
              <article class="data-panel">
                <h3>消息中心</h3>
                <ul class="people-list">
                  <li v-for="notice in notices" :key="notice.title">
                    <div>
                      <strong>{{ notice.title }}</strong>
                      <span>{{ notice.desc }}</span>
                    </div>
                    <em>{{ notice.level }}</em>
                  </li>
                </ul>
              </article>

              <article class="data-panel">
                <h3>权限锁状态</h3>
                <ul class="people-list">
                  <li v-for="rule in contactRules" :key="rule.role">
                    <div>
                      <strong>{{ rule.role }}</strong>
                      <span>{{ rule.scope }}</span>
                    </div>
                    <em>{{ rule.lock }}</em>
                  </li>
                </ul>
              </article>
            </div>

            <div class="calendar-strip">
              <img src="https://p1-hera.feishucdn.com/tos-cn-i-jbbdkfciu3/507c8d1b715d4612856c4639bed856e4~tplv-jbbdkfciu3-image:0:0.image" alt="团队协同日历" />
              <div>
                <h3>团队协同日历</h3>
                <p>用于项目审批、出差登记、入职流程和跨部门协作提醒。</p>
              </div>
            </div>
          </section>

          <section v-if="activePage === 'org'" class="view-stack">
            <div class="section-head">
              <h2>组织架构</h2>
              <p>基层员工可联系部门负责人；管理层联系方式需要由负责人临时授权。</p>
            </div>
            <div class="org-grid">
              <article v-for="group in orgGroups" :key="group.name" class="data-panel">
                <h3>{{ group.name }}</h3>
                <ul class="people-list">
                  <li v-for="person in group.people" :key="person.name">
                    <div>
                      <strong>{{ person.name }}</strong>
                      <span>{{ person.role }}</span>
                    </div>
                    <em :class="{ locked: person.locked }">{{ person.locked ? '权限受限' : person.contact }}</em>
                  </li>
                </ul>
              </article>
            </div>

            <div class="data-panel">
              <h3>联系权限规则</h3>
              <ul class="people-list">
                <li v-for="rule in contactRules" :key="rule.role">
                  <div>
                    <strong>{{ rule.role }}</strong>
                    <span>{{ rule.scope }}</span>
                  </div>
                  <em>{{ rule.lock }}</em>
                </li>
              </ul>
            </div>
          </section>

          <section v-if="activePage === 'workbench'" class="view-stack">
            <div class="section-head">
              <h2>工作台</h2>
              <p>最小版本先呈现常用 OA 应用入口。</p>
            </div>
            <div class="app-grid">
              <article v-for="app in workApps" :key="app.title" class="app-tile" :data-tone="app.tone">
                <strong>{{ app.title }}</strong>
                <span>{{ app.status }}</span>
                <em>{{ app.owner }}</em>
              </article>
            </div>
          </section>

          <section v-if="activePage === 'security'" class="view-stack">
            <div class="section-head">
              <h2>安全中心</h2>
              <p>管理账号、角色、权限与批量授权。</p>
            </div>

            <div class="form-panel">
              <h3>用户管理</h3>
              <div class="form-grid five-cols">
                <input v-model="userForm.username" placeholder="用户名" />
                <input v-model="userForm.password" type="password" placeholder="密码" />
                <input v-model="userForm.nickname" placeholder="昵称" />
                <input v-model="userForm.email" placeholder="邮箱" />
                <select v-model.number="userForm.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
              <div class="toolbar">
                <button type="button" @click="saveUser">创建用户</button>
                <button type="button" class="secondary" @click="loadUsers">刷新用户</button>
              </div>
              <ul class="compact-list">
                <li v-for="item in userList" :key="item.id">{{ item.id }} - {{ item.username }} - {{ item.nickname || '未命名' }}</li>
              </ul>
            </div>

            <div class="form-panel">
              <h3>角色管理</h3>
              <div class="form-grid four-cols">
                <input v-model="roleForm.name" placeholder="角色名" />
                <input v-model="roleForm.code" placeholder="角色编码" />
                <input v-model="roleForm.description" placeholder="描述" />
                <button type="button" @click="saveRole">创建角色</button>
              </div>
              <button type="button" class="secondary" @click="loadRoles">刷新角色</button>
              <ul class="compact-list">
                <li v-for="item in roleList" :key="item.id">{{ item.id }} - {{ item.name }} ({{ item.code }})</li>
              </ul>
            </div>

            <div class="form-panel">
              <h3>权限管理</h3>
              <div class="form-grid five-cols">
                <input v-model.number="permissionForm.parentId" type="number" placeholder="parentId" />
                <input v-model="permissionForm.name" placeholder="权限名" />
                <input v-model="permissionForm.code" placeholder="权限编码" />
                <select v-model.number="permissionForm.type">
                  <option :value="1">菜单</option>
                  <option :value="2">按钮</option>
                </select>
                <input v-model="permissionForm.path" placeholder="path" />
              </div>
              <div class="toolbar">
                <button type="button" @click="savePermission">创建权限</button>
                <button type="button" class="secondary" @click="loadPermissions">刷新权限</button>
              </div>
              <ul class="compact-list">
                <li v-for="item in permissionList" :key="item.id">{{ item.id }} - {{ item.name }} ({{ item.code }})</li>
              </ul>
            </div>

            <div class="form-panel">
              <h3>批量授权</h3>
              <div class="form-grid four-cols">
                <input v-model.number="authzForm.roleId" type="number" placeholder="角色ID" />
                <input v-model="authzForm.permissionIds" placeholder="权限ID列表：1,2,3" />
                <input v-model.number="authzForm.userId" type="number" placeholder="用户ID" />
                <input v-model="authzForm.roleIds" placeholder="角色ID列表：1,2" />
              </div>
              <button type="button" @click="batchGrant">提交批量授权</button>
            </div>
          </section>

          <section v-if="activePage === 'finance'" class="view-stack">
            <div class="section-head">
              <h2>费用中心</h2>
              <p>覆盖费用报销、采购支出和薪水发放摘要。</p>
            </div>
            <div class="metrics-grid">
              <article v-for="item in financeItems" :key="item.label" class="metric-card">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <em>{{ item.trend }}</em>
              </article>
            </div>
            <div class="data-panel">
              <h3>费用流程</h3>
              <ul class="people-list">
                <li v-for="flow in financeFlows" :key="flow.title">
                  <div>
                    <strong>{{ flow.title }}</strong>
                    <span>{{ flow.desc }}</span>
                  </div>
                  <em>{{ flow.steps }}</em>
                </li>
              </ul>
            </div>
          </section>

          <section v-if="activePage === 'stats'" class="view-stack">
            <div class="section-head">
              <h2>数据统计</h2>
              <p>最小版本展示公开统计卡片，后续可接入腾讯文档或在线表格数据源。</p>
            </div>
            <div class="chart-panel">
              <div v-for="item in stats" :key="item.label" class="bar-row">
                <span>{{ item.label }}</span>
                <div><i :style="{ width: item.label.length * 12 + 42 + 'px' }"></i></div>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </section>
        </section>

        <aside class="right-rail">
          <article v-for="link in rightLinks" :key="link.title" class="rail-card">
            <strong>{{ link.title }}</strong>
            <span>{{ link.desc }}</span>
          </article>
          <article class="rail-card">
            <strong>当前连接</strong>
            <span>{{ dataMode === 'demo' ? '后端不可用时使用本地演示数据' : '已连接后端 API' }}</span>
          </article>
        </aside>
      </div>
    </section>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

button,
input,
select {
  font: inherit;
}

button {
  border: 0;
  cursor: pointer;
}

.login-screen {
  min-height: 100vh;
  display: grid;
  grid-template-rows: 1fr auto;
  background: #f3f7f4;
  color: #17221c;
}

.login-panel {
  width: min(880px, calc(100vw - 32px));
  margin: auto;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 32px;
  align-items: center;
}

.brand-block {
  padding: 32px 0;
}

.eyebrow {
  margin: 0 0 10px;
  color: #4f6f5a;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.brand-block h1,
.topbar h1,
.hero-band h2,
.section-head h2 {
  margin: 0;
  letter-spacing: 0;
}

.brand-block h1 {
  font-size: 42px;
  line-height: 1.12;
}

.brand-block p:last-child {
  max-width: 440px;
  margin: 18px 0 0;
  color: #53635a;
  line-height: 1.7;
}

.login-form {
  display: grid;
  gap: 16px;
  padding: 28px;
  border: 1px solid #d9e3dc;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 18px 42px rgba(32, 65, 45, 0.12);
}

.login-form label {
  display: grid;
  gap: 8px;
  color: #33443a;
  font-weight: 600;
}

.login-form input,
.form-grid input,
.form-grid select {
  width: 100%;
  min-width: 0;
  height: 40px;
  border: 1px solid #cbd8d0;
  border-radius: 6px;
  padding: 0 12px;
  background: #fff;
  color: #17221c;
}

.login-form button,
.hero-band button,
.toolbar button,
.form-panel > button,
.form-grid button,
.user-box button {
  min-height: 40px;
  border-radius: 6px;
  padding: 0 14px;
  background: #1f6f43;
  color: #fff;
  font-weight: 700;
}

.login-form button:disabled {
  opacity: 0.65;
  cursor: wait;
}

.form-message {
  margin: 0;
  color: #7b341e;
  line-height: 1.5;
}

.login-footer {
  min-height: 56px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: center;
  padding: 12px 20px;
  color: #637268;
  font-size: 13px;
}

.login-footer button {
  background: transparent;
  color: #1f6f43;
  padding: 0;
}

.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 224px minmax(0, 1fr);
  background: #eef3f0;
  color: #17221c;
}

.sidebar {
  background: #12231a;
  color: #f4fbf6;
  padding: 20px 14px;
}

.sidebar-brand {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 8px 22px;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  background: #8fd19e;
  color: #102116;
  font-weight: 800;
}

.sidebar-brand small {
  display: block;
  margin-top: 2px;
  color: #b7c8bd;
}

.nav-list {
  display: grid;
  gap: 6px;
}

.nav-list button {
  width: 100%;
  min-height: 40px;
  border-radius: 6px;
  padding: 0 12px;
  background: transparent;
  color: #dce8df;
  text-align: left;
}

.nav-list button.active,
.nav-list button:hover {
  background: #244833;
  color: #ffffff;
}

.workspace {
  min-width: 0;
  display: grid;
  grid-template-rows: auto 1fr;
}

.topbar {
  min-height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 24px;
  border-bottom: 1px solid #d7e1db;
  background: #ffffff;
}

.topbar h1 {
  font-size: 24px;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #58685e;
  font-size: 14px;
}

.user-box strong {
  color: #17221c;
}

.user-box em,
.mode-pill {
  font-style: normal;
}

.mode-pill {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  padding: 0 10px;
  background: #e4ece7;
  color: #1f5c3a;
  font-size: 12px;
  font-weight: 700;
}

.user-box button {
  background: #293c32;
}

.content-layout {
  min-width: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  padding: 18px;
}

.main-content {
  min-width: 0;
}

.view-stack {
  display: grid;
  gap: 16px;
}

.hero-band,
.data-panel,
.form-panel,
.rail-card,
.chart-panel,
.calendar-strip {
  border: 1px solid #d8e2dc;
  border-radius: 8px;
  background: #ffffff;
}

.hero-band {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  padding: 22px;
}

.hero-band h2 {
  font-size: 28px;
}

.hero-band p:last-child,
.section-head p {
  max-width: 720px;
  margin: 10px 0 0;
  color: #607167;
  line-height: 1.7;
}

.section-head {
  padding: 4px 0;
}

.metrics-grid,
.split-grid,
.org-grid,
.app-grid {
  display: grid;
  gap: 14px;
}

.split-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metrics-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric-card {
  min-height: 104px;
  display: grid;
  align-content: center;
  gap: 8px;
  padding: 18px;
  border: 1px solid #d8e2dc;
  border-radius: 8px;
  background: #ffffff;
}

.metric-card span,
.metric-card em {
  color: #607167;
  font-size: 13px;
  font-style: normal;
}

.metric-card strong {
  font-size: 24px;
}

.calendar-strip {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  overflow: hidden;
}

.calendar-strip img {
  width: 100%;
  height: 132px;
  object-fit: cover;
}

.calendar-strip div {
  padding: 18px;
}

.calendar-strip h3,
.data-panel h3,
.form-panel h3 {
  margin: 0 0 12px;
}

.org-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.data-panel,
.form-panel,
.chart-panel {
  padding: 16px;
}

.people-list,
.compact-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
}

.people-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid #eef2ef;
}

.people-list span,
.people-list em {
  display: block;
  margin-top: 4px;
  color: #607167;
  font-size: 13px;
  font-style: normal;
}

.people-list em.locked {
  color: #8a5a16;
}

.app-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.app-tile {
  min-height: 120px;
  display: grid;
  align-content: space-between;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #d8e2dc;
  background: #ffffff;
}

.app-tile span {
  color: #607167;
}

.app-tile em {
  align-self: end;
  color: #40564a;
  font-size: 13px;
  font-style: normal;
}

.app-tile[data-tone='blue'] {
  border-top: 4px solid #4776b4;
}

.app-tile[data-tone='green'] {
  border-top: 4px solid #3a8f61;
}

.app-tile[data-tone='amber'] {
  border-top: 4px solid #b98224;
}

.app-tile[data-tone='red'] {
  border-top: 4px solid #b75050;
}

.form-panel {
  display: grid;
  gap: 12px;
}

.form-grid {
  display: grid;
  gap: 10px;
}

.four-cols {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.five-cols {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar button.secondary,
.form-panel button.secondary {
  background: #e4ece7;
  color: #1f3a2b;
}

.compact-list {
  max-height: 140px;
  overflow: auto;
  color: #4d5f54;
  font-size: 14px;
}

.compact-list li {
  padding: 8px 0;
  border-top: 1px solid #eef2ef;
}

.chart-panel {
  display: grid;
  gap: 16px;
}

.bar-row {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr) 110px;
  gap: 12px;
  align-items: center;
}

.bar-row div {
  height: 12px;
  border-radius: 999px;
  background: #e6eee9;
  overflow: hidden;
}

.bar-row i {
  display: block;
  height: 100%;
  max-width: 100%;
  border-radius: inherit;
  background: #3a8f61;
}

.right-rail {
  display: grid;
  gap: 12px;
  align-content: start;
}

.rail-card {
  display: grid;
  gap: 8px;
  padding: 16px;
}

.rail-card span {
  color: #607167;
  line-height: 1.55;
  font-size: 14px;
}

@media (max-width: 1100px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .right-rail {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .metrics-grid,
  .split-grid,
  .app-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .org-grid,
  .four-cols,
  .five-cols {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .login-panel,
  .admin-shell,
  .content-layout,
  .calendar-strip {
    grid-template-columns: 1fr;
  }

  .admin-shell {
    grid-template-rows: auto 1fr;
  }

  .sidebar {
    padding: 12px;
  }

  .nav-list {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .nav-list button {
    text-align: center;
  }

  .topbar,
  .hero-band,
  .user-box {
    align-items: flex-start;
    flex-direction: column;
  }

  .right-rail,
  .metrics-grid,
  .split-grid,
  .app-grid,
  .org-grid,
  .four-cols,
  .five-cols {
    grid-template-columns: 1fr;
  }
}
</style>
