<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import MiniSearch from 'minisearch'
import {
  AlignJustify,
  Archive,
  ArrowUp,
  CheckCircle2,
  Eye,
  Grid3X3,
  Home,
  LayoutDashboard,
  Menu,
  MessageCircle,
  RefreshCw,
  Search,
  ShieldCheck,
  Tag,
  X,
} from 'lucide-vue-next'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import avatarUrl from '../assets/images/avatar.svg'

const defaultProfile = {
  username: 'zhaoyoung',
  nickname: 'Zhao Young',
  createdAt: '2026-01-18',
  createTime: '2026-01-18',
  motto: '把工程、写作和一点审美放在同一个工作台上。',
  bio: '把工程、写作和一点审美放在同一个工作台上。',
  followers: 4,
  following: 4,
  avatar: avatarUrl,
}

const apiBase = import.meta.env.VITE_API_BASE_URL || ''
const turnstileSiteKey = import.meta.env.VITE_TURNSTILE_SITE_KEY || ''

const hero = {
  title: 'Zhao Young',
  subtitle: '个人网站主页面，记录文章、项目、Demo 和正在成长的工程作品。',
  badge: 'Personal Site',
}

const fallbackArticles = [
  {
    id: 1,
    title: '从零搭一个个人博客首页',
    excerpt: '记录个人站首页的信息架构、组件拆分、响应式布局和后续接入文章系统的思路。',
    publishTime: '2026-05-22T10:00:00',
    tag: '前端',
    category: 'Vue',
    readCount: 1897,
    commentCount: 8,
    contentMarkdown: '# 从零搭一个个人博客首页\n\n记录个人站首页的信息架构、组件拆分、响应式布局和后续接入文章系统的思路。',
  },
  {
    id: 2,
    title: '企业后台系统的权限模型整理',
    excerpt: '梳理用户、角色、权限、审计日志之间的关系，让后端接口和前端页面更容易继续扩展。',
    publishTime: '2026-05-18T10:00:00',
    tag: '后端',
    category: 'Spring Boot',
    readCount: 936,
    commentCount: 3,
    contentMarkdown: '# 企业后台系统的权限模型整理\n\n梳理用户、角色、权限、审计日志之间的关系。',
  },
  {
    id: 3,
    title: '把站内搜索做成可替换模块',
    excerpt: '使用 MiniSearch 做浏览器端文章搜索，后续可以替换为 Pagefind、Algolia 或自己的搜索接口。',
    publishTime: '2026-05-12T10:00:00',
    tag: '搜索',
    category: '工程化',
    readCount: 704,
    commentCount: 2,
    contentMarkdown: '# 把站内搜索做成可替换模块\n\n先实现标题关键词匹配搜索，后续可以切换到全文搜索。',
  },
  {
    id: 4,
    title: '文章归档页要展示什么',
    excerpt: '归档不是一个简单列表，它应该支持年份、月份、主题和阅读路径。',
    publishTime: '2026-04-29T10:00:00',
    tag: '设计',
    category: '信息架构',
    readCount: 551,
    commentCount: 1,
    contentMarkdown: '# 文章归档页要展示什么\n\n归档不是一个简单列表，它应该支持年份、月份、主题和阅读路径。',
  },
  {
    id: 5,
    title: '我的网站 Demo 预览规范',
    excerpt: '为每个 Demo 保留标题、地址、截图和一句话说明，方便从主页直接进入作品。',
    publishTime: '2026-04-11T10:00:00',
    tag: '作品',
    category: 'Demo',
    readCount: 488,
    commentCount: 0,
    contentMarkdown: '# 我的网站 Demo 预览规范\n\n为每个 Demo 保留标题、地址、截图和一句话说明。',
  },
]

const demos = [
  {
    name: '博客管理后台',
    url: '/blog-admin',
    desc: '管理员上传、删除文章，管理评论和注册用户。',
    internal: true,
  },
  {
    name: '旧后台管理子站',
    url: '/admin',
    desc: '原来的企业后台页面保留为个人站子页面。',
    internal: true,
  },
  {
    name: 'Demo 1 - 预留作品页',
    url: '/demo_1.html',
    desc: '后续可以把你自己的第一个独立作品放在这里。',
    internal: false,
  },
]

const menuEntries = [
  { label: '我的标签', value: '前端 / 后端 / 搜索 / 作品', icon: Tag },
  { label: '文章主题分类', value: 'Vue / Spring Boot / 工程化', icon: Grid3X3 },
  { label: '阅读排行', value: 'Top 10', icon: Eye },
  { label: '文章归档', value: '按时间浏览', icon: Archive, target: 'archive' },
  { label: '旧后台子站', value: '/admin', icon: LayoutDashboard, route: '/admin' },
]

const contributionWeeks = [
  [0, 1, 0, 0, 2, 0, 1],
  [1, 0, 3, 0, 0, 1, 0],
  [0, 0, 2, 4, 0, 0, 1],
  [2, 0, 0, 1, 3, 0, 0],
  [0, 2, 0, 0, 1, 4, 0],
  [1, 0, 3, 0, 0, 2, 1],
  [0, 1, 0, 4, 0, 0, 2],
  [2, 0, 0, 1, 3, 0, 0],
  [0, 0, 2, 0, 1, 4, 0],
  [1, 3, 0, 0, 2, 0, 1],
  [0, 0, 1, 4, 0, 2, 0],
  [2, 0, 0, 1, 0, 3, 0],
]

const createSearchEngine = (items) => {
  const engine = new MiniSearch({
    fields: ['title', 'excerpt', 'tag', 'category'],
    storeFields: ['id', 'title', 'excerpt', 'summary', 'tag', 'category', 'publishTime', 'readCount', 'commentCount', 'contentMarkdown'],
    searchOptions: {
      boost: { title: 4, tag: 2, category: 2 },
      prefix: true,
      fuzzy: 0.2,
    },
  })
  engine.addAll(items)
  return engine
}

const articles = ref([...fallbackArticles])
const comments = ref([])
const captcha = ref(null)
const blogToken = ref(localStorage.getItem('blog_token') || '')
const blogUser = ref(localStorage.getItem('blog_user') || '')
const siteProfile = ref({ ...defaultProfile })
const currentProfile = ref(null)
const profileAvatarInput = ref(null)
const profileForm = reactive({
  nickname: '',
  email: '',
  bio: '',
  avatar: '',
})
const profileMessage = ref('')
const profileSaving = ref(false)
const heroCoverStorageKey = 'personal_site_hero_cover'
const heroCoverNameStorageKey = 'personal_site_hero_cover_name'
const customHeroImage = ref(localStorage.getItem(heroCoverStorageKey) || '')
const heroCoverName = ref(localStorage.getItem(heroCoverNameStorageKey) || '')
const searchKeyword = ref('')
const menuOpen = ref(false)
const scrolledPastHero = ref(false)
const activeView = ref('home')
const selectedArticle = ref(fallbackArticles[0])
const heroCanvas = ref(null)
const heroCoverInput = ref(null)
const heroSection = ref(null)
const turnstileElement = ref(null)
const archiveSection = ref(null)
const articlesSection = ref(null)
const readerSection = ref(null)
const commentsSection = ref(null)
const commentForm = reactive({
  nickname: '',
  email: '',
  content: '',
  captchaAnswer: '',
  turnstileToken: '',
})
const blogLoginForm = reactive({ username: '', password: '' })
const blogRegisterForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  captchaAnswer: '',
  turnstileToken: '',
})
const authMode = ref('guest')
const articleMessage = ref('')
const commentMessage = ref('')
const loadingArticles = ref(false)
const loadingCaptcha = ref(false)
const submittingComment = ref(false)
const authMessage = ref('')
const captchaMessage = ref('')
let cleanupHeroCanvas = () => {}
let updateHeroCoverImage = () => {}
let turnstileWidgetId = null

