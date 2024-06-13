<script setup lang="ts">
import
{useRoute,useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {showNotify, showToast} from "vant";
import {getLove} from "../api/common.ts";
const route = useRoute();
const router=useRouter();
const userAccount = ref('');
const userPassword = ref('');

// 获取滚动通知
const tips = ref()
const love = async () => {
  const res = await getLove()
  tips.value = res.ishan
}
onMounted(() => {
  love()
})

const onSubmit = async () => {
  const res = await myAxios.post('/user/login', {
    userAccount: userAccount.value,
    userPassword: userPassword.value,
  })
  if (res.code === 0 && res.data) {
    showToast({
      type: 'success',
      message: '登录成功'
    })
    //当前用户没有设置标签
    if (res.data.tags===null){
      //跳转到标签页
      router.push('/user/tag')
      showNotify({type: 'warning', message: '请先设置标签'});
    }else{
      const redirectUrl = route.query?.redirect as string ?? '/';
      window.location.href = redirectUrl;
    }
    // 跳转到之前的页面
  } else {
    showToast({
      type: 'fail',
      message: res.message
    })
  }
};

const toForget = () => {
  window.location.href='/user/forget'
}

</script>

<template>
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
              v-model="userAccount"
              name="userAccout"
              label="账号"
              placeholder="请输入账号"
              :rules="[{ required: true, message: '请填写账号' }]"
          />
          <van-field
              v-model="userPassword"
              type="password"
              name="userPassword"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请填写密码' }]"
          />
        </van-cell-group>
        <span style="margin-right: -255px;font-size: 12px;color: #3c89fc;text-decoration: underline"
              @click="toForget">忘记密码</span>
        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit">
            登录
          </van-button>
          <van-button round block type="primary" style="margin-top: 10px" color="#FFA034" to="/user/register">
            注册
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