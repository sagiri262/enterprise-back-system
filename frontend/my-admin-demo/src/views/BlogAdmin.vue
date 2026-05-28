<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { MdEditor, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import {
  FilePlus2,
  Home,
  LogOut,
  MessageCircle,
  RefreshCw,
  ShieldCheck,
  Trash2,
  Users,
} from 'lucide-vue-next'

const apiBase = import.meta.env.VITE_API_BASE_URL || ''
const adminTokenKey = 'blog_admin_token'
const adminUserKey = 'blog_admin_user'
const token = ref(localStorage.getItem(adminTokenKey) || '')
const currentUser = ref(localStorage.getItem(adminUserKey) || '')
const currentProfile = ref(null)
const activeTab = ref('articles')
const message = ref('')
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const articleForm = reactive({
  title: '',
  author: 'Zhao Young',
  summary: '',
  tag: '',
  category: '',
  contentMarkdown: '# 新文章标题\n\n在这里使用 Markdown 写正文。',
  status: 1,
})

const articles = ref([])
const comments = ref([])
const users = ref([])
const selectedArticle = ref(null)

const isLoggedIn = computed(() => Boolean(token.value))
const roles = computed(() => currentProfile.value?.roles || [])
const permissions = computed(() => currentProfile.value?.permissions || [])
const isAdminUser = computed(() => roles.value.includes('admin'))
const canManageArticles = computed(() => isAdminUser.value || permissions.value.includes('blog:article:create'))
const canManageComments = computed(() => isAdminUser.value || permissions.value.includes('blog:comment:delete'))
const canManageUsers = computed(() => isAdminUser.value || permissions.value.includes('user:list'))

const clearSession = () => {
  token.value = ''
  currentUser.value = ''
  currentProfile.value = null
  localStorage.removeItem(adminTokenKey)
  localStorage.removeItem(adminUserKey)
}

const authHeaders = () => {
  const headers = { 'Content-Type': 'application/json' }
  if (token.value) {
    headers.Authorization = token.value.startsWith('Bearer ') ? token.value : `Bearer ${token.value}`
  }
  return headers
}

const parseJsonResponse = async (resp) => {
  const text = await resp.text()
  let json = null
  try {
    json = text ? JSON.parse(text) : null
  } catch {
    throw new Error(text ? `后端返回不是 JSON：${text.slice(0, 80)}` : '无法连接 Java 后端，请先在项目根目录运行 ./mvnw spring-boot:run 启动 8080 服务')
  }
  if (!resp.ok) {
    if (resp.status === 401) {
      throw new Error('登录已过期，请重新登录')
    }
    if (resp.status === 500) {
      throw new Error(json?.message || '无法连接 Java 后端，请先在项目根目录运行 ./mvnw spring-boot:run 启动 8080 服务')
    }
    throw new Error(json?.message || `请求失败：${resp.status}`)
  }
  if (!json) {
    throw new Error('无法连接 Java 后端，请先在项目根目录运行 ./mvnw spring-boot:run 启动 8080 服务')
  }
  if (json.code !== 0) {
    throw new Error(json.message || '请求失败')
  }
  return json
}

const request = async (url, options = {}) => {
  loading.value = true
  try {
    const resp = await fetch(`${apiBase}${url}`, options)
    return await parseJsonResponse(resp)
  } finally {
    loading.value = false
  }
}

const loadCurrentProfile = async () => {
  const json = await request('/api/blog/auth/me', { headers: authHeaders() })
  currentProfile.value = json.data
  currentUser.value = json.data?.nickname || json.data?.username || currentUser.value
  return currentProfile.value
}

const login = async () => {
  message.value = ''
  try {
    const json = await request('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginForm),
    })
    token.value = json.data.token
    currentUser.value = json.data.username
    localStorage.setItem(adminTokenKey, token.value)
    localStorage.setItem(adminUserKey, currentUser.value)
    await loadCurrentProfile()
    if (!canManageArticles.value && !canManageComments.value && !canManageUsers.value) {
      clearSession()
      message.value = '当前账号没有进入博客管理界面的权限'
      return
    }
    message.value = `登录成功：${currentUser.value}`
  } catch (error) {
    message.value = `登录失败：${error.message}`
    return
  }
  await loadInitialData()
}

