<template>
  <div class="app-container">
    <el-card class="box-card">
      <h2 style="text-align: center; margin-bottom: 20px;">欢迎注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            style="border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 60px; height: 60px; display: flex; justify-content: center; align-items: center;"
          >
            <img v-if="form.avatar" :src="form.avatar" style="width: 100%; height: 100%; object-fit: cover; display: block;"/>
            <el-icon v-else :size="20" color="#8c939d"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="密码">
          <el-input type="password" v-model="form.password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input type="password" v-model="confirmPassword" placeholder="请再次输入密码"></el-input>
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
import { reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const form = reactive({ username: '', password: '', email: '', avatar: '' })
const confirmPassword = ref('')

const handleAvatarSuccess = (res) => {
  form.avatar = res
  ElMessage.success('头像上传成功')
}

const handleRegister = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请填写完整！')
  if (form.password !== confirmPassword.value) return ElMessage.warning('两次输入的密码不一致！')

  try {
    const res = await axios.post('http://localhost:8080/api/user/register', form)
    
    // ✨✨ 适配 Result 结构
    if (res.data.code === 200) {
      ElMessage.success('注册成功，快去登录吧！')
      router.push('/login')
    } else {
      ElMessage.warning(res.data.msg)
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