<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showNotify, showSuccessToast} from "vant";
import {getCurrentUser} from "../services/user.ts";
import {getLove} from "../api/common.ts";

const codeTime = ref(0)
const code = ref('')
const route = useRoute();
const router = useRouter();
const reg_tel = /^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/;
const reg_mail = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
const editUser = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName
})

const user=ref();


// 获取滚动通知
const tips = ref()
const love = async () => {
  const res = await getLove()
  tips.value = res.ishan
}
onMounted(async () => {
  love()
  const currentUser = await getCurrentUser();
  if (!currentUser) {
    showFailToast('未登录');
    return;
  }
  user.value=currentUser;
})
const onSubmit = async () => {

  if (editUser.value.editKey==='email') {
    let email = editUser.value.currentValue;
    if (email === '') {
      showNotify({message: '请先输入邮箱'});
      return;
    } else {
      if (!reg_mail.test(<string>email)) {
        showNotify({type: 'danger', message: '邮件不合法'});
        return;
      }
    }
  }

  const res = await myAxios.post('/user/update', {
    'id': user.value.id,
    [editUser.value.editKey as string]: editUser.value.currentValue,
    code: code.value
  })
  if (res.code === 0 && res.data > 0) {
    showSuccessToast('修改成功');
    router.back();
  } else {
    showFailToast(res.message);
  }
};
const lock = ref(true)

const sendMessage = async () => {
  let phone = editUser.value.currentValue;
  if (phone === '') {
    showNotify({type: 'danger', message: '请先输入手机号'});
  } else {
    if (!reg_tel.test(<string>phone)) {
      showNotify({type: 'danger', message: '手机号格式错误'});
    } else {
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
          const res=await myAxios.get('/user/verifyCapture',{
            params:{
              lot_number:result.lot_number,
              captcha_output:result.captcha_output,
              pass_token:result.pass_token,
              gen_time:result.gen_time,
              captcha_id:result.captcha_id,
            }
          })
          try {
            if (res.data.result === 'success') {
              //验证成功
              //your code
              let flag = countDown();
              if (flag) {
                const res = await myAxios.get("/user/captcha?phone=" + phone)
                if (res?.code === 0) {
                  showNotify({type: 'success', message: '短信发送成功，2分钟内有效'});
                  lock.value = false
                } else {
                  showFailToast(res?.message)
                }
              }
            }
          } catch (e) {
            showFailToast('验证失败')
          }

        }).onError(function () {
          //your code
        })
        captcha.showCaptcha();
      });
    }

  }
}
const sendEmailMessage = async () => {
  let email = editUser.value.currentValue;
  if (email === '') {
    showNotify({message: '请先输入邮箱'});
  } else {
    if (!reg_mail.test(<string>email)) {
      showNotify({type: 'danger', message: '邮件不合法'});
    } else {
        const res = await myAxios.post('/user/update', {
          'id': user.value.id,
          [editUser.value.editKey as string]: editUser.value.currentValue,
        })
        lock.value = false
        if (res?.data.code === 0) {
          showSuccessToast("邮件修改成功")
        } else {
          showFailToast("邮件修改失败," + res?.data.description ?? '')
        }
    }
  }
}

const buttonDis = ref(false);

const onClickLeft = () => {
  router.push("/user")
};


const countDown = () => {
  if (codeTime.value > 0) {
    buttonDis.value = true;
    showFailToast("不能重复获取")
    return false;
  } else {
    codeTime.value = 60
    buttonDis.value = true;
    let time = setInterval(() => {
      codeTime.value--
      if (codeTime.value < 1) {
        buttonDis.value = false;
        clearInterval(time)
        codeTime.value = 0
      }
    }, 1000)
    return true;
  }
}


</script>

<template>
  <van-sticky>
    <van-nav-bar
        title="编辑个人信息"
        left-arrow
        @click-left="onClickLeft"
    >
    </van-nav-bar>
  </van-sticky>
  <!-- 通知 -->
  <van-notice-bar
      left-icon="volume-o"
      :text="tips"
      style="margin-top: 10px"
  />
  <van-form @submit="onSubmit" style="margin-top: 10px">
    <van-cell-group inset>
      <van-field
          label-width="3em"
          v-model="editUser.currentValue"
          :name="editUser.editKey"
          :label="editUser.editName"
          :placeholder="`请输入${editUser.editName}`"
          autofocus
      >
        <template v-if="editUser.editKey==='phone'|| editUser.editKey==='email'" #button>
          <div style="margin-top: -5px">
            <van-button v-if="editUser.editKey==='phone'" size="small" type="primary" @click="sendMessage"
                        :disabled="buttonDis">
              <span v-if="!codeTime">发送验证码</span>
              <span v-else>{{ codeTime }}秒</span>
            </van-button>
<!--            <van-button v-else size="small" type="primary" @click="sendEmailMessage">-->
<!--              <span v-if="!codeTime">发送验证码</span>-->
<!--              <span v-else>{{ codeTime }}秒</span>-->
<!--            </van-button>-->
          </div>
        </template>
      </van-field>
      <van-field
          v-if="!lock"
          v-model="code"
          required
          label="验证码"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请填写验证码' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<style scoped >

</style>