const logout = () => {
  clearSession()
  message.value = '已退出管理员登录'
}

const loadArticles = async () => {
  try {
    const json = await request('/api/blog/articles/admin?page=1&size=50', { headers: authHeaders() })
    articles.value = json.data?.records || []
    if (!selectedArticle.value && articles.value.length > 0) {
      selectedArticle.value = articles.value[0]
      await loadComments(selectedArticle.value.id)
    }
    message.value = `文章已刷新，共 ${articles.value.length} 篇`
  } catch (error) {
    if (error.message.includes('登录已过期')) {
      clearSession()
    }
    message.value = `文章加载失败：${error.message}`
  }
}

const loadComments = async (articleId = selectedArticle.value?.id) => {
  if (!articleId) {
    comments.value = []
    return
  }
  try {
    selectedArticle.value = articles.value.find((item) => item.id === articleId) || selectedArticle.value
    const json = await request(`/api/blog/comments?articleId=${articleId}`, { headers: authHeaders() })
    comments.value = json.data || []
    message.value = `评论已刷新，共 ${comments.value.length} 条`
  } catch (error) {
    message.value = `评论加载失败：${error.message}`
  }
}

const loadUsers = async () => {
  try {
    const json = await request('/api/users?page=1&size=50', { headers: authHeaders() })
    users.value = json.data?.records || []
  } catch (error) {
    if (error.message.includes('登录已过期')) {
      clearSession()
    }
    message.value = `用户加载失败：${error.message}`
  }
}

const loadInitialData = async () => {
  if (!canManageArticles.value && canManageComments.value) {
    activeTab.value = 'comments'
  }
  if (!canManageArticles.value && !canManageComments.value && canManageUsers.value) {
    activeTab.value = 'users'
  }
  const tasks = []
  if (canManageArticles.value) tasks.push(loadArticles())
  if (canManageUsers.value) tasks.push(loadUsers())
  await Promise.all(tasks)
}

const resetArticleForm = () => {
  articleForm.title = ''
  articleForm.author = 'Zhao Young'
  articleForm.summary = ''
  articleForm.tag = ''
  articleForm.category = ''
  articleForm.contentMarkdown = '# 新文章标题\n\n在这里使用 Markdown 写正文。'
  articleForm.status = 1
}

const saveArticle = async () => {
  message.value = ''
  try {
    await request('/api/blog/articles', {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(articleForm),
    })
    message.value = '文章已上传'
    resetArticleForm()
    await loadArticles()
  } catch (error) {
    message.value = `文章上传失败：${error.message}`
  }
}

const deleteArticle = async (article) => {
  if (!window.confirm(`确认删除文章「${article.title}」吗？`)) return
  try {
    await request(`/api/blog/articles/${article.id}`, {
      method: 'DELETE',
      headers: authHeaders(),
    })
    if (selectedArticle.value?.id === article.id) {
      selectedArticle.value = null
      comments.value = []
    }
    message.value = '文章已删除'
    await loadArticles()
  } catch (error) {
    message.value = `删除文章失败：${error.message}`
  }
}

const deleteComment = async (comment) => {
  try {
    await request(`/api/blog/comments/${comment.id}`, {
      method: 'DELETE',
      headers: authHeaders(),
    })
    message.value = '评论已删除'
    await loadComments(comment.articleId)
  } catch (error) {
    message.value = `删除评论失败：${error.message}`
  }
}