const searchEngine = computed(() => createSearchEngine(articles.value))
const latestArticles = computed(() => articles.value.slice(0, 3))
const archiveArticles = computed(() => articles.value)
const selectedArticleIndex = computed(() => articles.value.findIndex((article) => article.id === selectedArticle.value.id))
const previousArticle = computed(() => articles.value[selectedArticleIndex.value - 1] || null)
const nextArticle = computed(() => articles.value[selectedArticleIndex.value + 1] || null)
const totalContributions = computed(() => contributionWeeks.flat().reduce((total, count) => total + count, 0))
const isBlogLoggedIn = computed(() => Boolean(blogToken.value))
const profile = computed(() => {
  const source = siteProfile.value || defaultProfile
  return {
    ...defaultProfile,
    ...source,
    avatar: source.avatar || defaultProfile.avatar,
    motto: source.bio || source.motto || defaultProfile.motto,
    createdAt: source.createTime || source.createdAt || defaultProfile.createdAt,
  }
})
const currentProfileName = computed(() => {
  if (!currentProfile.value) return blogUser.value
  return currentProfile.value.nickname || currentProfile.value.username || blogUser.value
})
const canWriteArticle = computed(() => {
  const roles = currentProfile.value?.roles || []
  const permissions = currentProfile.value?.permissions || []
  return roles.includes('admin') || roles.includes('blogger') || permissions.includes('blog:article:create')
})

const accountAge = computed(() => {
  const start = new Date(profile.value.createdAt)
  const now = new Date()
  const totalMonths = Math.max(0, (now.getFullYear() - start.getFullYear()) * 12 + now.getMonth() - start.getMonth())
  const years = Math.floor(totalMonths / 12)
  const months = totalMonths % 12
  if (years > 0) return `${years}年${months}个月`
  return `${months || 1}个月`
})

const searchResults = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return []

  const miniSearchResults = searchEngine.value.search(keyword)
  const fallbackResults = articles.value
    .filter((article) => article.title?.includes(keyword))
    .map((article) => ({ ...article, score: 1 }))

  const merged = new Map()
  for (const result of [...miniSearchResults, ...fallbackResults]) {
    merged.set(result.id, result)
  }

  return [...merged.values()].slice(0, 5)
})

const renderTurnstile = async () => {
  if (!turnstileSiteKey || !turnstileElement.value) return
  if (!window.turnstile) {
    await new Promise((resolve, reject) => {
      const existing = document.querySelector('script[data-turnstile]')
      if (existing) {
        existing.addEventListener('load', resolve, { once: true })
        existing.addEventListener('error', reject, { once: true })
        return
      }
      const script = document.createElement('script')
      script.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js'
      script.async = true
      script.defer = true
      script.dataset.turnstile = 'true'
      script.addEventListener('load', resolve, { once: true })
      script.addEventListener('error', reject, { once: true })
      document.head.appendChild(script)
    })
  }
  if (window.turnstile && turnstileWidgetId === null) {
    turnstileWidgetId = window.turnstile.render(turnstileElement.value, {
      sitekey: turnstileSiteKey,
      callback: (token) => {
        commentForm.turnstileToken = token
        blogRegisterForm.turnstileToken = token
      },
      'expired-callback': () => {
        commentForm.turnstileToken = ''
        blogRegisterForm.turnstileToken = ''
      },
    })
  }
}

const resetTurnstile = () => {
  if (window.turnstile && turnstileWidgetId !== null) {
    window.turnstile.reset(turnstileWidgetId)
  }
  commentForm.turnstileToken = ''
  blogRegisterForm.turnstileToken = ''
}

const authHeaders = (tokenValue = blogToken.value) => {
  const headers = { 'Content-Type': 'application/json' }
  if (tokenValue) {
    headers.Authorization = tokenValue.startsWith('Bearer ') ? tokenValue : `Bearer ${tokenValue}`
  }
  return headers
}

const parseJsonResponse = async (resp) => {
  const text = await resp.text()
  let json = null
  try {
    json = text ? JSON.parse(text) : null
  } catch {
    throw new Error(text ? `后端返回不是 JSON：${text.slice(0, 80)}` : '后端返回为空')
  }
  if (!resp.ok) {
    throw new Error(json?.message || `请求失败：${resp.status}`)
  }
  if (!json) {
    throw new Error('后端返回为空，请确认 Java 服务已启动并返回 JSON')
  }
  if (json.code !== 0) {
    throw new Error(json.message || '请求失败')
  }
  return json
}

const normalizeArticle = (article) => ({
  ...article,
  excerpt: article.summary || article.excerpt || '',
  publishTime: article.publishTime || article.createTime,
  readCount: article.readCount || 0,
  commentCount: article.commentCount || 0,
})

const normalizeProfile = (data) => ({
  ...defaultProfile,
  ...(data || {}),
  avatar: data?.avatar || defaultProfile.avatar,
  bio: data?.bio || defaultProfile.bio,
  motto: data?.bio || defaultProfile.motto,
  createdAt: data?.createTime || data?.createdAt || defaultProfile.createdAt,
})

const formatDate = (value) => {
  if (!value) return ''
  return String(value).slice(0, 10)
}

const syncProfileForm = (data = currentProfile.value) => {
  profileForm.nickname = data?.nickname || ''
  profileForm.email = data?.email || ''
  profileForm.bio = data?.bio || ''
  profileForm.avatar = data?.avatar || ''
}

const loadSiteProfile = async () => {
  try {
    const resp = await fetch(`${apiBase}/api/blog/auth/site-profile`)
    const json = await parseJsonResponse(resp)
    siteProfile.value = normalizeProfile(json.data)
  } catch (error) {
    siteProfile.value = { ...defaultProfile }
    articleMessage.value = articleMessage.value || `站主资料暂不可用，已使用本地资料：${error.message}`
  }
}

const loadMyProfile = async () => {
  if (!blogToken.value) return null
  try {
    const resp = await fetch(`${apiBase}/api/blog/auth/me`, {
      headers: authHeaders(),
    })
    const json = await parseJsonResponse(resp)
    currentProfile.value = normalizeProfile(json.data)
    blogUser.value = currentProfile.value.username
    localStorage.setItem('blog_user', blogUser.value)
    syncProfileForm(currentProfile.value)
    if (currentProfile.value.siteOwner === 1) {
      siteProfile.value = normalizeProfile(currentProfile.value)
    }
    return currentProfile.value
  } catch (error) {
    currentProfile.value = null
    profileMessage.value = `资料加载失败：${error.message}`
    return null
  }
}

const loadArticles = async (keyword = '') => {
  loadingArticles.value = true
  articleMessage.value = ''
  try {
    const params = new URLSearchParams({ page: '1', size: '50' })
    if (keyword.trim()) params.set('keyword', keyword.trim())
    const resp = await fetch(`${apiBase}/api/blog/articles?${params}`)
    const json = await parseJsonResponse(resp)
    const records = json.data?.records || []
    articles.value = records.map(normalizeArticle)
    if (records.length > 0) {
      selectedArticle.value = articles.value[0]
    }
  } catch (error) {
    articleMessage.value = `文章接口暂不可用，已使用本地演示数据：${error.message}`
    articles.value = [...fallbackArticles]
  } finally {
    loadingArticles.value = false
  }
}

const loadArticleDetail = async (article) => {
  try {
    const resp = await fetch(`${apiBase}/api/blog/articles/${article.id}`)
    const json = await parseJsonResponse(resp)
    selectedArticle.value = json.data ? normalizeArticle(json.data) : article
  } catch {
    selectedArticle.value = article
  }
}

