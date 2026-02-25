<template>
  <div class="detail-container">
    <el-card class="detail-card">
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <el-button @click="$router.push('/home')">⬅️ 返回列表</el-button>
        <el-button v-if="isAuthor" type="primary" @click="handleEdit">✏️ 编辑文章</el-button>
      </div>

      <h1 class="title">{{ blog.title }}</h1>
      <div class="meta-info">
        <span>👤 {{ blog.author }}</span>
        <span>🔥 阅读：{{ blog.views }}</span>
        <span>🕒 {{ blog.createTime }}</span>
      </div>

      <div class="action-bar">
        <div class="action-item" @click="handleLike">
          <el-icon :size="20" :color="isLiked ? '#F56C6C' : '#999'">
            <StarFilled v-if="isLiked" /><Star v-else />
          </el-icon>
          <span>{{ blog.likes || 0 }}</span>
        </div>
        <div class="action-item" @click="toggleAction(1)">
          <el-icon :size="20" :color="status.isCollected ? '#E6A23C' : '#999'">
            <CollectionTag />
          </el-icon>
          <span>{{ status.isCollected ? '已收藏' : '收藏' }}</span>
        </div>
        <div class="action-item" @click="toggleAction(2)">
          <el-icon :size="20" :color="status.isToRead ? '#67C23A' : '#999'">
            <Timer />
          </el-icon>
          <span>{{ status.isToRead ? '已待读' : '待读' }}</span>
        </div>
        <div class="action-item" @click="toggleAction(3)">
          <el-icon :size="20" :color="status.isBlocked ? '#333' : '#999'">
            <CircleClose />
          </el-icon>
          <span>不感兴趣</span>
        </div>
      </div>
      
      <el-divider></el-divider>

      <div class="markdown-body" v-html="md.render(blog.content || '')"></div>

      <el-divider></el-divider>

      <div class="comment-section">
        <h3>💬 评论区</h3>
        
        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
          <el-input 
            v-model="newComment" 
            placeholder="写下你的想法..." 
            @keyup.enter="submitComment"
          ></el-input>
          <el-button type="primary" @click="submitComment">发送</el-button>
        </div>

        <div v-if="comments.length > 0">
          <div v-for="item in comments" :key="item.id" style="border-bottom: 1px solid #eee; padding: 10px 0;">
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
              <el-avatar :size="30" style="background-color: #66ccff;">{{ (item.username || '匿').charAt(0) }}</el-avatar>
              <span style="font-weight: bold; font-size: 14px; color: #333;">{{ item.username }}</span>
              <span style="font-size: 12px; color: #999;">{{ item.createTime }}</span>
            </div>
            <div style="padding-left: 40px; color: #666;">
              {{ item.content }}
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，快来抢沙发！"></el-empty>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="修改博客" width="50%">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title"></el-input></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category">
            <el-option label="技术" value="技术"></el-option>
            <el-option label="生活" value="生活"></el-option>
            <el-option label="感悟" value="感悟"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="正文"><el-input type="textarea" :rows="10" v-model="editForm.content"></el-input></el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">💾 保存修改</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
// ✨ 1. 引入 onBeforeUnmount
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import MarkdownIt from 'markdown-it'
// 引入图标
import { Star, StarFilled, CollectionTag, Timer, CircleClose } from '@element-plus/icons-vue'

const route = useRoute()
const md = new MarkdownIt()
const blog = ref({}) 
const comments = ref([]) 
const newComment = ref('') 
const userStore = localStorage.getItem('user')
const currentUser = userStore ? JSON.parse(userStore) : null

// ✨ 2. 记录进入页面的时间
let enterTime = Date.now()

// 状态管理
const isLiked = ref(false)
const status = reactive({
  isCollected: false,
  isToRead: false,
  isBlocked: false
})

// 计算属性：是否是作者本人
const isAuthor = computed(() => {
  if (!currentUser || !blog.value.author) return false
  return currentUser.username === blog.value.author || currentUser.nickname === blog.value.author
})

// ✨ 3. 核心：页面销毁/跳转时上报阅读时长
onBeforeUnmount(() => {
  // 如果没登录，或者博客还没加载出来，就不记录
  if (!currentUser || !blog.value.id) return

  const leaveTime = Date.now()
  // 计算停留时长 (秒)
  const duration = Math.floor((leaveTime - enterTime) / 1000)

  // 只有阅读超过 2 秒才视为有效阅读，避免误点
  if (duration > 2) {
    // 使用 FormData 发送数据，对应后端的 @RequestParam
    const formData = new FormData()
    formData.append('userId', currentUser.id)
    formData.append('blogId', blog.value.id)
    formData.append('seconds', duration)

    // 发送请求 (异步发送，不阻塞页面关闭)
    axios.post('http://localhost:8080/api/blog/duration', formData)
      .then(() => console.log(`已上报阅读时长: ${duration}秒`))
      .catch(err => console.error('时长上报失败', err))
  }
})

// 统一的加载入口
onMounted(async () => {
  // 先加载详情
  await loadDetail()
  // 再加载评论
  loadComments()
  // 最后检查状态
  if (currentUser) {
    checkAllStatus(route.params.id)
  }
})

