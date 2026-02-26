<template>
  <div class="user-center-layout">
    <el-header class="header">
      <div class="logo" @click="$router.push('/home')">✨ 返回首页</div>
      <div class="user-info">
        <el-avatar v-if="user.avatar" :size="30" :src="user.avatar" style="margin-right: 10px;"></el-avatar>
        <el-avatar v-else :size="30" style="background-color: #409eff; margin-right: 10px;">{{ (user.nickname || user.username || 'U').charAt(0) }}</el-avatar>
        
        <span>{{ user.nickname || user.username }}的个人中心</span>
        <el-button type="danger" size="small" @click="handleLogout" style="margin-left: 20px;">退出</el-button>
      </div>
    </el-header>

    <el-container style="height: calc(100vh - 60px);">
      <el-aside width="200px" style="background-color: #fff; border-right: 1px solid #eee;">
        <el-menu :default-active="activeMenu" @select="handleSelect" style="border: none; margin-top: 20px;">
          <el-menu-item index="dashboard">
            <el-icon><Odometer /></el-icon><span>📊 数据概览</span>
          </el-menu-item>
          <el-menu-item index="notification">
            <el-icon><Bell /></el-icon>
            <span>🔔 消息中心</span>
            <el-tag v-if="unreadCount > 0" type="danger" size="small" round style="margin-left: 10px;">{{ unreadCount }}</el-tag>
          </el-menu-item>
          <el-menu-item index="bookshelf">
            <el-icon><Collection /></el-icon><span>📚 我的书架</span>
          </el-menu-item>
          <el-menu-item index="my-blogs">
            <el-icon><EditPen /></el-icon><span>📝 文章管理</span>
          </el-menu-item>
          <el-menu-item index="settings">
            <el-icon><Setting /></el-icon><span>⚙️ 个人设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main style="background-color: #f5f7fa; padding: 30px;">
        
        <div v-if="activeMenu === 'dashboard'">
          <h2 style="margin-bottom: 20px;">👋 欢迎回来，{{ user.nickname || user.username }}</h2>
          <el-row :gutter="20" style="margin-bottom: 30px;">
            <el-col :span="6"><el-card shadow="hover"><h3>📝 发布文章</h3><div class="stat-num">{{ myBlogs.length }} 篇</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover"><h3>👍 点赞文章</h3><div class="stat-num">{{ likedList.length }} 篇</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover"><h3>⭐️ 收藏文章</h3><div class="stat-num">{{ collectList.length }} 篇</div></el-card></el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <h3>⏱️ 学习时长</h3>
                <div class="stat-num">{{ Math.floor(totalDuration / 60) }} 分钟</div>
              </el-card>
            </el-col>
          </el-row>
          <el-card>
            <div slot="header"><b>📈 您的阅读偏好 (技术雷达)</b></div>
            <div id="radarChart" style="width: 100%; height: 400px;"></div>
          </el-card>
        </div>

        <div v-if="activeMenu === 'notification'">
          <div style="margin-bottom: 20px; display: flex; justify-content: space-between;">
             <h3>🔔 我的消息</h3>
             <el-button type="primary" link @click="markAllRead">全部已读</el-button>
          </div>
          <el-card v-for="note in notificationList" :key="note.id" style="margin-bottom: 10px;" shadow="hover">
             <div style="display: flex; justify-content: space-between; align-items: center;">
                <div style="display: flex; align-items: center; gap: 10px;">
                   <el-badge is-dot :hidden="note.isRead === 1" class="item">
                      <el-icon size="20" color="#409eff" v-if="note.type===1"><StarFilled /></el-icon>
                      <el-icon size="20" color="#67c23a" v-if="note.type===2"><Comment /></el-icon>
                   </el-badge>
                   <div>
                      <span style="font-weight: bold;">{{ note.senderName }}</span> 
                      <span style="color: #666; margin: 0 5px;">{{ note.type===1 ? '点赞了' : '评论了' }}</span>
                      <el-link type="primary" @click="$router.push(`/blog/${note.relatedId}`)">查看详情</el-link>
                      <div style="font-size: 12px; color: #999; margin-top: 5px;">{{ note.content }}</div>
                   </div>
                </div>
                <div style="color: #ccc; font-size: 12px;">{{ note.createTime }}</div>
             </div>
          </el-card>
          <el-empty v-if="notificationList.length===0" description="暂无新消息"></el-empty>
        </div>

        <div v-if="activeMenu === 'bookshelf'">
          <el-tabs v-model="activeTab" type="card">
            <el-tab-pane label="👍 我的点赞" name="likes">
               <el-table :data="likedList" style="width: 100%" empty-text="暂无点赞内容">
                 <el-table-column prop="title" label="标题">
                   <template #default="scope">
                     <el-link type="primary" @click="$router.push(`/blog/${scope.row.id}`)">{{ scope.row.title }}</el-link>
                   </template>
                 </el-table-column>
                 <el-table-column prop="author" label="作者" width="120"></el-table-column>
                 <el-table-column label="操作" width="100">
                   <template #default="scope">
                     <el-button size="small" type="danger" link @click="handleCancelLike(scope.row.id)">取消点赞</el-button>
                   </template>
                 </el-table-column>
               </el-table>
            </el-tab-pane>

            <el-tab-pane label="⭐️ 我的收藏" name="collect">
               <el-table :data="collectList" style="width: 100%" empty-text="暂无收藏">
                 <el-table-column prop="title" label="标题">
                   <template #default="scope">
                     <el-link type="primary" @click="$router.push(`/blog/${scope.row.id}`)">{{ scope.row.title }}</el-link>
                   </template>
                 </el-table-column>
                 <el-table-column prop="author" label="作者" width="120"></el-table-column>
                 <el-table-column label="操作" width="100">
                   <template #default="scope">
                     <el-button size="small" type="danger" link @click="removeAction(scope.row.id, 1)">取消收藏</el-button>
                   </template>
                 </el-table-column>
               </el-table>
            </el-tab-pane>

            <el-tab-pane label="📅 待读列表" name="toread">
               <el-table :data="toReadList" style="width: 100%" empty-text="书架空空如也">
                 <el-table-column prop="title" label="标题">
                   <template #default="scope">
                     <el-link type="primary" @click="$router.push(`/blog/${scope.row.id}`)">{{ scope.row.title }}</el-link>
                   </template>
                 </el-table-column>
                 <el-table-column prop="category" label="分类" width="120"></el-table-column>
                 <el-table-column label="操作" width="100">
                   <template #default="scope">
                     <el-button size="small" type="success" link @click="removeAction(scope.row.id, 2)">完成阅读</el-button>
                   </template>
                 </el-table-column>
               </el-table>
            </el-tab-pane>

            <el-tab-pane label="🚫 黑名单" name="blocked">
               <el-table :data="blockedList" style="width: 100%" empty-text="黑名单为空">
                 <el-table-column prop="title" label="标题">
                   <template #default="scope">
                     <span style="color: #999;">{{ scope.row.title }}</span>
                   </template>
                 </el-table-column>
                 <el-table-column prop="author" label="作者" width="120"></el-table-column>
                 <el-table-column label="操作" width="120">
                   <template #default="scope">
                     <el-button size="small" type="warning" link @click="removeAction(scope.row.id, 3)">移出黑名单</el-button>
                   </template>
                 </el-table-column>
               </el-table>
            </el-tab-pane>

            <el-tab-pane label="🕒 浏览历史" name="history">
               <div v-for="blog in historyList" :key="blog.id" class="mini-item" @click="$router.push(`/blog/${blog.id}`)">
                 <span style="font-weight: bold;">{{ blog.title }}</span>
                 <span style="font-size: 12px; color: #999;">最近访问</span>
               </div>
               <el-empty v-if="historyList.length===0" description="暂无浏览记录"></el-empty>
            </el-tab-pane>
          </el-tabs>
        </div>

        <div v-if="activeMenu === 'my-blogs'">
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="$router.push('/home')">去写文章</el-button>
          </div>
          <el-table :data="myBlogs" style="width: 100%">
            <el-table-column prop="title" label="标题"></el-table-column>
            <el-table-column prop="views" label="阅读量" width="100"></el-table-column>
            <el-table-column prop="likes" label="点赞" width="100"></el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180"></el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button size="small" @click="$router.push(`/blog/${scope.row.id}`)">查看</el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeMenu === 'settings'">
          <el-card style="width: 500px;">
            <div slot="header"><b>✏️ 修改资料</b></div>
            <el-form :model="userForm" label-width="80px">
              <el-form-item label="头像">
                 <el-upload 
                    action="http://localhost:8080/api/upload" 
                    :show-file-list="false" 
                    :on-success="handleUploadSuccess" 
                    style="border: 1px dashed #d9d9d9; width: 80px; height: 80px; border-radius: 50%; display: flex; justify-content: center; align-items: center; cursor: pointer; overflow: hidden;"
                  >
                    <img v-if="userForm.avatar" :src="userForm.avatar" style="width: 100%; height: 100%; object-fit: cover;"/>
                    <el-icon v-else :size="20" color="#8c939d"><Plus/></el-icon>
                  </el-upload>
              </el-form-item>
              <el-form-item label="账号"><el-input v-model="userForm.username" disabled></el-input></el-form-item>
              <el-form-item label="昵称"><el-input v-model="userForm.nickname"></el-input></el-form-item>
              <el-form-item label="新密码"><el-input v-model="userForm.password" type="password" show-password placeholder="不改请留空"></el-input></el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateUser">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Odometer, EditPen, Collection, Setting, Plus, Bell, StarFilled, Comment } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const activeMenu = ref('dashboard')