const loadComments = async () => {
  if (!selectedArticle.value?.id) return
  try {
    const resp = await fetch(`${apiBase}/api/blog/comments?articleId=${selectedArticle.value.id}`)
    const json = await parseJsonResponse(resp)
    comments.value = json.data || []
  } catch {
    comments.value = []
  }
}

const loadCaptcha = async () => {
  loadingCaptcha.value = true
  captchaMessage.value = ''
  try {
    const resp = await fetch(`${apiBase}/api/blog/comments/captcha`)
    const json = await parseJsonResponse(resp)
    captcha.value = json.data
    commentForm.captchaAnswer = ''
    blogRegisterForm.captchaAnswer = ''
  } catch (error) {
    captcha.value = null
    captchaMessage.value = `验证码加载失败：${error.message}`
  } finally {
    loadingCaptcha.value = false
  }
}

const handleHeroCoverSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    articleMessage.value = '请选择图片文件作为封面背景'
    event.target.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    articleMessage.value = '图片超过 2MB，建议先压缩后再作为主页封面'
    event.target.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    const imageUrl = typeof reader.result === 'string' ? reader.result : ''
    if (!imageUrl) return
    customHeroImage.value = imageUrl
    heroCoverName.value = file.name
    localStorage.setItem(heroCoverStorageKey, imageUrl)
    localStorage.setItem(heroCoverNameStorageKey, file.name)
    updateHeroCoverImage(imageUrl)
    articleMessage.value = `封面背景已更新：${file.name}`
    event.target.value = ''
  }
  reader.onerror = () => {
    articleMessage.value = '封面背景读取失败，请重新选择图片'
    event.target.value = ''
  }
  reader.readAsDataURL(file)
}

const handleProfileAvatarSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  profileMessage.value = ''
  if (!file.type.startsWith('image/')) {
    profileMessage.value = '请选择图片文件作为头像'
    event.target.value = ''
    return
  }
  if (file.size > 512 * 1024) {
    profileMessage.value = '头像超过 512KB，建议压缩后再上传'
    event.target.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    const imageUrl = typeof reader.result === 'string' ? reader.result : ''
    if (!imageUrl) return
    profileForm.avatar = imageUrl
    profileMessage.value = `头像已选择：${file.name}，点击保存后生效`
    event.target.value = ''
  }
  reader.onerror = () => {
    profileMessage.value = '头像读取失败，请重新选择图片'
    event.target.value = ''
  }
  reader.readAsDataURL(file)
}

const saveMyProfile = async () => {
  if (!blogToken.value) {
    profileMessage.value = '请先登录普通用户账号'
    return
  }
  profileSaving.value = true
  profileMessage.value = ''
  try {
    const resp = await fetch(`${apiBase}/api/blog/auth/me`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({
        nickname: profileForm.nickname,
        email: profileForm.email,
        bio: profileForm.bio,
        avatar: profileForm.avatar,
      }),
    })
    const json = await parseJsonResponse(resp)
    currentProfile.value = normalizeProfile(json.data)
    syncProfileForm(currentProfile.value)
    if (currentProfile.value.siteOwner === 1) {
      siteProfile.value = normalizeProfile(currentProfile.value)
    }
    profileMessage.value = '资料已保存'
  } catch (error) {
    profileMessage.value = `资料保存失败：${error.message}`
  } finally {
    profileSaving.value = false
  }
}

const resetHeroCover = () => {
  customHeroImage.value = ''
  heroCoverName.value = ''
  localStorage.removeItem(heroCoverStorageKey)
  localStorage.removeItem(heroCoverNameStorageKey)
  updateHeroCoverImage('')
  articleMessage.value = '已恢复默认 Canvas 封面'
}

const submitComment = async () => {
  submittingComment.value = true
  commentMessage.value = ''
  try {
    if (!turnstileSiteKey && !commentForm.turnstileToken.trim()) {
      commentForm.turnstileToken = 'local-dev-token'
    }
    const resp = await fetch(`${apiBase}/api/blog/comments`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({
        articleId: selectedArticle.value.id,
        nickname: isBlogLoggedIn.value ? '' : commentForm.nickname,
        email: isBlogLoggedIn.value ? null : commentForm.email || null,
        content: commentForm.content,
        captchaId: captcha.value?.id,
        captchaAnswer: commentForm.captchaAnswer,
        turnstileToken: commentForm.turnstileToken,
      }),
    })
    await parseJsonResponse(resp)
    commentMessage.value = '评论已发布'
    commentForm.content = ''
    commentForm.captchaAnswer = ''
    resetTurnstile()
    await loadCaptcha()
    await loadComments()
  } catch (error) {
    commentMessage.value = `评论失败：${error.message}`
    await loadCaptcha()
  } finally {
    submittingComment.value = false
  }
}

const blogLogin = async () => {
  authMessage.value = ''
  try {
    const resp = await fetch(`${apiBase}/api/blog/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(blogLoginForm),
    })
    const json = await parseJsonResponse(resp)
    blogToken.value = json.data.token
    blogUser.value = json.data.username
    localStorage.setItem('blog_token', blogToken.value)
    localStorage.setItem('blog_user', blogUser.value)
    await loadMyProfile()
    blogLoginForm.password = ''
    authMessage.value = `已登录：${currentProfileName.value}`
  } catch (error) {
    authMessage.value = `登录失败：${error.message}`
  }
}

const blogRegister = async () => {
  authMessage.value = ''
  try {
    if (!turnstileSiteKey && !blogRegisterForm.turnstileToken.trim()) {
      blogRegisterForm.turnstileToken = 'local-dev-token'
    }
    const resp = await fetch(`${apiBase}/api/blog/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...blogRegisterForm,
        captchaId: captcha.value?.id,
      }),
    })
    const json = await parseJsonResponse(resp)
    blogToken.value = json.data.token
    blogUser.value = json.data.username
    localStorage.setItem('blog_token', blogToken.value)
    localStorage.setItem('blog_user', blogUser.value)
    await loadMyProfile()
    authMode.value = 'guest'
    authMessage.value = `注册并登录成功：${currentProfileName.value}`
    blogRegisterForm.password = ''
    blogRegisterForm.captchaAnswer = ''
    resetTurnstile()
    await loadCaptcha()
  } catch (error) {
    authMessage.value = `注册失败：${error.message}`
    await loadCaptcha()
  }
}

const blogLogout = () => {
  blogToken.value = ''
  blogUser.value = ''
  currentProfile.value = null
  syncProfileForm(null)
  localStorage.removeItem('blog_token')
  localStorage.removeItem('blog_user')
  authMessage.value = '已退出普通用户登录'
  profileMessage.value = ''
}

const scrollToSection = (target, closeMenu = true) => {
  const refs = {
    archive: archiveSection,
    articles: articlesSection,
    comments: commentsSection,
    home: heroSection,
    reader: readerSection,
  }
  refs[target]?.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  if (closeMenu) menuOpen.value = false
}

const returnHome = () => {
  activeView.value = 'home'
  scrollToSection('home', false)
}

