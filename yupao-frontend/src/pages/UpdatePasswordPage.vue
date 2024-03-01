<script setup lang="ts">

import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getLove} from "../api/common.ts";
import myAxios from "../plugins/myAxios.ts";
import {showNotify, showToast} from "vant";
import {getCurrentUser} from "../services/user.ts";

const router=useRouter();
const oldPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const onClickLeft = () => {
  router.push("/user")
};

// 获取滚动通知
const tips = ref()
const love = async () => {
  const res = await getLove()
  tips.value = res.ishan
}
onMounted(() => {
  love()
})
const validator = () => {
  return newPassword.value === confirmPassword.value;
}
//长度为8-20位
const reg_password = /^.{8,20}$/;

const checkPassword= ()=>{
  if (!reg_password.test(newPassword.value)) {
    showNotify({type: 'danger', message: '密码不低于8位'});
    return false;
  }
  return true;
}
const loading = ref(false);

const onSubmit = async () => {
  loading.value=true;
  const user=await getCurrentUser()
  const res = await myAxios.post('/user/updatePassword', {
    id:user.id,
    oldPassword: oldPassword.value,
    newPassword: newPassword.value,
    confirmPassword: confirmPassword.value,
  })
  if (res.code === 0 && res.data) {
    await new Promise(resolve => setTimeout(resolve, 1000));
    showToast({
      type: 'success',
      message: '修改成功'
    })
      //跳转到标签页
      router.push('/user/login')
    // 跳转到之前的页面
  } else {
    showToast({
      type: 'fail',
      message: res.message
    })
  }
};

</script>

<template>
  <van-sticky>
    <van-nav-bar
        title="修改密码"
        left-arrow
        @click-left="onClickLeft"
    >
    </van-nav-bar>
  </van-sticky>
  <div class="container">
    <!-- 通知 -->
    <van-notice-bar
        left-icon="volume-o"
        :text="tips"
        style="margin-top: 20px"
    />
    <div class="login-form">
      <h2 style="margin-bottom: 20px">Joy Friend</h2>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
              v-model="oldPassword"
              type="password"
              name="oldPassword"
              label="原密码"
              placeholder="请输入原密码"
              :rules="[{ required: true, message: '请填写密码' }]"
          />
          <van-field
              v-model="newPassword"
              type="password"
              name="newPassword"
              label="新密码"
              placeholder="请输入新密码"
              :rules="[{ required: true, message: '请填写密码' },{validator:checkPassword}]"
          />
          <van-field
              v-model="confirmPassword"
              type="password"
              name="confirmPassword"
              label="确认密码"
              placeholder="请输入确认密码"
              :rules="[{ required: true, message: '请填写密码' },{validator,message:'两次输入的密码不一致'}]"
          />
        </van-cell-group>
        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit">
            修改密码
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  height: 100vh;
}
.login-form {
  // 页面居中显示
  display: block;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin: 20px;
  .van-field {
    margin-bottom: 20px;
  }
  .van-button {
    margin-top: 20px;
  }
}
</style>