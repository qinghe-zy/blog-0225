<template>
  <div class="common-layout">
    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #ddd; position: sticky; top: 0; z-index: 1000; display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
        <div style="display: flex; align-items: center; gap: 10px;">
          <h2 style="margin: 0; color: #333; font-size: 22px;">✨ 智能博客系统</h2>
        </div>
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-dropdown>
            <div style="display: flex; align-items: center; cursor: pointer;">
              <el-avatar style="background-color: #409eff; margin-right: 5px;">{{ (currentUser.nickname || currentUser.username || '匿').charAt(0) }}</el-avatar>
              <span style="font-weight: bold;">{{ currentUser.nickname || currentUser.username }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user')">🚀 个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" style="color: red;">🚪 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container style="width: 1200px; margin: 20px auto;">
        <el-aside width="240px">
          <el-card style="border: none; position: sticky; top: 80px;">
            <el-menu :default-active="currentMenu" style="border: none;">
              <el-menu-item index="1" @click="loadBlogs">
                <el-icon><Document /></el-icon><span>全部文章</span>
              </el-menu-item>
              <el-menu-item index="2" @click="loadHotBlogs">
                <el-icon><Trophy /></el-icon><span style="color: #ff502c;">全站热门</span>
              </el-menu-item>
            </el-menu>

            <el-divider></el-divider>

            <div style="padding: 0 20px;">
              <div style="font-size: 14px; color: #999; margin-bottom: 10px;">🏷️ 热门标签</div>
              <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                <el-tag 
                  v-for="tag in ['Java', 'Vue', 'Spring', '算法', 'MySQL', '架构', 'AI']" 
                  :key="tag" 
                  size="small" 
                  style="cursor: pointer;"
                  @click="searchKeyword=tag; handleSearch()"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-aside>

        <el-main style="padding: 0 20px; overflow: visible;">
          <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
            <h3 style="margin: 0;">{{ listTitle }}</h3>
            <div style="display: flex; gap: 10px;">
              <el-button type="primary" @click="dialogVisible = true">➕ 发布博客</el-button>
              <el-input v-model="searchKeyword" placeholder="搜标题、标签..." style="width: 200px;" clearable @clear="handleSearch"></el-input>
              <el-button type="success" @click="handleSearch">🔍</el-button>
            </div>
          </div>

          <el-row :gutter="20">
            <el-col :span="8" v-for="blog in blogList" :key="blog.id">
              <el-card :body-style="{ padding: '0px' }" style="margin-bottom: 20px; height: 380px; display: flex; flex-direction: column;" shadow="hover">
                
                <img 
                  :src="blog.url || 'https://picsum.photos/400/200?random=' + blog.id" 
                  style="height: 160px; width: 100%; object-fit: cover; cursor: pointer;"
                  @click="toDetail(blog.id)"
                />
                
                <div style="padding: 14px; flex: 1; display: flex; flex-direction: column;">
                  <span 
                    style="font-weight: bold; font-size: 16px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer; margin-bottom: 8px;"
                    @click="toDetail(blog.id)"
                  >
                    {{ blog.title }}
                  </span>
                  
                  <div style="margin-bottom: 8px; height: 24px; overflow: hidden;">
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

                  <div 
                    style="font-size: 13px; color: #888; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; cursor: pointer;"
                    @click="toDetail(blog.id)"
                  >
                    {{ blog.summary || '暂无摘要' }}
                  </div>
                  
                  <div style="margin-top: auto; display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #999;">
                    <span>🔥 {{ blog.views || 0 }} 阅读</span>
                    <div>
                      <el-button type="primary" link @click="toDetail(blog.id)">详情</el-button>
                      
                      <el-button 
                        v-if="canDelete(blog)" 
                        type="danger" 
                        link 
                        @click="handleDelete(blog.id)"
                      >删除</el-button>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="blogList.length === 0" description="这里空空如也，快去发布第一篇文章吧！"></el-empty>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog v-model="dialogVisible" title="发布新文章" width="50%">
        <el-form :model="blogForm" label-width="80px">
            <el-form-item label="标题">
              <el-input v-model="blogForm.title" placeholder="请输入标题"></el-input>
            </el-form-item>
            
            <el-form-item label="分类">
              <el-radio-group v-model="blogForm.category">
                <el-radio label="技术">技术</el-radio>
                <el-radio label="生活">生活</el-radio>
                <el-radio label="面试">面试</el-radio>
                <el-radio label="职场">职场</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="标签">
              <el-select
                v-model="blogForm.tags"
                multiple
                filterable
                allow-create
                default-first-option
                :reserve-keyword="false"
                placeholder="可以直接输入新标签并回车"
                style="width: 100%"
              >
                <el-option value="Java" label="Java" />
                <el-option value="Vue" label="Vue" />
                <el-option value="Spring" label="Spring" />
                <el-option value="Python" label="Python" />
                <el-option value="MySQL" label="MySQL" />
                <el-option value="算法" label="算法" />
                <el-option value="面试" label="面试" />
              </el-select>
            </el-form-item>

            <el-form-item label="封面">
              <el-upload 
                action="http://localhost:8080/api/upload" 
                :show-file-list="false" 
                :on-success="(res)=>{blogForm.url=res;ElMessage.success('上传成功')}" 
                style="border: 1px dashed #d9d9d9; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center; cursor: pointer;"
              >
                <img v-if="blogForm.url" :src="blogForm.url" style="width: 100%; height: 100%; object-fit: cover; display: block;"/>
                <el-icon v-else :size="28" color="#8c939d"><Plus/></el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="正文">
              <el-input type="textarea" :rows="8" v-model="blogForm.content" placeholder="支持 Markdown 语法..."></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible=false">取消</el-button>
          <el-button type="primary" @click="submitBlog" :loading="isSubmitting">发布</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Plus, ArrowDown, Document, Trophy, Search } from '@element-plus/icons-vue'

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
  category: '技术', 
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
  } catch (e) {
    ElMessage.error('获取文章失败')
  }
}

