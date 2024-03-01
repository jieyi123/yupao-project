<script setup lang="ts">
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {showFailToast, showNotify, showSuccessToast} from "vant";
import {getLove} from "../api/common.ts";


const reg_tel = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
//长度为8-20位
const reg_username = /^.{8,20}$/;

const userAccount = ref('');
const userPassword = ref('');
const checkPassword = ref('');
const lock = ref(true)
const phone = ref('');
const verifyCode = ref('');
const codeTime = ref(0);
const notice = ref(false);
const notice_text = ref('')

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
  return userPassword.value === checkPassword.value;
}

const checkAccount= ()=>{

  if (!reg_username.test(userAccount.value)) {
    // notice.value = true;
    // notice_text.value = '字母开头，允许4-16字节，允许字母数字下划线'
    showNotify({type: 'danger', message: '字母开头，允许4-16字节，允许字母数字下划线'});
    return false;
  }
  return true;
}

const sendMessage = async () => {
  if (phone.value === '') {
    showNotify({type: 'danger', message: '请先输入手机号'});
  } else {
    if (!reg_tel.test(phone.value)) {
      showNotify({type: 'danger', message: '手机号格式错误'});
    } else {
      if (userAccount.value==='' || userPassword.value === '' || checkPassword.value === '') {
        showFailToast("请完成以上信息")
        return;
      }
        //验证码
        initAlicom4({
          captchaId: "cf6ae8259eb05cfae171b2bd6d18bd62",
          product: 'bind',
          //protocol: 'http://' //部署在对应服务上可删除此配置
        }, function (captcha: any) {
          // captcha为验证码实例
          captcha.onReady(function () {
          }).onSuccess(async function () {
            const result = captcha.getValidate();
            //your code
            let flag = countDown();
            if (flag) {
              const res = await myAxios.get("/user/captcha?phone=" + phone.value)
              if (res?.code === 0) {
                showNotify({type: 'success', message: '短信发送成功，2分钟内有效'});
                lock.value = false
              } else {
                showFailToast(res?.message)
              }
            }
          }).onError(function () {
            //your code
          })
          captcha.showCaptcha();
        });
      }

  }
}
const buttonDis=ref(false);


const countDown = () => {
  if (codeTime.value > 0) {
    buttonDis.value=true;
    showFailToast("不能重复获取")
    return false;
  } else {
    codeTime.value = 60
    buttonDis.value=true;
    let time = setInterval(() => {
      codeTime.value--
      if (codeTime.value < 1) {
        buttonDis.value=false;
        clearInterval(time)
        codeTime.value = 0
      }
    }, 1000)
    return true;
  }
}

const onSubmit = async () => {
  if (phone.value === '') {
    showFailToast("请填写手机号")
    return
  }
  if (verifyCode.value === '') {
    showFailToast("请输入验证码")
    return
  }
  if (userPassword.value === '' || checkPassword.value === '') {
    showFailToast("请填写密码")
    return
  }
  if (!reg_username.test(userAccount.value)) {
    // showFailToast("账号非法");
    notice.value = true;
    notice_text.value = '字母开头，允许4-16字节，允许字母数字下划线'
    showNotify({type: 'danger', message: '字母开头，允许4-16字节，允许字母数字下划线'});
    return ;
  }

  const res = await myAxios.post("/user/register", {
    phone: phone.value,
    verifyCode: verifyCode.value,
    userAccount: userAccount.value,
    userPassword: userPassword.value,
    checkPassword: checkPassword.value
  })
  if (res?.code === 0) {
    showSuccessToast("注册成功")
    // const redirectUrl = route.query?.redirect as string ?? '/user/login';
    // window.location.href = redirectUrl;
  } else {
    showFailToast(res?.message)
  }
};

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
              name="userAccount"
              label="账户"
              required
              placeholder="请输入账户"
              :rules="[{ required: true, message: '请填写账户' },{validator:checkAccount}]"
          />
          <van-field
              v-model="userPassword"
              type="password"
              required
              name="userPassword"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请填写密码', }]"
          />
          <van-field
              v-model="checkPassword"
              required
              type="password"
              label="确认密码"
              placeholder="请再次输入密码"
              :rules="[{ required: true,message: '请输入密码' },{validator,message:'两次输入的密码不一致'}]"
          />
            <van-field
                style="text-align: center"
                v-model="phone"
                required
                label="手机号"
                placeholder="请输入手机号"
                :rules="[{ required: true, message: '请填写手机号' }]"
            >
              <template #button>
                <div style="margin-top: -24px">
                <van-button size="small" type="primary" @click="sendMessage" :disabled="buttonDis">
                  <span v-if="!codeTime">发送验证码</span>
                  <span v-else>{{ codeTime }}秒</span>
                </van-button>
                </div>
              </template>
            </van-field>
          <van-field
              v-if="!lock"
              v-model="verifyCode"
              required
              label="验证码"
              placeholder="请输入验证码"
              :rules="[{ required: true, message: '请填写验证码' }]"
          />
        </van-cell-group>
        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit">
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
  padding: 7px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin: 10px;
  margin-top: 20px;

  .van-field {
    margin-bottom: 20px;
  }

  .van-button {
    margin-top: 20px;
  }
}
</style>