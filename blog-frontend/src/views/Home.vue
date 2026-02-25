<template>
  <div class="common-layout">
    <el-container>
      <el-header class="header-bar">
        <div class="header-left">
          <h2 class="logo-text">✨ 个人博客系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown>
            <div class="user-info-cursor">
              <el-avatar v-if="currentUser.avatar" class="user-avatar" :src="currentUser.avatar"></el-avatar>
              <el-avatar v-else class="user-avatar">{{ (currentUser.nickname || currentUser.username || '匿').charAt(0) }}</el-avatar>
              
              <span class="username-text">{{ currentUser.nickname || currentUser.username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user')">🚀 个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" style="color: #F56C6C;">🚪 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container class="main-container">
        <el-aside width="240px">
          <el-card class="menu-card">
            <el-menu :default-active="currentMenu" class="clean-menu">
              <el-menu-item index="1" @click="loadBlogs">
                <el-icon><Document /></el-icon><span>全部文章</span>
              </el-menu-item>
              <el-menu-item index="2" @click="loadHotBlogs">
                <el-icon><Trophy /></el-icon><span style="color: #ff502c;">全站热门</span>
              </el-menu-item>
              <el-menu-item index="3" @click="loadRecommend">
                <el-icon><Star /></el-icon><span style="color: #67C23A;">猜你喜欢</span>
              </el-menu-item>
            </el-menu>

            <el-divider></el-divider>

            <div class="tags-section">
              <div class="tags-title">🏷️ 热门标签</div>
              <div class="tags-cloud">
                <el-tag 
                  v-for="tag in ['Java', 'Vue', 'Spring', '算法', 'MySQL', '架构', 'AI']" 
                  :key="tag" 
                  size="small" 
                  class="tag-item"
                  @click="searchKeyword=tag; handleSearch()"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-aside>

        <el-main class="content-main">
          <div class="toolbar">
            <h3 class="page-title">{{ listTitle }}</h3>
            <div class="tools">
              <el-button type="primary" @click="dialogVisible = true">➕ 发布文章</el-button>
              <el-input 
                v-model="searchKeyword" 
                placeholder="搜索标题、标签..." 
                style="width: 200px;" 
                clearable 
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              ></el-input>
              <el-button type="success" @click="handleSearch">🔍 搜索</el-button>
            </div>
          </div>

          <el-row :gutter="20">
            <el-col :span="8" v-for="blog in blogList" :key="blog.id">
              <el-card :body-style="{ padding: '0px' }" class="blog-card" shadow="hover">
                
                <img 
                  :src="blog.url || 'https://picsum.photos/400/200?random=' + blog.id" 
                  class="blog-cover"
                  @click="toDetail(blog.id)"
                />
                
                <div class="blog-info">
                  <span class="blog-title" @click="toDetail(blog.id)">{{ blog.title }}</span>
                  
                  <div class="blog-tags">
                    <el-tag 
                      v-for="tag in (blog.tags ? blog.tags.split(',') : [])" 
                      :key="tag" 
                      size="small" 
                      effect="plain" 
                      type="info"
                      style="margin-right: 5px;"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>

                  <div class="blog-summary" @click="toDetail(blog.id)">
                    {{ blog.summary || '暂无摘要' }}
                  </div>
                  
                  <div class="blog-footer">
                    <div class="footer-stats">
                      <span>🔥 {{ blog.views || 0 }}</span>
                      <span v-if="blog.score > 0" style="color: #E6A23C; margin-left: 10px;">
                        ⭐ {{ blog.score }}
                      </span>
                    </div>
                    <div>
                      <el-button type="primary" link @click="toDetail(blog.id)">详情</el-button>
                      <el-button v-if="canDelete(blog)" type="danger" link @click="handleDelete(blog.id)">删除</el-button>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-empty v-if="blogList.length === 0" description="暂无相关文章，去发布一篇吧！"></el-empty>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog v-model="dialogVisible" title="发布新文章" width="50%">
        <el-form :model="blogForm" label-width="80px">
            <el-form-item label="文章标题">
              <el-input v-model="blogForm.title" placeholder="请输入标题"></el-input>
            </el-form-item>
            
            <el-form-item label="分类标签">
              <el-select
                v-model="blogForm.tags"
                multiple
                filterable
                allow-create
                default-first-option
                :reserve-keyword="false"
                placeholder="输入标签并回车（首个标签为主分类）"
                style="width: 100%"
              >
                <el-option value="Java" label="Java" />
                <el-option value="Vue" label="Vue" />
                <el-option value="Spring" label="Spring" />
                <el-option value="Python" label="Python" />
                <el-option value="MySQL" label="MySQL" />
                <el-option value="算法" label="算法" />
                <el-option value="面试" label="面试" />
                <el-option value="生活" label="生活" />
              </el-select>
            </el-form-item>

            <el-form-item label="封面图片">
              <el-upload 
                action="http://localhost:8080/api/upload" 
                :show-file-list="false" 
                :on-success="(res)=>{blogForm.url=res;ElMessage.success('封面上传成功')}" 
                class="avatar-uploader"
              >
                <img v-if="blogForm.url" :src="blogForm.url" class="avatar"/>
                <el-icon v-else class="avatar-uploader-icon"><Plus/></el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="文章正文">
              <el-input type="textarea" :rows="8" v-model="blogForm.content" placeholder="支持 Markdown 语法..."></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible=false">取 消</el-button>
          <el-button type="primary" @click="submitBlog" :loading="isSubmitting">发 布</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Plus, ArrowDown, Document, Trophy, Search, Star } from '@element-plus/icons-vue'

const router = useRouter()
const listTitle = ref('全部文章')
const currentMenu = ref('1')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isSubmitting = ref(false)

const userStore = localStorage.getItem('user')
const currentUser = ref(userStore ? JSON.parse(userStore) : {})

const blogList = ref([])

const blogForm = reactive({ 
  title: '', 
  tags: [], 
  content: '', 
  author: currentUser.value.nickname || currentUser.value.username, 
  url: '' 
})

// 权限判断
const canDelete = (blog) => {
  if (!currentUser.value.username) return false
  return currentUser.value.username === blog.author || currentUser.value.nickname === blog.author
}

const loadBlogs = async () => { 
  try {
    const res = await axios.get('http://localhost:8080/api/blog/all')
    blogList.value = res.data
    listTitle.value = '全部文章'
    currentMenu.value = '1'
  } catch (e) {
    ElMessage.error('获取文章列表失败')
  }
}

const loadHotBlogs = async () => { 
  try {
    const res = await axios.get('http://localhost:8080/api/blog/hot')
    blogList.value = res.data
    listTitle.value = '全站热门榜单'
    currentMenu.value = '2' 
  } catch (e) {
    ElMessage.error('获取热门失败')
  }
}

// ✨✨✨ 智能推荐入口 ✨✨✨
const loadRecommend = async () => {
  if (!currentUser.value.id) {
    ElMessage.warning('请登录后查看个性化推荐')
    return
  }
  try {
    const res = await axios.get(`http://localhost:8080/api/blog/recommend?userId=${currentUser.value.id}`)
    blogList.value = res.data
    listTitle.value = '猜你喜欢 (基于您的阅读兴趣)'
    currentMenu.value = '3'
  } catch (e) {
    ElMessage.error('获取推荐数据失败')
  }
}

const handleSearch = async () => { 
  if(!searchKeyword.value) return loadBlogs()
  try {
    const res = await axios.get('http://localhost:8080/api/blog/search', { params: { keyword: searchKeyword.value } })
    blogList.value = res.data
    listTitle.value = `搜索结果: "${searchKeyword.value}"`
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

const submitBlog = async () => {
  if (!blogForm.title || !blogForm.content) return ElMessage.warning('标题和正文不能为空')
  if (blogForm.tags.length === 0) return ElMessage.warning('请至少输入一个标签') 

  isSubmitting.value = true
  
  const derivedCategory = blogForm.tags[0]

  const submitData = { 
    ...blogForm, 
    category: derivedCategory, 
    tags: blogForm.tags.join(','), 
    author: currentUser.value.nickname || currentUser.value.username 
  }

  try { 
    await axios.post('http://localhost:8080/api/blog/add', submitData)
    ElMessage.success('发布成功！')
    dialogVisible.value = false
    loadBlogs()
    // 重置表单
    blogForm.title = ''
    blogForm.content = ''
    blogForm.url = ''
    blogForm.tags = []
  } catch(e) {
    ElMessage.error('发布失败，请检查网络')
  } finally {
    isSubmitting.value = false
  } 
}

const toDetail = (id) => {
  if (!id) return
  router.push(`/blog/${id}`)
}

const handleDelete = (id) => { 
  ElMessageBox.confirm('确定要删除这篇文章吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async()=>{
    await axios.delete(`http://localhost:8080/api/blog/delete/${id}`)
    loadBlogs()
    ElMessage.success('已删除')
  }) 
}

const handleLogout = () => { 
  localStorage.removeItem('user')
  router.push('/login') 
  ElMessage.success('已安全退出')
}

onMounted(() => {
  loadBlogs()
})
</script>

<style scoped>
/* 布局样式 */
.header-bar { background-color: #fff; border-bottom: 1px solid #ddd; position: sticky; top: 0; z-index: 1000; display: flex; align-items: center; justify-content: space-between; padding: 0 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.header-left h2 { margin: 0; color: #409EFF; font-size: 24px; font-weight: 700; letter-spacing: 1px; }
.user-info-cursor { display: flex; align-items: center; cursor: pointer; padding: 5px 10px; border-radius: 4px; transition: 0.3s; }
.user-info-cursor:hover { background-color: #f5f7fa; }
.user-avatar { background-color: #409eff; margin-right: 8px; }
.username-text { font-weight: bold; color: #333; }

.main-container { width: 1200px; margin: 20px auto; gap: 20px; }
.menu-card { border: none; position: sticky; top: 80px; }
.clean-menu { border: none; }
.clean-menu :deep(.el-menu-item.is-active) { background-color: #ecf5ff; border-right: 3px solid #409eff; color: #409eff; font-weight: bold; }

.tags-section { padding: 0 20px; margin-top: 20px; }
.tags-title { font-size: 14px; color: #999; margin-bottom: 12px; font-weight: bold; }
.tags-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-item { cursor: pointer; transition: 0.2s; }
.tag-item:hover { transform: translateY(-2px); }

.content-main { padding: 0; overflow: visible; }
.toolbar { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; background: #fff; padding: 15px 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
.page-title { margin: 0; font-size: 18px; color: #333; border-left: 4px solid #409eff; padding-left: 10px; }
.tools { display: flex; gap: 10px; }

/* 博客卡片样式 */
.blog-card { margin-bottom: 20px; height: 380px; display: flex; flex-direction: column; transition: transform 0.3s, box-shadow 0.3s; border-radius: 8px; border: none; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.blog-card:hover { transform: translateY(-5px); box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
.blog-cover { height: 160px; width: 100%; object-fit: cover; cursor: pointer; }
.blog-info { padding: 16px; flex: 1; display: flex; flex-direction: column; }
.blog-title { font-weight: bold; font-size: 18px; color: #333; margin-bottom: 10px; cursor: pointer; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.blog-title:hover { color: #409eff; }
.blog-tags { margin-bottom: 10px; height: 24px; overflow: hidden; }
.blog-summary { font-size: 13px; color: #666; line-height: 1.6; height: 42px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; cursor: pointer; margin-bottom: 15px; }
.blog-footer { margin-top: auto; display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #999; border-top: 1px solid #f0f0f0; padding-top: 10px; }
.footer-stats { display: flex; align-items: center; }

/* 上传样式 */
.avatar-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center; transition: 0.2s; }
.avatar-uploader:hover { border-color: #409EFF; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; }
.avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
</style>