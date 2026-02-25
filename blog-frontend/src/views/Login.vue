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
    
    // ✨ 修改判断逻辑：如果后端返回的是一个对象，并且有 id，说明登录成功
    if (res.data && res.data.id) {
      ElMessage.success('登录成功！')
      
      //把整个用户对象转成字符串，存入 localStorage
      // 以后我们要用 id 就取 user.id，要用名字就取 user.username
      localStorage.setItem('user', JSON.stringify(res.data))
      
      router.push('/home')
    } else {
      // 如果返回的是字符串（比如"账号或密码错误"），就显示报错
      ElMessage.error(res.data || '登录失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}
</script>

<style scoped>
.app-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f3f4f6;}
.box-card { width: 400px; padding: 20px; border-radius: 10px; }
</style>