const activeTab = ref('likes')

const myBlogs = ref([])
const collectList = ref([])
const toReadList = ref([])
const historyList = ref([])
const likedList = ref([])
const blockedList = ref([]) 
const notificationList = ref([]) 
const unreadCount = ref(0) 

const userForm = reactive({ ...user.value, password: '' })
const totalDuration = ref(0)

const handleSelect = async (index) => {
  activeMenu.value = index
  if (index === 'dashboard') {
    await fetchAllData()
    initCharts()
  } else if (index === 'notification') {
    await fetchNotifications()
  }
}

// ✨✨✨ 修复：处理头像上传回调的 Result 包装 ✨✨✨
const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    userForm.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const fetchAllData = async () => {
  if (!user.value.id) return
  try {
    const allRes = await axios.get('http://localhost:8080/api/blog/all')
    // 兼容 List 和 Result
    const blogs = allRes.data.code === 200 ? allRes.data.data : (Array.isArray(allRes.data) ? allRes.data : [])
    myBlogs.value = blogs.filter(b => b.author === user.value.nickname || b.author === user.value.username)
    
    // Action 列表
    const fetchAction = async (type) => {
        const res = await axios.get('http://localhost:8080/api/action/list', { params: { userId: user.value.id, type } })
        return res.data.code === 200 ? res.data.data : res.data
    }
    collectList.value = await fetchAction(1)
    toReadList.value = await fetchAction(2)
    blockedList.value = await fetchAction(3)

    const historyRes = await axios.get('http://localhost:8080/api/blog/history', { params: { userId: user.value.id } })
    historyList.value = historyRes.data.code === 200 ? historyRes.data.data : historyRes.data
    
    const likeRes = await axios.get('http://localhost:8080/api/blog/my-likes', { params: { userId: user.value.id } })
    likedList.value = likeRes.data.code === 200 ? likeRes.data.data : likeRes.data
    
    // ✨✨✨ 修复：未读数适配 Result ✨✨✨
    const countRes = await axios.get('http://localhost:8080/api/notification/count', { params: { userId: user.value.id } })
    if (countRes.data.code === 200) {
        unreadCount.value = countRes.data.data
    } else {
        unreadCount.value = countRes.data // 兼容旧接口
    }

    const statsRes = await axios.get('http://localhost:8080/api/user/stats', { params: { userId: user.value.id } })
    if (statsRes.data.code === 200) {
        totalDuration.value = statsRes.data.data || 0
    }

  } catch (e) {
    console.error('加载数据失败', e)
  }
}