// 加载详情
const loadDetail = async () => {
  const blogId = route.params.id
  try {
    const res = await axios.get(`http://localhost:8080/api/blog/detail/${blogId}`, {
      params: { userId: currentUser ? currentUser.id : null }
    })
    blog.value = res.data
  } catch (e) {
    console.error('获取详情失败', e)
  }
}

// 检查交互状态
const checkAllStatus = async (blogId) => {
  const userId = currentUser.id
  
  // 点赞
  const likeRes = await axios.get('http://localhost:8080/api/blog/checkLike', { params: { blogId, userId } })
  isLiked.value = likeRes.data

  // 收藏/待读/拉黑
  const s1 = await axios.get('http://localhost:8080/api/action/check', { params: { blogId, userId, type: 1 } })
  status.isCollected = s1.data
  
  const s2 = await axios.get('http://localhost:8080/api/action/check', { params: { blogId, userId, type: 2 } })
  status.isToRead = s2.data
  
  const s3 = await axios.get('http://localhost:8080/api/action/check', { params: { blogId, userId, type: 3 } })
  status.isBlocked = s3.data
}

// 点赞逻辑
const handleLike = async () => {
  if (!currentUser) return ElMessage.warning('请先登录')
  const res = await axios.post(`http://localhost:8080/api/blog/like?blogId=${blog.value.id}&userId=${currentUser.id}`)
  
  if (res.data === '点赞成功') {
    isLiked.value = true
    blog.value.likes = (blog.value.likes || 0) + 1
    ElMessage.success('点赞成功')
  } else {
    isLiked.value = false
    blog.value.likes = (blog.value.likes || 0) - 1
    ElMessage.info('取消点赞')
  }
}

// 通用动作逻辑 (收藏、待读、拉黑)
const toggleAction = async (type) => {
  if (!currentUser) return ElMessage.warning('请先登录')
  
  try {
    await axios.post(`http://localhost:8080/api/action/toggle?blogId=${blog.value.id}&userId=${currentUser.id}&type=${type}`)
    
    if (type === 1) {
      status.isCollected = !status.isCollected
      blog.value.collects = status.isCollected ? (blog.value.collects + 1) : (blog.value.collects - 1)
      ElMessage.success(status.isCollected ? '已收藏' : '取消收藏')
    } 
    else if (type === 2) {
      status.isToRead = !status.isToRead
      ElMessage.success(status.isToRead ? '已加入待读' : '移出待读')
    }
    else if (type === 3) {
      status.isBlocked = !status.isBlocked
      if(status.isBlocked) ElMessage.info('已拉黑，将减少推荐')
    }
  } catch(e) {
    ElMessage.error('操作失败')
  }
}

// 评论逻辑
const loadComments = async () => {
  const res = await axios.get(`http://localhost:8080/api/comment/list/${route.params.id}`)
  comments.value = res.data
}

const submitComment = async () => {
  if (!currentUser) return ElMessage.warning('请先登录')
  if (!newComment.value.trim()) return ElMessage.warning('内容不能为空')
  
  await axios.post('http://localhost:8080/api/comment/add', {
    content: newComment.value,
    userId: currentUser.id,
    username: currentUser.nickname || currentUser.username,
    blogId: route.params.id
  })
  ElMessage.success('评论成功')
  newComment.value = ''
  loadComments()
}

// 编辑逻辑
const editDialogVisible = ref(false)
const editForm = reactive({})

const handleEdit = () => {
  Object.assign(editForm, blog.value)
  editDialogVisible.value = true
}

const submitEdit = async () => {
  const res = await axios.put('http://localhost:8080/api/blog/update', editForm)
  if (res.data === '修改成功！') {
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadDetail()
  }
}
</script>

<style scoped>
.detail-container { padding: 20px; display: flex; justify-content: center; background-color: #f5f7fa; min-height: 100vh; }
.detail-card { width: 900px; padding: 20px; }
.title { text-align: center; font-size: 28px; color: #333; margin-bottom: 10px; }
.meta-info { text-align: center; color: #999; margin-bottom: 20px; font-size: 14px; display: flex; justify-content: center; gap: 20px; }

/* 核心操作区样式 */
.action-bar { display: flex; justify-content: center; gap: 40px; margin: 30px 0; padding: 10px; background: #fafafa; border-radius: 8px; }
.action-item { display: flex; flex-direction: column; align-items: center; cursor: pointer; gap: 5px; color: #666; font-size: 12px; transition: 0.2s; }
.action-item:hover { transform: scale(1.1); color: #409eff; }

/* Markdown 样式 */
:deep(.markdown-body p) { margin-bottom: 16px; line-height: 1.8; }
:deep(.markdown-body h1) { font-size: 24px; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; margin-top: 24px; }
:deep(.markdown-body blockquote) { color: #666; border-left: 4px solid #dfe2e5; padding-left: 10px; }
:deep(.markdown-body code) { background-color: #fff5f5; color: #ff502c; padding: 2px 4px; border-radius: 4px; }
:deep(.markdown-body pre) { background-color: #f6f8fa; padding: 16px; border-radius: 8px; overflow: auto; }
</style>