<template>
  <div class="app-container">
    <el-card class="box-card">
      <h2 style="text-align: center; margin-bottom: 20px;">欢迎登录</h2>
      <el-form :model="form" label-width="70px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入你的用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="form.password" placeholder="请输入密码" @keyup.enter="handleLogin"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleLogin" style="width: 100%;">立 即 登 录</el-button>
        </el-form-item>
        <div style="text-align: right; margin-top: 10px;">
          <el-link type="primary" @click="$router.push('/register')">没有账号？去注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请填写完整！')
  
  try {
    const res = await axios.post('http://localhost:8080/api/user/login', form)
    
    // ✨✨ 适配 Result 结构: code=200 为成功
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg || '登录成功')
      // 数据在 res.data.data 里
      localStorage.setItem('user', JSON.stringify(res.data.data))
      router.push('/home')
    } else {
      // 业务错误 (如密码错)
      ElMessage.error(res.data.msg)
    }
  } catch (e) {
    ElMessage.error('无法连接服务器')
  }
}
</script>

<style scoped>
.app-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f3f4f6;}
.box-card { width: 400px; padding: 20px; border-radius: 10px; }
</style>