const disableUser = async (user) => {
  try {
    await request(`/api/users/${user.id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({
        username: user.username,
        nickname: user.nickname,
        email: user.email,
        status: user.status === 1 ? 0 : 1,
      }),
    })
    message.value = user.status === 1 ? '用户已禁用' : '用户已启用'
    await loadUsers()
  } catch (error) {
    message.value = `用户状态更新失败：${error.message}`
  }
}

const formatDate = (value) => (value ? String(value).slice(0, 19).replace('T', ' ') : '')

onMounted(() => {
  if (isLoggedIn.value) {
    loadCurrentProfile().then(loadInitialData).catch(() => {
      clearSession()
    })
  }
})
</script>

<template>
  <main class="blog-admin">
    <RouterLink class="home-link" to="/">
      <Home :size="20" />
      返回主页
    </RouterLink>

    <section v-if="!isLoggedIn" class="login-panel">
      <div>
        <p>Blog Admin</p>
        <h1>博客管理界面</h1>
        <span>管理员可管理文章、评论和用户；博客作者账号可发布、修改和删除文章。</span>
      </div>
      <form @submit.prevent="login">
        <input v-model="loginForm.username" autocomplete="username" placeholder="管理员或作者账号" />
        <input v-model="loginForm.password" autocomplete="current-password" type="password" placeholder="密码" />
        <button type="submit" :disabled="loading">
          <ShieldCheck :size="18" />
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p v-if="message" class="message">{{ message }}</p>
    </section>

    <section v-else class="admin-workspace">
      <header class="topbar">
        <div>
          <p>Blog Admin</p>
          <h1>文章与评论管理</h1>
        </div>
        <div class="status-box">
          <span>{{ loading ? '请求中...' : message || '系统就绪' }}</span>
          <small>{{ isAdminUser ? '管理员' : '博客作者' }}</small>
          <strong>{{ currentUser }}</strong>
          <button type="button" @click="logout">
            <LogOut :size="18" />
            退出
          </button>
        </div>
      </header>

      <nav class="tabs">
        <button v-if="canManageArticles" type="button" :class="{ active: activeTab === 'articles' }" @click="activeTab = 'articles'">
          <FilePlus2 :size="18" />
          文章
        </button>
        <button v-if="canManageComments" type="button" :class="{ active: activeTab === 'comments' }" @click="activeTab = 'comments'">
          <MessageCircle :size="18" />
          评论
        </button>
        <button v-if="canManageUsers" type="button" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">
          <Users :size="18" />
          用户
        </button>
      </nav>

      <section v-if="activeTab === 'articles' && canManageArticles" class="grid-layout">
        <form class="compose-panel" @submit.prevent="saveArticle">
          <h2>上传文章</h2>
          <div class="form-grid">
            <input v-model="articleForm.title" required placeholder="标题" />
            <input v-model="articleForm.author" required placeholder="作者" />
            <input v-model="articleForm.tag" placeholder="标签" />
            <input v-model="articleForm.category" placeholder="主题分类" />
          </div>
          <textarea v-model="articleForm.summary" rows="2" placeholder="摘要"></textarea>
          <MdEditor v-model="articleForm.contentMarkdown" language="zh-CN" preview-theme="github" code-theme="github" />
          <button type="submit" :disabled="loading">
            <FilePlus2 :size="18" />
            上传文章
          </button>
        </form>

        <aside class="list-panel">
          <div class="panel-head">
            <h2>文章列表</h2>
            <button type="button" @click="loadArticles">
              <RefreshCw :size="16" />
              刷新
            </button>
          </div>
          <article v-for="article in articles" :key="article.id" class="row-card" @click="selectedArticle = article; loadComments(article.id)">
            <div>
              <strong>{{ article.title }}</strong>
              <span>{{ formatDate(article.publishTime || article.createTime) }} / {{ article.commentCount || 0 }} 评论</span>
            </div>
            <button type="button" @click.stop="deleteArticle(article)">
              <Trash2 :size="16" />
            </button>
          </article>
        </aside>
      </section>

      <section v-if="activeTab === 'comments' && canManageComments" class="grid-layout">
        <aside class="list-panel">
          <div class="panel-head">
            <h2>选择文章</h2>
            <button type="button" @click="loadArticles">
              <RefreshCw :size="16" />
              刷新
            </button>
          </div>
          <button
            v-for="article in articles"
            :key="article.id"
            class="article-choice"
            type="button"
            :class="{ active: selectedArticle?.id === article.id }"
            @click="loadComments(article.id)"
          >
            {{ article.title }}
          </button>
        </aside>

        <section class="list-panel">
          <div class="panel-head">
            <h2>评论管理</h2>
            <span>{{ selectedArticle?.title || '未选择文章' }}</span>
          </div>
          <article v-for="comment in comments" :key="comment.id" class="comment-card">
            <header>
              <strong>{{ comment.nickname }}</strong>
              <span>{{ formatDate(comment.createTime) }}</span>
              <em>{{ comment.ipAddress || 'no-ip' }}</em>
              <button type="button" @click="deleteComment(comment)">
                <Trash2 :size="16" />
              </button>
            </header>
            <p>{{ comment.content }}</p>
          </article>
          <div v-if="comments.length === 0" class="empty-box">暂无评论。</div>
        </section>
      </section>

      <section v-if="activeTab === 'users' && canManageUsers" class="list-panel users-panel">
        <div class="panel-head">
          <h2>注册用户与站主账号</h2>
          <button type="button" @click="loadUsers">
            <RefreshCw :size="16" />
            刷新
          </button>
        </div>
        <article v-for="user in users" :key="user.id" class="user-card">
          <img :src="user.avatar || '/favicon.ico'" alt="用户头像" />
          <div>
            <strong>{{ user.username }}</strong>
            <span>{{ user.nickname || '未命名' }} / {{ user.email || '无邮箱' }}</span>
            <small>{{ user.siteOwner === 1 ? '个人站站主账号' : '普通注册账号' }}</small>
          </div>
          <em :class="{ off: user.status !== 1 }">{{ user.status === 1 ? '启用' : '禁用' }}</em>
          <button type="button" @click="disableUser(user)">{{ user.status === 1 ? '禁用' : '启用' }}</button>
        </article>
      </section>

      <section v-if="selectedArticle" class="preview-panel">
        <h2>文章预览：{{ selectedArticle.title }}</h2>
        <MdPreview :model-value="selectedArticle.contentMarkdown || selectedArticle.summary || ''" preview-theme="github" code-theme="github" />
      </section>
    </section>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.blog-admin {
  min-height: 100vh;
  padding: 28px;
  background: #f5f7fb;
  color: #182238;
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

button,
input,
textarea {
  font: inherit;
}

button {
  border: 0;
  cursor: pointer;
}

.home-link,
.tabs button,
.status-box button,
.compose-panel button,
.panel-head button,
.row-card button,
.comment-card button,
.user-card button,
.login-panel button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 6px;
  min-height: 40px;
  padding: 0 14px;
  background: #4f6fd7;
  color: #ffffff;
  font-weight: 800;
  text-decoration: none;
}

.home-link {
  margin-bottom: 18px;
  background: #ffffff;
  color: #4f6fd7;
  border: 1px solid #dfe5f1;
}

.login-panel,
.admin-workspace {
  width: min(1180px, 100%);
  margin: 0 auto;
}

.login-panel {
  display: grid;
  gap: 20px;
  border-radius: 8px;
  padding: 30px;
  background: #ffffff;
  box-shadow: 0 16px 48px rgba(58, 65, 112, 0.09);
}

.login-panel p,
.topbar p {
  margin: 0 0 8px;
  color: #5d6fb7;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0;
  text-transform: uppercase;
}

.login-panel h1,
.topbar h1,
.compose-panel h2,
.panel-head h2,
.preview-panel h2 {
  margin: 0;
  color: #1c2440;
  letter-spacing: 0;
}

.login-panel span,
.message {
  color: #66708f;
}

.login-panel form,
.form-grid {
  display: grid;
  gap: 12px;
}

.login-panel form {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

input,
textarea {
  width: 100%;
  min-width: 0;
  border: 1px solid #dfe5f1;
  border-radius: 6px;
  padding: 10px 12px;
  background: #ffffff;
  color: #1c2440;
  outline: none;
}

.topbar,
.status-box,
.panel-head,
.comment-card header,
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar {
  justify-content: space-between;
  margin-bottom: 18px;
}

.status-box {
  flex-wrap: wrap;
  justify-content: flex-end;
  color: #66708f;
}

.status-box small {
  border-radius: 999px;
  padding: 4px 10px;
  background: #edf8f1;
  color: #2d8a50;
  font-weight: 800;
}

.status-box button,
.panel-head button {
  background: #ffffff;
  color: #4f6fd7;
  border: 1px solid #dfe5f1;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.tabs button {
  background: #ffffff;
  color: #4f6fd7;
  border: 1px solid #dfe5f1;
}

.tabs button.active {
  background: #4f6fd7;
  color: #ffffff;
}

.grid-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.7fr);
  gap: 18px;
}

.compose-panel,
.list-panel,
.preview-panel {
  display: grid;
  gap: 14px;
  border-radius: 8px;
  padding: 20px;
  background: #ffffff;
  box-shadow: 0 16px 48px rgba(58, 65, 112, 0.08);
}

.form-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.panel-head {
  justify-content: space-between;
}

.panel-head span {
  color: #66708f;
}

.row-card,
.comment-card,
.user-card {
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  padding: 12px;
  background: #fbfcff;
}

.row-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  text-align: left;
}

.row-card div,
.user-card div {
  display: grid;
  gap: 4px;
}

.row-card span,
.comment-card span,
.comment-card em,
.user-card span {
  color: #7f88a4;
  font-size: 13px;
  font-style: normal;
}

.row-card button,
.comment-card button {
  width: 38px;
  min-height: 38px;
  padding: 0;
  background: #fff0f0;
  color: #c0392b;
}

.article-choice {
  min-height: 42px;
  border-radius: 6px;
  padding: 0 12px;
  background: #f3f6ff;
  color: #4f6fd7;
  text-align: left;
  font-weight: 800;
}

.article-choice.active {
  background: #4f6fd7;
  color: #ffffff;
}

.comment-card {
  display: grid;
  gap: 8px;
}

.comment-card header {
  flex-wrap: wrap;
}

.comment-card header button {
  margin-left: auto;
}

.comment-card p {
  margin: 0;
  color: #46506b;
  line-height: 1.75;
}

.users-panel {
  width: min(860px, 100%);
}

.user-card {
  justify-content: space-between;
}

.user-card img {
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  border: 1px solid #dfe5f1;
  border-radius: 8px;
  object-fit: cover;
}

.user-card em {
  border-radius: 999px;
  padding: 4px 10px;
  background: #edf8f1;
  color: #2d8a50;
  font-style: normal;
  font-weight: 800;
}

.user-card small {
  color: #4f6fd7;
  font-size: 12px;
  font-weight: 800;
}

.user-card em.off {
  background: #fff0f0;
  color: #c0392b;
}

.empty-box {
  min-height: 90px;
  display: grid;
  place-items: center;
  border: 1px dashed #d6ddec;
  border-radius: 8px;
  color: #66708f;
}

.preview-panel {
  margin-top: 18px;
}

@media (max-width: 920px) {
  .blog-admin {
    padding: 16px;
  }

  .topbar,
  .grid-layout,
  .login-panel form,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .topbar {
    display: grid;
  }

  .status-box {
    justify-content: flex-start;
  }
}
</style>