const loadHotBlogs = async () => { 
  try {
    const res = await axios.get('http://localhost:8080/api/blog/hot')
    blogList.value = res.data
    listTitle.value = '全站热门'
    currentMenu.value = '2' 
  } catch (e) {
    ElMessage.error('获取热门失败')
  }
}

const handleSearch = async () => { 
  if(!searchKeyword.value) return loadBlogs()
  try {
    const res = await axios.get('http://localhost:8080/api/blog/search', { params: { keyword: searchKeyword.value } })
    blogList.value = res.data
    listTitle.value = `搜索: ${searchKeyword.value}`
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

const submitBlog = async () => {
  if (!blogForm.title || !blogForm.content) return ElMessage.warning('请填写标题和正文')
  isSubmitting.value = true
  const submitData = { ...blogForm, tags: blogForm.tags.join(','), author: currentUser.value.nickname || currentUser.value.username }
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
    ElMessage.error('发布失败，请检查后端服务')
  } finally {
    isSubmitting.value = false
  } 
}

const toDetail = (id) => {
  console.log('跳转ID:', id)
  if (!id) {
    ElMessage.error('文章ID无效')
    return
  }
  router.push(`/blog/${id}`)
}

const handleDelete = (id) => { 
  ElMessageBox.confirm('确定删除这篇文章吗？').then(async()=>{
    await axios.delete(`http://localhost:8080/api/blog/delete/${id}`)
    loadBlogs()
    ElMessage.success('已删除')
  }) 
}

const handleLogout = () => { 
  localStorage.removeItem('user')
  router.push('/login') 
}

onMounted(() => {
  loadBlogs()
})
</script>

<style scoped>
/* 去除所有复杂特效，只保留简单的颜色变化 */
</style>