// ✨✨✨ 修复：通知列表适配 Result ✨✨✨
const fetchNotifications = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/notification/list', { params: { userId: user.value.id } })
    if (res.data.code === 200) {
        notificationList.value = res.data.data
    }
    // 刷新未读数
    const countRes = await axios.get('http://localhost:8080/api/notification/count', { params: { userId: user.value.id } })
    if (countRes.data.code === 200) {
        unreadCount.value = countRes.data.data
    }
  } catch(e) {
    ElMessage.error('消息加载失败')
  }
}

const markAllRead = async () => {
  await axios.post('http://localhost:8080/api/notification/read-all?userId=' + user.value.id)
  ElMessage.success('全部已读')
  fetchNotifications()
}

const removeAction = async (blogId, type) => {
  await axios.post(`http://localhost:8080/api/action/toggle?blogId=${blogId}&userId=${user.value.id}&type=${type}`)
  ElMessage.success('已移除')
  fetchAllData()
}

const handleCancelLike = async (blogId) => {
  await axios.post(`http://localhost:8080/api/blog/like?blogId=${blogId}&userId=${user.value.id}`)
  ElMessage.success('已取消点赞')
  fetchAllData()
}

const handleDelete = async (id) => {
  ElMessageBox.confirm('确认删除？').then(async () => {
    const res = await axios.delete(`http://localhost:8080/api/blog/delete/${id}`)
    // 兼容
    if (res.data.code === 200 || res.data === '删除成功') {
        ElMessage.success('删除成功')
        fetchAllData()
    }
  })
}