const openArticle = (article) => {
  activeView.value = 'article'
  selectedArticle.value = article
  menuOpen.value = false
  loadArticleDetail(article).then(loadComments)
  requestAnimationFrame(() => {
    const target = document.querySelector(`[data-article-id="${article.id}"]`) || readerSection.value
    target?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
}

const handleMenuEntry = (entry) => {
  if (entry.target) {
    scrollToSection(entry.target)
  }
}

const updateScrollState = () => {
  const heroBottom = heroSection.value?.getBoundingClientRect().bottom ?? 0
  scrolledPastHero.value = heroBottom < 0
}

const contributionClass = (count) => {
  if (count <= 0) return 'level-0'
  if (count === 1) return 'level-1'
  if (count === 2) return 'level-2'
  if (count === 3) return 'level-3'
  return 'level-4'
}

const setupHeroCanvas = () => {
  const canvas = heroCanvas.value
  const ctx = canvas?.getContext('2d')
  if (!canvas || !ctx) return

  const pointer = { x: 0.5, y: 0.45 }
  let rafId = 0
  let coverImage = null

  const drawCoverImage = (image, width, height) => {
    const imageRatio = image.naturalWidth / image.naturalHeight
    const canvasRatio = width / height
    const drawHeight = imageRatio > canvasRatio ? height : width / imageRatio
    const drawWidth = imageRatio > canvasRatio ? height * imageRatio : width
    const drawX = (width - drawWidth) / 2
    const drawY = (height - drawHeight) / 2
    ctx.drawImage(image, drawX, drawY, drawWidth, drawHeight)

    const overlay = ctx.createLinearGradient(0, 0, 0, height)
    overlay.addColorStop(0, 'rgba(8, 18, 35, 0.22)')
    overlay.addColorStop(0.55, 'rgba(8, 18, 35, 0.1)')
    overlay.addColorStop(1, 'rgba(8, 18, 35, 0.44)')
    ctx.fillStyle = overlay
    ctx.fillRect(0, 0, width, height)
  }

  const drawTree = (x, baseY, scale, color) => {
    ctx.fillStyle = color
    ctx.beginPath()
    ctx.moveTo(x, baseY - 210 * scale)
    ctx.lineTo(x - 42 * scale, baseY - 120 * scale)
    ctx.lineTo(x - 24 * scale, baseY - 120 * scale)
    ctx.lineTo(x - 58 * scale, baseY - 44 * scale)
    ctx.lineTo(x - 18 * scale, baseY - 44 * scale)
    ctx.lineTo(x - 18 * scale, baseY)
    ctx.lineTo(x + 18 * scale, baseY)
    ctx.lineTo(x + 18 * scale, baseY - 44 * scale)
    ctx.lineTo(x + 58 * scale, baseY - 44 * scale)
    ctx.lineTo(x + 24 * scale, baseY - 120 * scale)
    ctx.lineTo(x + 42 * scale, baseY - 120 * scale)
    ctx.closePath()
    ctx.fill()
  }

  const draw = () => {
    const rect = canvas.getBoundingClientRect()
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    const width = Math.max(1, rect.width)
    const height = Math.max(1, rect.height)

    canvas.width = Math.floor(width * dpr)
    canvas.height = Math.floor(height * dpr)
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

    if (coverImage?.complete && coverImage.naturalWidth > 0) {
      drawCoverImage(coverImage, width, height)
      return
    }

    const sky = ctx.createLinearGradient(0, 0, 0, height)
    sky.addColorStop(0, '#0d2447')
    sky.addColorStop(0.45, '#206f84')
    sky.addColorStop(0.75, '#f09a62')
    sky.addColorStop(1, '#18233f')
    ctx.fillStyle = sky
    ctx.fillRect(0, 0, width, height)

    ctx.fillStyle = 'rgba(255, 255, 255, 0.46)'
    for (let index = 0; index < 46; index += 1) {
      const x = (index * 91) % width
      const y = 30 + ((index * 37) % Math.max(80, height * 0.42))
      const size = index % 6 === 0 ? 2 : 1
      ctx.fillRect(x, y, size, size)
    }

    const sunX = width * (0.5 + (pointer.x - 0.5) * 0.04)
    const sunY = height * 0.72
    const sun = ctx.createRadialGradient(sunX, sunY, 8, sunX, sunY, Math.min(width, height) * 0.24)
    sun.addColorStop(0, 'rgba(255, 232, 143, 0.92)')
    sun.addColorStop(0.45, 'rgba(255, 176, 96, 0.42)')
    sun.addColorStop(1, 'rgba(255, 176, 96, 0)')
    ctx.fillStyle = sun
    ctx.fillRect(0, 0, width, height)

    ctx.fillStyle = 'rgba(18, 65, 94, 0.76)'
    ctx.beginPath()
    ctx.moveTo(0, height)
    ctx.lineTo(width * 0.22, height * 0.42)
    ctx.lineTo(width * 0.48, height)
    ctx.closePath()
    ctx.fill()

    ctx.fillStyle = 'rgba(8, 39, 68, 0.86)'
    ctx.beginPath()
    ctx.moveTo(width * 0.38, height)
    ctx.lineTo(width * 0.72, height * 0.35)
    ctx.lineTo(width, height)
    ctx.closePath()
    ctx.fill()

    const offset = (pointer.x - 0.5) * 22
    drawTree(70 + offset, height + 18, 1.08, 'rgba(3, 18, 35, 0.88)')
    drawTree(width - 110 + offset * 0.6, height + 28, 1.32, 'rgba(3, 18, 35, 0.92)')
    drawTree(width * 0.74 + offset * 0.45, height + 10, 0.74, 'rgba(5, 28, 49, 0.82)')
    drawTree(width * 0.28 - offset * 0.25, height + 22, 0.68, 'rgba(7, 38, 61, 0.72)')
  }

  const requestDraw = () => {
    window.cancelAnimationFrame(rafId)
    rafId = window.requestAnimationFrame(draw)
  }

  const loadCoverImage = (src) => {
    if (!src) {
      coverImage = null
      requestDraw()
      return
    }
    const image = new Image()
    image.onload = () => {
      coverImage = image
      requestDraw()
    }
    image.onerror = () => {
      coverImage = null
      articleMessage.value = '封面背景加载失败，已恢复默认 Canvas 封面'
      requestDraw()
    }
    image.src = src
  }

  const handlePointerMove = (event) => {
    const rect = canvas.getBoundingClientRect()
    pointer.x = (event.clientX - rect.left) / rect.width
    pointer.y = (event.clientY - rect.top) / rect.height
    requestDraw()
  }

  window.addEventListener('resize', requestDraw)
  canvas.addEventListener('pointermove', handlePointerMove)
  updateHeroCoverImage = loadCoverImage
  loadCoverImage(customHeroImage.value)
  requestDraw()

  cleanupHeroCanvas = () => {
    window.cancelAnimationFrame(rafId)
    window.removeEventListener('resize', requestDraw)
    canvas.removeEventListener('pointermove', handlePointerMove)
    updateHeroCoverImage = () => {}
  }
}

onMounted(() => {
  loadSiteProfile()
  if (blogToken.value) {
    loadMyProfile()
  }
  loadArticles()
  loadCaptcha()
  setupHeroCanvas()
  nextTick(renderTurnstile)
  updateScrollState()
  window.addEventListener('scroll', updateScrollState, { passive: true })
})

onUnmounted(() => {
  cleanupHeroCanvas()
  window.removeEventListener('scroll', updateScrollState)
})
</script>

<template>
  <main class="personal-site" :class="{ 'is-reading': activeView === 'article', 'past-hero': scrolledPastHero }">
    <button
      class="menu-trigger"
      :class="{ compact: scrolledPastHero }"
      type="button"
      aria-label="打开个人菜单"
      @click="menuOpen = true"
    >
      <Menu :size="24" />
      <span v-if="!scrolledPastHero">MENU</span>
    </button>

    <button class="home-trigger" type="button" aria-label="回到主页" @click="returnHome">
      <Home :size="24" />
    </button>

    <section ref="heroSection" class="hero-section">
      <canvas ref="heroCanvas" class="hero-canvas" aria-hidden="true"></canvas>
      <div class="hero-content">
        <p>{{ hero.badge }}</p>
        <h1>{{ hero.title }}</h1>
        <span>{{ hero.subtitle }}</span>
        <button type="button" @click="scrollToSection('articles', false)">开始阅读</button>
      </div>
      <input
        ref="heroCoverInput"
        class="sr-only"
        type="file"
        accept="image/*"
        @change="handleHeroCoverSelect"
      />
      <button
        class="canvas-chip"
        type="button"
        :title="heroCoverName ? `当前封面：${heroCoverName}` : '选择本地图片作为主页封面'"
        aria-label="选择本地图片作为主页封面"
        @click="heroCoverInput?.click()"
      >
        <img src="/edit.svg" alt="自定义封面" />
      </button>
      <button
        v-if="customHeroImage"
        class="canvas-reset"
        type="button"
        title="恢复默认 Canvas 封面"
        @click="resetHeroCover"
      >
        恢复默认
      </button>
    </section>

    <section class="content-shell">
      <section ref="articlesSection" class="section-block articles-block">
        <div class="section-heading">
          <h2>最新文章</h2>
          <button type="button" @click="scrollToSection('archive', false)">展开更多</button>
        </div>
        <div class="article-search-row">
          <label>
            <Search :size="20" />
            <input
              v-model="searchKeyword"
              type="search"
              placeholder="按标题关键词搜索文章"
              @keyup.enter="loadArticles(searchKeyword)"
            />
          </label>
          <button type="button" @click="loadArticles(searchKeyword)">搜索</button>
          <button type="button" class="ghost-button" @click="searchKeyword = ''; loadArticles()">重置</button>
        </div>
        <p v-if="articleMessage" class="inline-message">{{ articleMessage }}</p>
        <p v-if="loadingArticles" class="inline-message">文章加载中...</p>

        <article
          v-for="article in latestArticles"
          :key="article.id"
          class="article-card"
          :data-article-id="article.id"
          @click="openArticle(article)"
        >
          <div>
            <span>{{ article.tag }}</span>
            <h3>{{ article.title }}</h3>
            <p>{{ article.excerpt }}</p>
          </div>
          <footer>
            <time>{{ formatDate(article.publishTime) }}</time>
            <span>{{ article.readCount }} 阅读</span>
            <span>{{ article.commentCount }} 评论</span>
          </footer>
        </article>
      </section>

      <section class="section-block demo-block">
        <div class="section-heading">
          <h2>网站预览</h2>
          <span>Demo Preview</span>
        </div>
        <div class="demo-grid">
          <RouterLink v-for="demo in demos.filter((item) => item.internal)" :key="demo.url" class="demo-card" :to="demo.url">
            <strong>《{{ demo.name }}》</strong>
            <code>{{ demo.url }}</code>
            <span>{{ demo.desc }}</span>
          </RouterLink>
          <a
            v-for="demo in demos.filter((item) => !item.internal)"
            :key="demo.url"
            class="demo-card"
            :href="demo.url"
            target="_blank"
            rel="noopener"
          >
            <strong>《{{ demo.name }}》</strong>
            <code>{{ demo.url }}</code>
            <span>{{ demo.desc }}</span>
          </a>
        </div>
      </section>

      <section ref="archiveSection" class="section-block archive-block">
        <div class="section-heading">
          <h2>文章归档</h2>
          <span>Archive</span>
        </div>
        <div class="archive-list">
          <a v-for="article in archiveArticles" :key="article.id" href="#" @click.prevent="openArticle(article)">
            <time>{{ formatDate(article.publishTime) }}</time>
            <span>{{ article.title }}</span>
            <em>{{ article.category }}</em>
          </a>
        </div>
      </section>

      <section ref="readerSection" class="section-block article-reader" aria-label="文章阅读示例">
        <div class="section-heading">
          <h2>文章阅读区域</h2>
          <span>Reading</span>
        </div>
        <article class="reader-prose">
          <h3>{{ selectedArticle.title }}</h3>
          <MdPreview
            class="markdown-preview"
            :model-value="selectedArticle.contentMarkdown || selectedArticle.excerpt || ''"
            preview-theme="github"
            code-theme="github"
          />
        </article>
        <nav class="article-nav" aria-label="上一篇下一篇">
          <a v-if="previousArticle" href="#" @click.prevent="openArticle(previousArticle)">上一篇：{{ previousArticle.title }}</a>
          <a v-if="nextArticle" href="#" @click.prevent="openArticle(nextArticle)">下一篇：{{ nextArticle.title }}</a>
        </nav>
      </section>

      <section ref="commentsSection" class="section-block comment-block">
        <div class="section-heading">
          <h2>评论</h2>
          <span>Comments</span>
        </div>
        <div class="comment-list">
          <article v-for="comment in comments" :key="comment.id" class="comment-item">
            <header>
              <strong>{{ comment.nickname }}</strong>
              <time>{{ formatDate(comment.createTime) }}</time>
            </header>
            <p>{{ comment.content }}</p>
          </article>
          <div v-if="comments.length === 0" class="comment-box">还没有评论，坐下写第一条。</div>
        </div>
        <div class="comment-auth-panel">
          <div class="comment-auth-tabs">
            <button type="button" :class="{ active: authMode === 'guest' }" @click="authMode = 'guest'">游客评论</button>
            <button type="button" :class="{ active: authMode === 'login' }" @click="authMode = 'login'">普通用户登录</button>
            <button type="button" :class="{ active: authMode === 'register' }" @click="authMode = 'register'">注册普通用户</button>
          </div>
          <div v-if="isBlogLoggedIn" class="login-status">
            <span>
              当前登录：{{ currentProfileName }}
              <em v-if="canWriteArticle">作者账号</em>
            </span>
            <button type="button" class="ghost-button" @click="blogLogout">退出</button>
          </div>
          <form v-if="isBlogLoggedIn" class="profile-editor" @submit.prevent="saveMyProfile">
            <input
              ref="profileAvatarInput"
              class="sr-only"
              type="file"
              accept="image/*"
              @change="handleProfileAvatarSelect"
            />
            <button class="avatar-edit-button" type="button" @click="profileAvatarInput?.click()">
              <img :src="profileForm.avatar || currentProfile?.avatar || avatarUrl" alt="当前头像" />
              <span>更换头像</span>
            </button>
            <div class="profile-fields">
              <input v-model="profileForm.nickname" placeholder="昵称" />
              <input v-model="profileForm.email" type="email" placeholder="邮箱" />
              <textarea v-model="profileForm.bio" rows="2" placeholder="个人简介"></textarea>
            </div>
            <div class="profile-actions">
              <button type="submit" :disabled="profileSaving">{{ profileSaving ? '保存中...' : '保存资料' }}</button>
              <RouterLink v-if="canWriteArticle" class="author-link" to="/blog-admin">进入文章后台</RouterLink>
            </div>
            <p v-if="profileMessage" class="inline-message">{{ profileMessage }}</p>
          </form>
          <form v-if="authMode === 'login' && !isBlogLoggedIn" class="comment-auth-form" @submit.prevent="blogLogin">
            <input v-model="blogLoginForm.username" required autocomplete="username" placeholder="用户名" />
            <input v-model="blogLoginForm.password" required autocomplete="current-password" type="password" placeholder="密码" />
            <button type="submit">登录后评论</button>
          </form>
          <form v-if="authMode === 'register' && !isBlogLoggedIn" class="comment-auth-form register-form" @submit.prevent="blogRegister">
            <input v-model="blogRegisterForm.username" required autocomplete="username" placeholder="注册用户名" />
            <input v-model="blogRegisterForm.password" required autocomplete="new-password" type="password" placeholder="注册密码，至少6位" />
            <input v-model="blogRegisterForm.nickname" placeholder="昵称，可不填" />
            <input v-model="blogRegisterForm.email" required type="email" placeholder="邮箱" />
            <div class="captcha-image-box">
              <img v-if="captcha?.image" :src="captcha.image" alt="验证码图片" />
              <span v-else>{{ loadingCaptcha ? '验证码加载中...' : (captchaMessage || captcha?.question || '点击换一个加载验证码') }}</span>
            </div>
            <input v-model="blogRegisterForm.captchaAnswer" required placeholder="验证码答案" />
            <button type="button" class="ghost-button" :disabled="loadingCaptcha" @click="loadCaptcha">
              <RefreshCw :size="16" />
              {{ loadingCaptcha ? '加载中' : '换一个' }}
            </button>
            <button type="submit">注册</button>
          </form>
          <p v-if="authMessage" class="inline-message">{{ authMessage }}</p>
        </div>
        <form class="comment-form" @submit.prevent="submitComment">
          <input v-if="!isBlogLoggedIn" v-model="commentForm.nickname" placeholder="昵称，可不填" />
          <input v-if="!isBlogLoggedIn" v-model="commentForm.email" type="email" placeholder="邮箱，可不填" />
          <textarea v-model="commentForm.content" required rows="4" placeholder="写下你的评论"></textarea>
          <div class="captcha-row">
            <div class="captcha-image-box">
              <img v-if="captcha?.image" :src="captcha.image" alt="验证码图片" />
              <span v-else>{{ loadingCaptcha ? '验证码加载中...' : (captchaMessage || captcha?.question || '点击换一个加载验证码') }}</span>
            </div>
            <input v-model="commentForm.captchaAnswer" required placeholder="答案" />
            <button type="button" class="ghost-button" :disabled="loadingCaptcha" @click="loadCaptcha">
              <RefreshCw :size="16" />
              {{ loadingCaptcha ? '加载中' : '换一个' }}
            </button>
          </div>
          <p v-if="captchaMessage" class="inline-message">{{ captchaMessage }}</p>
          <div v-if="turnstileSiteKey" ref="turnstileElement" class="turnstile-box"></div>
          <p v-else class="inline-message">本地开发模式：Cloudflare Turnstile 未配置 site key，提交时使用本地 token；生产环境必须配置。</p>
          <button type="submit" :disabled="submittingComment">
            <CheckCircle2 :size="18" />
            {{ submittingComment ? '提交中...' : '发表评论' }}
          </button>
          <p v-if="commentMessage" class="inline-message">{{ commentMessage }}</p>
        </form>
      </section>

      <footer class="site-footer">
        <span>Personal Site Control</span>
        <RouterLink to="/blog-admin">
          <ShieldCheck :size="18" />
          管理员入口
        </RouterLink>
      </footer>
    </section>

    <aside class="profile-drawer" :class="{ open: menuOpen }" aria-label="个人菜单">
      <header class="drawer-header">
        <span>欢迎访问本站</span>
        <button type="button" aria-label="关闭菜单" @click="menuOpen = false">
          <X :size="24" />
        </button>
      </header>

      <section class="profile-card">
        <img :src="profile.avatar" alt="个人头像" />
        <div>
          <strong>昵称：{{ profile.nickname }}</strong>
          <span>{{ profile.motto }}</span>
          <span>创建：{{ formatDate(profile.createdAt) }}</span>
          <span>站龄：{{ accountAge }}</span>
          <span>粉丝：{{ profile.followers }}　关注：{{ profile.following }}</span>
        </div>
      </section>

      <label class="search-box">
        <input v-model="searchKeyword" type="search" placeholder="搜索站内文章..." />
        <Search :size="23" />
      </label>

      <div v-if="searchResults.length" class="search-results">
        <button v-for="result in searchResults" :key="result.id" type="button" @click="openArticle(result)">
          <strong>{{ result.title }}</strong>
          <span>{{ result.category }} / {{ result.tag }}</span>
        </button>
      </div>

      <nav class="drawer-links" aria-label="文章入口">
        <RouterLink v-for="entry in menuEntries.filter((item) => item.route)" :key="entry.label" :to="entry.route" @click="menuOpen = false">
          <span>
            <component :is="entry.icon" :size="24" />
            {{ entry.label }}
          </span>
          <em>{{ entry.value }} ></em>
        </RouterLink>
        <button v-for="entry in menuEntries.filter((item) => !item.route)" :key="entry.label" type="button" @click="handleMenuEntry(entry)">
          <span>
            <component :is="entry.icon" :size="24" />
            {{ entry.label }}
          </span>
          <em>{{ entry.value }} ></em>
        </button>
      </nav>

      <section class="contribution-panel">
        <div>
          <strong>{{ totalContributions }} contributions in the last year</strong>
          <span>提交记录 / 写作记录</span>
        </div>
        <div class="contribution-grid" aria-label="提交记录">
          <span
            v-for="(day, index) in contributionWeeks.flat()"
            :key="index"
            :class="contributionClass(day)"
            :title="`${day} 次记录`"
          ></span>
        </div>
        <footer>
          <span>Less</span>
          <i class="level-0"></i>
          <i class="level-1"></i>
          <i class="level-2"></i>
          <i class="level-3"></i>
          <i class="level-4"></i>
          <span>More</span>
        </footer>
      </section>
    </aside>

    <button v-if="menuOpen" class="drawer-mask" type="button" aria-label="关闭菜单背景" @click="menuOpen = false"></button>

    <nav class="quick-actions" :class="{ visible: scrolledPastHero }" aria-label="阅读快捷操作">
      <button type="button" title="文章目录" aria-label="回到文章目录" @click="scrollToSection('articles', false)">
        <AlignJustify :size="25" />
      </button>
      <button type="button" title="主页" aria-label="主页" @click="returnHome">
        <Home :size="24" />
      </button>
      <button type="button" title="评论" aria-label="评论" @click="scrollToSection('comments', false)">
        <MessageCircle :size="24" />
      </button>
      <button type="button" title="返回顶部" aria-label="返回顶部" @click="returnHome">
        <ArrowUp :size="24" />
      </button>
    </nav>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.personal-site {
  min-height: 100vh;
  background: #f6f8fc;
  color: #192238;
  font-family:
    Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

button,
input {
  font: inherit;
}

button {
  border: 0;
  cursor: pointer;
}

a {
  color: inherit;
  text-decoration: none;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  clip-path: inset(50%);
}

.menu-trigger,
.home-trigger,
.quick-actions button {
  display: inline-grid;
  place-items: center;
  color: #6f76b8;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(121, 132, 190, 0.18);
  box-shadow: 0 10px 28px rgba(58, 65, 112, 0.13);
  backdrop-filter: blur(16px);
}

.menu-trigger {
  position: fixed;
  z-index: 40;
  top: 28px;
  left: 28px;
  grid-template-columns: 24px auto;
  gap: 14px;
  min-width: 166px;
  height: 56px;
  border-radius: 6px;
  padding: 0 20px;
  color: #ffffff;
  background: rgba(12, 28, 52, 0.42);
  border-color: rgba(255, 255, 255, 0.5);
  font-size: 20px;
}

.menu-trigger.compact {
  top: auto;
  bottom: 26px;
  left: 0;
  min-width: 46px;
  width: 46px;
  height: 54px;
  border-radius: 0 6px 6px 0;
  padding: 0;
  color: #6f76b8;
  background: rgba(255, 255, 255, 0.92);
  border-color: rgba(121, 132, 190, 0.16);
}

.home-trigger {
  position: fixed;
  z-index: 35;
  top: 28px;
  right: 28px;
  width: 56px;
  height: 56px;
  border-radius: 999px;
  color: #ffffff;
  background: rgba(12, 28, 52, 0.42);
  border-color: rgba(255, 255, 255, 0.42);
}

.hero-section {
  position: relative;
  min-height: 520px;
  display: grid;
  place-items: center;
  overflow: hidden;
  color: #ffffff;
  background: #10243f;
}

.hero-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.hero-section::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(10, 24, 43, 0.24), rgba(10, 24, 43, 0.12) 45%, rgba(10, 24, 43, 0.42)),
    radial-gradient(circle at 50% 58%, rgba(255, 244, 177, 0.22), transparent 22%);
}

.hero-content {
  position: relative;
  z-index: 2;
  width: min(760px, calc(100vw - 48px));
  text-align: center;
  text-shadow: 0 3px 20px rgba(3, 11, 26, 0.45);
}

.hero-content p {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.hero-content h1 {
  margin: 0;
  font-size: clamp(42px, 6vw, 76px);
  line-height: 1.08;
  letter-spacing: 0;
}

.hero-content span {
  display: block;
  max-width: 660px;
  margin: 22px auto 0;
  font-size: 18px;
  line-height: 1.8;
}

.hero-content button {
  min-width: 118px;
  min-height: 42px;
  margin-top: 24px;
  border-radius: 6px;
  padding: 0 18px;
  background: #f3a64e;
  color: #ffffff;
  font-weight: 800;
  box-shadow: 0 12px 24px rgba(89, 52, 19, 0.18);
}

.canvas-chip {
  position: absolute;
  z-index: 2;
  right: 32px;
  bottom: 28px;
  width: 44px;
  min-height: 36px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 0;
  background: rgba(255, 255, 255, 0.78);
  color: #1d3a5c;
  font-size: 13px;
  font-weight: 800;
}

.canvas-chip img {
  width: 22px;
  height: 22px;
}

.canvas-reset {
  position: absolute;
  z-index: 2;
  right: 86px;
  bottom: 32px;
  min-height: 36px;
  border-radius: 999px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.78);
  color: #1d3a5c;
  font-size: 13px;
  font-weight: 800;
}

.content-shell {
  width: min(1280px, calc(100vw - 48px));
  margin: 0 auto;
  padding: 42px 0 96px;
}

.section-block {
  margin-bottom: 28px;
  padding: 28px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 16px 48px rgba(58, 65, 112, 0.09);
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 0;
  color: #1c2440;
  font-size: 32px;
  letter-spacing: 0;
}

.section-heading span,
.section-heading button {
  color: #7e86a7;
  font-size: 14px;
}

.section-heading button {
  min-height: 38px;
  border-radius: 6px;
  padding: 0 16px;
  background: rgba(112, 132, 255, 0.08);
  color: #5371dc;
  font-weight: 800;
}

.article-card {
  display: grid;
  gap: 16px;
  padding: 24px 8px;
  border-top: 1px solid #e9edf5;
  cursor: pointer;
}

.article-search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.article-search-row label {
  min-width: 0;
  min-height: 44px;
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  align-items: center;
  border: 1px solid #dfe5f1;
  border-radius: 6px;
  padding: 0 12px;
  background: #fbfcff;
  color: #7b84a2;
}

.article-search-row input,
.comment-form input,
.comment-auth-form input,
.comment-form textarea,
.captcha-row input {
  width: 100%;
  min-width: 0;
  border: 1px solid #dfe5f1;
  border-radius: 6px;
  background: #ffffff;
  color: #1c2440;
  outline: none;
}

.article-search-row label input {
  height: 42px;
  border: 0;
  background: transparent;
}

.article-search-row button,
.comment-form > button,
.comment-auth-form button,
.captcha-row button,
.ghost-button {
  min-height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 6px;
  padding: 0 14px;
  background: #5371dc;
  color: #ffffff;
  font-weight: 800;
}

.ghost-button {
  background: rgba(112, 132, 255, 0.08);
  color: #5371dc;
}

.inline-message {
  margin: 8px 0 0;
  color: #66708f;
  line-height: 1.6;
  font-size: 14px;
}

.article-card:first-of-type {
  border-top: 0;
}

.article-card span:first-child {
  display: inline-flex;
  width: fit-content;
  min-height: 28px;
  align-items: center;
  border-radius: 6px;
  padding: 0 10px;
  background: #fff2df;
  color: #d0761f;
  font-size: 13px;
  font-weight: 800;
}

.article-card h3 {
  margin: 12px 0 8px;
  font-size: 26px;
  letter-spacing: 0;
}

.article-card p,
.reader-prose p {
  margin: 0;
  color: #46506b;
  font-size: 17px;
  line-height: 1.9;
}

.article-card footer {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #8991aa;
  font-size: 14px;
}

.comment-form {
  display: grid;
  gap: 12px;
}

.comment-form input,
.comment-auth-form input,
.comment-form textarea,
.captcha-row input {
  padding: 10px 12px;
  line-height: 1.6;
}

.demo-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.demo-card {
  min-height: 138px;
  display: grid;
  align-content: center;
  gap: 12px;
  border: 1px solid #dfe5f1;
  border-radius: 8px;
  padding: 20px;
  background:
    linear-gradient(90deg, rgba(83, 113, 220, 0.08), transparent 36%),
    #fbfcff;
}

.demo-card strong {
  font-size: 20px;
}

.demo-card code {
  width: fit-content;
  max-width: 100%;
  overflow-wrap: anywhere;
  border-radius: 4px;
  padding: 6px 8px;
  background: #eef3ff;
  color: #4766c6;
}

.demo-card span,
.archive-list em,
.comment-box {
  color: #66708f;
}

.archive-list {
  display: grid;
}

.archive-list a {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) 140px;
  gap: 16px;
  align-items: center;
  min-height: 54px;
  border-top: 1px solid #edf0f7;
}

