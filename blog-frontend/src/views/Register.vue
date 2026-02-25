<template>
  <div class="app-container">
    <el-card class="box-card">
      <h2 style="text-align: center; margin-bottom: 20px;">欢迎注册</h2>
      <el-form :model="form" label-width="70px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="可选填"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%;">立 即 注 册</el-button>
        </el-form-item>
        <div style="text-align: right; margin-top: 10px;">
          <el-link type="primary" @click="$router.push('/login')">已有账号？去登录</el-link>
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

const router = useRouter() // 引入路由工具
const form = reactive({ username: '', password: '', email: '' })

const handleRegister = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请填写完整！')
  try {
    const res = await axios.post('http://localhost:8080/api/user/register', form)
    if (res.data === '注册成功！') {
      ElMessage.success('注册成功，快去登录吧！')
      router.push('/login') // 注册成功后，自动跳转到登录页！
    } else {
      ElMessage.warning(res.data)
    }
  } catch (e) {
    ElMessage.error('网络错误，请检查后端是否启动')
  }
}
</script>

<style scoped>
.app-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f3f4f6;}
.box-card { width: 400px; padding: 20px; border-radius: 10px; }
</style>