<template>
  <div class="user-center-layout">
    <el-header class="header">
      <div class="logo" @click="$router.push('/home')">✨ 返回首页</div>
      <div class="user-info">
        <el-avatar :size="30" style="background-color: #409eff; margin-right: 10px;">{{ (user.nickname || user.username || 'U').charAt(0) }}</el-avatar>
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
import { Odometer, EditPen, Collection, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const activeMenu = ref('dashboard')
// 默认显示“我的点赞”
const activeTab = ref('likes')

// 数据源
const myBlogs = ref([])
const collectList = ref([])
const toReadList = ref([])
const historyList = ref([])
// ✨✨ 新增：点赞列表数据源
const likedList = ref([])

const userForm = reactive({ ...user.value, password: '' })
const totalDuration = ref(0)

// 切换菜单
const handleSelect = async (index) => {
  activeMenu.value = index
  if (index === 'dashboard') {
    await fetchAllData() // 刷新数据以便统计
    initCharts() // 然后再画图
  }
}

// ✨✨ 核心：加载所有数据 ✨✨
const fetchAllData = async () => {
  if (!user.value.id) return

  try {
    // 1. 获取我的文章
    const allRes = await axios.get('http://localhost:8080/api/blog/all')
    myBlogs.value = allRes.data.filter(b => b.author === user.value.nickname || b.author === user.value.username)

    // 2. 获取收藏列表 (Type=1)
    const collectRes = await axios.get('http://localhost:8080/api/action/list', { params: { userId: user.value.id, type: 1 } })
    collectList.value = collectRes.data

    // 3. 获取待读列表 (Type=2)
    const toReadRes = await axios.get('http://localhost:8080/api/action/list', { params: { userId: user.value.id, type: 2 } })
    toReadList.value = toReadRes.data

    // 4. 获取历史记录
    const historyRes = await axios.get('http://localhost:8080/api/blog/history', { params: { userId: user.value.id } })
    historyList.value = historyRes.data
    
    // 5. 获取真实学习时长
    const statsRes = await axios.get('http://localhost:8080/api/user/stats', { params: { userId: user.value.id } })
    totalDuration.value = statsRes.data || 0

    // ✨✨ 6. 新增：获取我的点赞列表 ✨✨
    const likeRes = await axios.get('http://localhost:8080/api/blog/my-likes', { params: { userId: user.value.id } })
    likedList.value = likeRes.data

  } catch (e) {
    console.error('加载个人数据失败', e)
  }
}

// 移除收藏/待读
const removeAction = async (blogId, type) => {
  await axios.post(`http://localhost:8080/api/action/toggle?blogId=${blogId}&userId=${user.value.id}&type=${type}`)
  ElMessage.success('已移除')
  fetchAllData() // 刷新列表
}

// ✨✨ 新增：取消点赞逻辑 ✨✨
const handleCancelLike = async (blogId) => {
  // 调用点赞接口（再次调用即取消）
  await axios.post(`http://localhost:8080/api/blog/like?blogId=${blogId}&userId=${user.value.id}`)
  ElMessage.success('已取消点赞')
  fetchAllData() // 刷新列表
}

// 删除我的文章
const handleDelete = async (id) => {
  ElMessageBox.confirm('确认删除？').then(async () => {
    await axios.delete(`http://localhost:8080/api/blog/delete/${id}`)
    ElMessage.success('删除成功')
    fetchAllData()
  })
}

// 修改资料
const updateUser = async () => {
  try {
    await axios.put('http://localhost:8080/api/user/update', userForm)
    if (userForm.password) {
      ElMessage.success('密码修改成功，请重新登录')
      handleLogout()
    } else {
      ElMessage.success('保存成功')
      user.value.nickname = userForm.nickname
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  } catch (e) { ElMessage.error('修改失败') }
}

const handleLogout = () => { localStorage.removeItem('user'); router.push('/login') }

// 初始化图表
const initCharts = async () => {
  await nextTick()
  const chartDom = document.getElementById('radarChart')
  if (chartDom) {
    try {
      const res = await axios.get(`http://localhost:8080/api/user/radar?userId=${user.value.id}`)
      const radarData = res.data

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