.archive-list a:first-child {
  border-top: 0;
}

.archive-list time {
  color: #7b84a2;
}

.archive-list span {
  font-weight: 800;
}

.archive-list em {
  justify-self: end;
  font-style: normal;
}

.reader-prose {
  padding: 10px 8px 20px;
}

.reader-prose h3 {
  margin: 0 0 18px;
  color: #1c2440;
  font-size: 30px;
}

.reader-prose p + p {
  margin-top: 18px;
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3 {
  margin: 0 0 16px;
  color: #1c2440;
}

.markdown-preview p {
  margin: 0 0 14px;
}

.article-nav {
  display: grid;
  gap: 10px;
  padding: 18px 8px 0;
  border-top: 1px dashed #dfe5f1;
}

.article-nav a {
  width: fit-content;
  color: #5371dc;
  font-weight: 800;
}

.comment-box {
  min-height: 88px;
  display: grid;
  place-items: center;
  border: 1px dashed #d6ddec;
  border-radius: 8px;
  background: #fbfcff;
}

.comment-list {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-auth-panel {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  padding: 14px;
  background: #fbfcff;
}

.comment-auth-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.comment-auth-tabs button,
.login-status button {
  min-height: 36px;
  border-radius: 6px;
  padding: 0 12px;
  background: rgba(112, 132, 255, 0.08);
  color: #5371dc;
  font-weight: 800;
}

.comment-auth-tabs button.active {
  background: #5371dc;
  color: #ffffff;
}

.login-status,
.comment-auth-form,
.profile-editor {
  display: grid;
  gap: 10px;
}

.login-status {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  color: #46506b;
}

.login-status span {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.login-status em {
  border-radius: 999px;
  padding: 3px 8px;
  background: #edf8f1;
  color: #2d8a50;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.comment-auth-form {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.comment-auth-form.register-form {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.profile-editor {
  grid-template-columns: 112px minmax(0, 1fr) auto;
  align-items: center;
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  padding: 12px;
  background: #ffffff;
}

.avatar-edit-button {
  width: 104px;
  display: grid;
  gap: 6px;
  justify-items: center;
  padding: 0;
  background: transparent;
  color: #5371dc;
  font-size: 13px;
  font-weight: 800;
}

.avatar-edit-button img {
  width: 64px;
  height: 64px;
  border: 1px solid #dfe5f1;
  border-radius: 8px;
  object-fit: cover;
}

.profile-fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.profile-fields textarea {
  grid-column: 1 / -1;
  width: 100%;
  min-width: 0;
  border: 1px solid #dfe5f1;
  border-radius: 6px;
  padding: 10px 12px;
  background: #ffffff;
  color: #1c2440;
  resize: vertical;
  outline: none;
}

.profile-actions {
  display: grid;
  gap: 8px;
}

.profile-actions button,
.author-link {
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  padding: 0 12px;
  background: #5371dc;
  color: #ffffff;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
}

.author-link {
  background: rgba(112, 132, 255, 0.08);
  color: #5371dc;
}

.profile-editor .inline-message {
  grid-column: 1 / -1;
}

.comment-item {
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  padding: 14px;
  background: #fbfcff;
}

.comment-item header {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 8px;
}

.comment-item time {
  color: #8991aa;
  font-size: 13px;
}

.comment-item button {
  min-height: 28px;
  border-radius: 6px;
  padding: 0 10px;
  background: #fff0f0;
  color: #c0392b;
  font-weight: 800;
}

.comment-item p {
  margin: 0;
  color: #46506b;
  line-height: 1.75;
}

.captcha-row {
  display: grid;
  grid-template-columns: auto minmax(120px, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.captcha-image-box {
  min-width: 168px;
  min-height: 56px;
  display: inline-grid;
  place-items: center;
  border: 1px solid #dfe5f1;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f8ff;
}

.captcha-image-box img {
  width: 168px;
  height: 56px;
  display: block;
  object-fit: cover;
}

.captcha-image-box span {
  color: #46506b;
  font-weight: 800;
}

.turnstile-box {
  min-height: 70px;
}

.site-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 4px 0;
  color: #7e86a7;
}

.site-footer a {
  min-height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #dfe5f1;
  border-radius: 6px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.74);
  color: #5371dc;
  font-weight: 800;
}

.profile-drawer {
  position: fixed;
  z-index: 70;
  inset: 0 auto 0 0;
  width: min(460px, 92vw);
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr) auto;
  gap: 16px;
  padding: 16px;
  max-height: 100vh;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.86);
  background:
    radial-gradient(circle at 18% 22%, rgba(83, 113, 220, 0.2), transparent 24%),
    linear-gradient(180deg, rgba(17, 26, 36, 0.96), rgba(13, 18, 24, 0.96));
  border-right: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 20px 0 48px rgba(7, 10, 18, 0.32);
  transform: translateX(-104%);
  transition: transform 180ms ease;
}

.profile-drawer.open {
  transform: translateX(0);
}

.drawer-mask {
  position: fixed;
  z-index: 60;
  inset: 0;
  background: rgba(14, 18, 27, 0.48);
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 42px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 800;
}

.drawer-header button {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  background: transparent;
  color: rgba(255, 255, 255, 0.68);
}

.profile-card {
  display: grid;
  grid-template-columns: 104px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
  padding: 16px 4px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-card img {
  width: 104px;
  height: 104px;
  border-radius: 8px;
  object-fit: cover;
}

.profile-card div {
  display: grid;
  gap: 7px;
  min-width: 0;
}

.profile-card strong {
  color: #ffffff;
  font-size: 18px;
}

.profile-card span,
.contribution-panel span {
  color: rgba(255, 255, 255, 0.66);
}

.search-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 44px;
  align-items: center;
  min-height: 56px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.search-box input {
  width: 100%;
  min-width: 0;
  height: 54px;
  border: 0;
  padding: 0 16px;
  background: transparent;
  color: #ffffff;
  outline: none;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.42);
}

.search-box svg {
  color: rgba(255, 255, 255, 0.56);
}

.search-results {
  max-height: 180px;
  display: grid;
  gap: 8px;
  overflow: auto;
}

.search-results button {
  display: grid;
  gap: 4px;
  border-radius: 6px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  text-align: left;
}

.search-results span {
  color: rgba(255, 255, 255, 0.58);
  font-size: 13px;
}

.drawer-links {
  display: grid;
  align-content: start;
  gap: 6px;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 4px;
  scrollbar-color: rgba(115, 188, 255, 0.7) rgba(255, 255, 255, 0.08);
  scrollbar-width: thin;
}

.drawer-links::-webkit-scrollbar {
  width: 6px;
}

.drawer-links::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
}