const updateUser = async () => {
  try {
    const res = await axios.put('http://localhost:8080/api/user/update', userForm)
    if (res.data.code === 200) {
        if (userForm.password) {
            ElMessage.success('密码修改成功，请重新登录')
            handleLogout()
        } else {
            ElMessage.success('资料保存成功')
            user.value = { ...user.value, nickname: userForm.nickname, avatar: userForm.avatar }
            localStorage.setItem('user', JSON.stringify(user.value))
        }
    } else {
        ElMessage.error(res.data.msg || '修改失败')
    }
  } catch (e) { ElMessage.error('网络错误') }
}

const handleLogout = () => { localStorage.removeItem('user'); router.push('/login') }

const initCharts = async () => {
  await nextTick()
  const chartDom = document.getElementById('radarChart')
  if (chartDom) {
    try {
      const res = await axios.get(`http://localhost:8080/api/user/radar?userId=${user.value.id}`)
      let radarData = {}
      if (res.data.code === 200) {
          radarData = res.data.data
      }
      
      if (radarData.indicators) {
          const myChart = echarts.init(chartDom)
          myChart.setOption({
            radar: {
              indicator: radarData.indicators
            },
            series: [{
              type: 'radar',
              data: [
                { 
                  value: radarData.values,
                  name: '阅读偏好' 
                }
              ]
            }]
          })
      }
    } catch (e) {
      console.error('图表加载失败', e)
    }
  }
}

onMounted(() => {
  if (!user.value.id) router.push('/login')
  fetchAllData()
  initCharts()
})
</script>

<style scoped>
.header { background: #fff; border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center; padding: 0 40px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); z-index: 10; }
.logo { font-size: 18px; font-weight: bold; cursor: pointer; color: #409eff; }
.stat-num { font-size: 24px; font-weight: bold; margin-top: 10px; color: #333; }
.mini-item { padding: 15px; background: #fff; margin-bottom: 10px; border-radius: 4px; cursor: pointer; display: flex; justify-content: space-between; border-bottom: 1px solid #eee; transition: 0.2s; }
.mini-item:hover { background: #f0f9eb; }
</style>