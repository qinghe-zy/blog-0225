<template>
  <div class="detail-container">
    <el-card class="detail-card">
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <el-button @click="goBack">⬅️ 返回上一页</el-button>
        <el-button v-if="isAuthor" type="primary" @click="handleEdit">✏️ 编辑文章</el-button>
      </div>

      <h1 class="title">{{ blog.title }}</h1>
      <div class="meta-info">
        <span>👤 {{ blog.author }}</span>
        <span>🔥 阅读：{{ blog.views }}</span>
        <span v-if="blog.score > 0">⭐ 评分：{{ blog.score }}</span>
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

      <div v-if="relatedBlogs.length > 0" style="margin-bottom: 30px;">
        <h3 style="margin-bottom: 15px;">📚 猜你喜欢 (相关推荐)</h3>
        <el-row :gutter="15">
          <el-col :span="8" v-for="item in relatedBlogs" :key="item.id">
            <el-card shadow="hover" :body-style="{ padding: '10px' }" style="cursor: pointer;" @click="toRelated(item.id)">
              <div style="font-weight: bold; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">{{ item.title }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 5px;">
                🔥 {{ item.views }} 阅读 · 🏷️ {{ item.tags }}
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="comment-section">
        <h3>💬 评论区</h3>
        
        <div style="margin-bottom: 10px; display: flex; align-items: center; gap: 10px;">
          <span style="font-size: 14px; color: #666;">点击星星直接评分:</span>
          <el-rate v-model="newScore" allow-half show-text @change="handleRateChange"></el-rate>
        </div>

        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
          <el-input ref="commentInput" v-model="newComment" placeholder="写下你的想法..." @keyup.enter="submitComment"></el-input>
          <el-button type="primary" @click="submitComment">发送评论</el-button>
        </div>

        <div v-if="comments.length > 0">
          <div v-for="item in comments" :key="item.id" style="border-bottom: 1px solid #eee; padding: 10px 0;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
                <el-avatar v-if="item.avatar" :size="30" :src="item.avatar"></el-avatar>
                <el-avatar v-else :size="30" style="background-color: #66ccff;">{{ (item.username || '匿').charAt(0) }}</el-avatar>
                <span style="font-weight: bold; font-size: 14px; color: #333;">{{ item.username }}</span>
                <el-rate v-if="item.score" v-model="item.score" disabled size="small"></el-rate>
                <span style="font-size: 12px; color: #999;">{{ item.createTime }}</span>
              </div>
              <div>
                 <el-button type="primary" link size="small" @click="handleReply(item.username)">回复</el-button>
                 <el-button v-if="currentUser && currentUser.id === item.userId" type="danger" link size="small" @click="handleDeleteComment(item.id)">删除</el-button>
              </div>
            </div>
            <div style="padding-left: 40px; color: #666; line-height: 1.6;">{{ item.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，快来抢沙发！"></el-empty>
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="修改博客" width="50%">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title"></el-input></el-form-item>
        <el-form-item label="分类/标签">
          <el-select v-model="editForm.tagsArray" multiple filterable allow-create default-first-option placeholder="第一个标签将作为主分类" style="width: 100%">
            <el-option value="Java" label="Java" /><el-option value="Vue" label="Vue" /><el-option value="Spring" label="Spring" />
            <el-option value="Python" label="Python" /><el-option value="MySQL" label="MySQL" />
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
import { ref, reactive, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import MarkdownIt from 'markdown-it'
import { Star, StarFilled, CollectionTag, Timer, CircleClose } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const md = new MarkdownIt()
const blog = ref({}) 
const comments = ref([]) 
const newComment = ref('')
const newScore = ref(0) 
const userStore = localStorage.getItem('user')
const currentUser = userStore ? JSON.parse(userStore) : null
const commentInput = ref(null) 

const relatedBlogs = ref([])
let enterTime = Date.now()

const isLiked = ref(false)
const status = reactive({ isCollected: false, isToRead: false, isBlocked: false })

const isAuthor = computed(() => {
  if (!currentUser || !blog.value.author) return false
  return currentUser.username === blog.value.author || currentUser.nickname === blog.value.author
})

const goBack = () => { if (window.history.length > 1) { router.back() } else { router.push('/home') } }

onBeforeUnmount(() => {
  if (!currentUser || !blog.value.id) return
  const leaveTime = Date.now()
  const duration = Math.floor((leaveTime - enterTime) / 1000)
  if (duration > 2) {
    const formData = new FormData()
    formData.append('userId', currentUser.id)
    formData.append('blogId', blog.value.id)
    formData.append('seconds', duration)
    navigator.sendBeacon('http://localhost:8080/api/blog/duration', formData)
  }
})

watch(() => route.params.id, (newId) => { if (newId) { enterTime = Date.now(); initPage(newId); window.scrollTo(0, 0) } })

const initPage = async (id) => {
  await loadDetail(id)
  loadComments(id)
  loadRelated(id)
  if (currentUser) {
    checkAllStatus(id)
    loadUserScore(id) 
  }
}

onMounted(() => { initPage(route.params.id) })

// ✨ 修复：适配 Result 结构
const loadDetail = async (id) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/blog/detail/${id}`, { params: { userId: currentUser ? currentUser.id : null } })
    if (res.data.code === 200) {
        blog.value = res.data.data
    }
  } catch (e) { console.error('获取详情失败', e) }
}

const loadRelated = async (id) => {
  try {
    const res = await axios.get(`http://localhost:8080/api/blog/related/${id}`)
    if (res.data.code === 200) relatedBlogs.value = res.data.data
  } catch (e) {}
}

const toRelated = (id) => { router.push(`/blog/${id}`) }

// ✨ 修复：适配 Result 结构
const checkAllStatus = async (blogId) => {
  const userId = currentUser.id
  const likeRes = await axios.get('http://localhost:8080/api/blog/checkLike', { params: { blogId, userId } })
  if (likeRes.data.code === 200) isLiked.value = likeRes.data.data

  const s1 = await axios.get('http://localhost:8080/api/action/check', { params: { blogId, userId, type: 1 } })
  if (s1.data.code === 200) status.isCollected = s1.data.data
  
  const s2 = await axios.get('http://localhost:8080/api/action/check', { params: { blogId, userId, type: 2 } })
  if (s2.data.code === 200) status.isToRead = s2.data.data
  
  const s3 = await axios.get('http://localhost:8080/api/action/check', { params: { blogId, userId, type: 3 } })
  if (s3.data.code === 200) status.isBlocked = s3.data.data
}

const loadUserScore = async (blogId) => {
  try {
    const res = await axios.get('http://localhost:8080/api/comment/getScore', { params: { userId: currentUser.id, blogId: blogId } })
    if (res.data.code === 200) newScore.value = res.data.data || 0
  } catch (e) { console.error('加载评分失败', e) }
}

// ✨✨✨ 修复核心Bug：防止 undefined ID 发送请求 ✨✨✨
const handleLike = async () => {
  if (!currentUser) return ElMessage.warning('请先登录')
  if (!blog.value || !blog.value.id) return // 关键修复：防止报错 "NumberFormatException: For input string: undefined"
  
  const res = await axios.post(`http://localhost:8080/api/blog/like?blogId=${blog.value.id}&userId=${currentUser.id}`)
  
  if (res.data.code === 200) {
    // 根据后端返回判断是成功还是取消
    const msg = res.data.msg || res.data.data
    // 如果返回 "点赞成功"，前端 +1；如果 "取消成功" -1；
    // 为防止前端计算错误，直接刷新数据更安全，但这里为了体验先做乐观更新
    if (msg.includes('点赞成功') || msg.includes('操作成功')) {
        isLiked.value = true
        blog.value.likes = (blog.value.likes || 0) + 1
        ElMessage.success('点赞成功')
    } else {
        isLiked.value = false
        // 防止前端显示负数
        blog.value.likes = Math.max(0, (blog.value.likes || 0) - 1)
        ElMessage.info('取消点赞')
    }
  }
}

const toggleAction = async (type) => {
  if (!currentUser) return ElMessage.warning('请先登录')
  try {
    const res = await axios.post(`http://localhost:8080/api/action/toggle?blogId=${blog.value.id}&userId=${currentUser.id}&type=${type}`)
    if (res.data.code === 200) {
        if (type === 1) {
            status.isCollected = !status.isCollected
            blog.value.collects = status.isCollected ? (blog.value.collects + 1) : Math.max(0, blog.value.collects - 1)
            ElMessage.success(status.isCollected ? '已收藏' : '取消收藏')
        } else if (type === 2) {
            status.isToRead = !status.isToRead
            ElMessage.success(status.isToRead ? '已加入待读' : '移出待读')
        } else if (type === 3) {
            status.isBlocked = !status.isBlocked
            if(status.isBlocked) ElMessage.info('已拉黑')
        }
    }
  } catch(e) { ElMessage.error('操作失败') }
}

const loadComments = async (id) => {
  const res = await axios.get(`http://localhost:8080/api/comment/list/${id}`)
  if (res.data.code === 200) comments.value = res.data.data
}

const handleRateChange = async (val) => {
  if (!currentUser) { newScore.value = 0; return ElMessage.warning('请先登录') }
  if (val === 0) return
  await axios.post('http://localhost:8080/api/comment/add', {
    content: null, userId: currentUser.id, username: currentUser.nickname || currentUser.username,
    avatar: currentUser.avatar, blogId: route.params.id, score: val
  })
  ElMessage.success('评分已提交')
  loadDetail(route.params.id) 
}

const submitComment = async () => {
  if (!currentUser) return ElMessage.warning('请先登录')
  if (!newComment.value.trim()) return ElMessage.warning('内容不能为空')
  await axios.post('http://localhost:8080/api/comment/add', {
    content: newComment.value, userId: currentUser.id, username: currentUser.nickname || currentUser.username,
    avatar: currentUser.avatar, blogId: route.params.id, score: 0 
  })
  ElMessage.success('评论成功')
  newComment.value = ''
  loadComments(route.params.id)
}

const handleReply = (targetUsername) => {
  if (!currentUser) return ElMessage.warning('请先登录')
  newComment.value = `回复 @${targetUsername}: `
  commentInput.value.focus()
}

const handleDeleteComment = (commentId) => {
  ElMessageBox.confirm('确定要删除这条评论吗？').then(async () => {
    try {
      const res = await axios.delete(`http://localhost:8080/api/comment/delete/${commentId}`, { params: { userId: currentUser.id } })
      if (res.data.code === 200) {
          ElMessage.success('已删除')
          loadComments(route.params.id)
          loadDetail(route.params.id)
      } else { ElMessage.error(res.data.msg) }
    } catch (e) { ElMessage.error('删除失败') }
  })
}

const editDialogVisible = ref(false)
const editForm = reactive({ title: '', content: '', tagsArray: [] })

const handleEdit = () => {
  Object.assign(editForm, blog.value)
  editForm.tagsArray = blog.value.tags ? blog.value.tags.split(',') : []
  editDialogVisible.value = true
}

const submitEdit = async () => {
  const tagsString = editForm.tagsArray.join(',')
  const updateData = {
    id: editForm.id, title: editForm.title, content: editForm.content,
    tags: tagsString, category: editForm.tagsArray[0] || '默认', 
    url: blog.value.url, summary: blog.value.summary
  }
  const res = await axios.put('http://localhost:8080/api/blog/update', updateData)
  if (res.data.code === 200) {
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadDetail(route.params.id) 
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.detail-container { padding: 20px; display: flex; justify-content: center; background-color: #f5f7fa; min-height: 100vh; }
.detail-card { width: 900px; padding: 20px; }
.title { text-align: center; font-size: 28px; color: #333; margin-bottom: 10px; }
.meta-info { text-align: center; color: #999; margin-bottom: 20px; font-size: 14px; display: flex; justify-content: center; gap: 20px; }
.action-bar { display: flex; justify-content: center; gap: 40px; margin: 30px 0; padding: 10px; background: #fafafa; border-radius: 8px; }
.action-item { display: flex; flex-direction: column; align-items: center; cursor: pointer; gap: 5px; color: #666; font-size: 12px; transition: 0.2s; }
.action-item:hover { transform: scale(1.1); color: #409eff; }
:deep(.markdown-body p) { margin-bottom: 16px; line-height: 1.8; }
:deep(.markdown-body h1) { font-size: 24px; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; margin-top: 24px; }
:deep(.markdown-body blockquote) { color: #666; border-left: 4px solid #dfe2e5; padding-left: 10px; }
:deep(.markdown-body code) { background-color: #fff5f5; color: #ff502c; padding: 2px 4px; border-radius: 4px; }
:deep(.markdown-body pre) { background-color: #f6f8fa; padding: 16px; border-radius: 8px; overflow: auto; }
</style>