.drawer-links::-webkit-scrollbar-thumb {
  background: rgba(115, 188, 255, 0.7);
  border-radius: 999px;
}

.drawer-links a,
.drawer-links button {
  min-height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-radius: 6px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.82);
  text-align: left;
}

.drawer-links a:hover,
.drawer-links button:hover {
  background: rgba(255, 255, 255, 0.09);
}

.drawer-links span {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  font-weight: 800;
}

.drawer-links svg {
  width: 24px;
  height: 24px;
  padding: 4px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.12);
}

.drawer-links em {
  color: rgba(255, 255, 255, 0.52);
  font-size: 13px;
  font-style: normal;
}

.contribution-panel {
  align-self: end;
  display: grid;
  gap: 14px;
  padding: 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
}

.contribution-panel div:first-child {
  display: grid;
  gap: 4px;
}

.contribution-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-auto-flow: column;
  grid-template-rows: repeat(7, 11px);
  gap: 4px;
  overflow: hidden;
}

.contribution-grid span,
.contribution-panel i {
  display: block;
  width: 11px;
  height: 11px;
  border-radius: 3px;
}

.level-0 {
  background: rgba(83, 102, 130, 0.28);
}

.level-1 {
  background: #b9ddff;
}

.level-2 {
  background: #73bcff;
}

.level-3 {
  background: #2e86ef;
}

.level-4 {
  background: #1856c9;
}

.contribution-panel footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  font-size: 12px;
}

.quick-actions {
  position: fixed;
  z-index: 36;
  right: 24px;
  bottom: 24px;
  display: grid;
  gap: 10px;
  opacity: 0;
  pointer-events: none;
  transform: translateY(12px);
  transition:
    opacity 160ms ease,
    transform 160ms ease;
}

.quick-actions.visible {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}

.quick-actions button {
  width: 58px;
  height: 58px;
  border-radius: 999px;
}

@media (max-width: 820px) {
  .menu-trigger {
    top: 16px;
    left: 16px;
    min-width: 128px;
    height: 48px;
    font-size: 16px;
  }

  .home-trigger {
    top: 16px;
    right: 16px;
    width: 48px;
    height: 48px;
  }

  .hero-section {
    min-height: 470px;
  }

  .content-shell {
    width: min(100% - 24px, 1280px);
    padding-top: 20px;
  }

  .section-block {
    padding: 20px;
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .demo-grid,
  .archive-list a,
  .article-search-row,
  .comment-auth-form,
  .comment-auth-form.register-form,
  .profile-editor,
  .profile-fields,
  .captcha-row {
    grid-template-columns: 1fr;
  }

  .profile-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .archive-list a {
    gap: 6px;
    padding: 14px 0;
  }

  .archive-list em {
    justify-self: start;
  }

  .profile-card {
    grid-template-columns: 82px minmax(0, 1fr);
  }

  .profile-card img {
    width: 82px;
    height: 82px;
  }

  .quick-actions {
    right: 14px;
    bottom: 14px;
  }

  .quick-actions button {
    width: 52px;
    height: 52px;
  }

  .canvas-chip {
    right: 16px;
    bottom: 16px;
  }

  .canvas-reset {
    right: 68px;
    bottom: 20px;
  }

  .site-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
