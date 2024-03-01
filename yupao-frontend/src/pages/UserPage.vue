<script setup lang="ts">
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {getCurrentUser} from "../services/user.ts";
import dayjs from "dayjs";
import {showConfirmDialog, showFailToast, showSuccessToast, showToast} from "vant";
import myAxios from "../plugins/myAxios.ts";

const userInfo = ref({
  userAvatar: '',
  userName: '',
  userAccount: '',
  gender: 0,
  tags: []
});

const fileList = ref([]);
const showPicker = ref(false);
const genders = [
  {text: '男', value: '1'},
  {text: '女', value: '0'},
];

const onConfirmGender = async ({selectedValues}) => {
  const currentUser = await getCurrentUser();
  const res = await myAxios.post("/user/update", {
    'id': currentUser.id,
    gender: selectedValues[0]
  })
  console.log("修改性别" + res)
  if (res?.code === 0) {
    showSuccessToast("修改成功")
  } else {
    showFailToast("修改失败" + (res.data.description ? `,${res.data.description}` : ''))
  }
  await refresh();
  showPicker.value = false
};

const refresh = async () => {
  const currentUser = await getCurrentUser();
  userInfo.value=currentUser
  if (currentUser.tags) {
    userInfo.value.tags = JSON.parse(currentUser.tags)
  }
}

const toEdit = (editKey: string, editName: string, currentValue: string) => {
  router.push({
    path: '/user/edit',
    query: {
      editKey,
      editName,
      currentValue,
    }
  })
}

const router = useRouter();
onMounted(async () => {
  const currentUser = await getCurrentUser();
  userInfo.value=currentUser
  if (currentUser.tags) {
    userInfo.value.tags = JSON.parse(currentUser.tags)
  }

  // time.value=user.value.createTime.substring(0, user.value.createTime.indexOf('T'))
})
const doLogout = () => {
  showConfirmDialog({
    title: '确定退出吗？',
    message:
        '如果解决方法是丑陋的，那就肯定还有更好的解决方法，只是还没有发现而已。'
  })
      .then(() => {
        logout()
      })
      .catch(() => {
        // on cancel
      })
}

const logout = async () => {
  const res = await myAxios.post('/user/logout');
  console.log(res)
  if (res.code === 0) {
    showToast({
      message: '退出成功',
      type: 'success'
    })
    router.push('/user/login')
  }
}

const updateAvatar = async () => {
  let formData = new FormData();
  formData.append("file", fileList.value[0].file)
  console.log(fileList)
  const res = await myAxios.post("/upload", formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  if (res?.code === 0) {
    showSuccessToast("头像更新成功")
    userInfo.value.userAvatar = res?.data;
    await myAxios.post("/user/update", {
      'id': userInfo.value.id,
      'userAvatar': res?.data,
    })
  } else {
    showFailToast("头像更新失败" + (res.data.description ? `,${res.data.description}` : ''))
  }
  fileList.value = []
}

</script>

<template>
  <!--  <template v-if="user">-->
  <!--    <van-cell title="当前用户" :value="user?.userName" />-->
  <!--    <van-cell title="修改信息" is-link to="/user/update" />-->
  <!--    <van-cell title="我创建的队伍" is-link to="/user/team/create" />-->
  <!--    <van-cell title="我加入的队伍" is-link to="/user/team/join" />-->
  <!--  </template>-->
  <div class="container">
    <!-- 顶部 -->
    <div class="container-avatar">

      <van-uploader v-model="fileList" :max-count="1" :after-read="updateAvatar" preview-size="60px">
        <template #preview-delete/>
        <van-image round width="10rem" height="10rem" :src="userInfo.userAvatar"/>
      </van-uploader>
    </div>
    <div class="container-user-info">

      <van-cell title="账号" :value="userInfo.userAccount"/>
      <van-cell title="昵称" is-link to="/user/edit" :value="userInfo.userName"
                @click="toEdit('userName', '昵称', userInfo.userName)"/>
      <van-cell title="性别" is-link :value="userInfo.gender" @click="()=>showPicker=true">
        <span v-if="userInfo.gender===1">男</span>
        <span v-else-if="userInfo.gender===0">女</span>
        <span v-else>{{ "还没有填写性别" }}</span>
      </van-cell>
      <van-cell title="电话" is-link to="/user/edit" :value="userInfo.phone"
                @click="toEdit('phone', '电话', userInfo.phone)"/>
      <van-cell title="邮箱" is-link to="/user/edit" :value="userInfo.email || '还没有填写邮箱'"
                @click="toEdit('email', '邮箱', userInfo.email)"/>
      <van-cell title="简介" is-link to="/user/edit" :value="userInfo.profile|| '还没有填写简介'"
                @click="toEdit('profile', '简介', userInfo.profile)"/>
      <van-cell title="标签" is-link to="/user/tag">
        <template #value>
          <van-tag plain type="danger" v-for="tag in userInfo.tags" style="margin-right: 8px; margin-top: 8px">
            {{ tag }}
          </van-tag>
        </template>
      </van-cell>
      <van-cell title="修改密码" is-link to="/user/update/password"/>
      <van-cell title="注册时间" :value="dayjs(userInfo.createTime).format('YYYY-MM-DD HH:mm')"/>
      <van-popup v-model:show="showPicker" round position="bottom">
        <van-picker
            title="性别"
            :columns="genders"
            @confirm="onConfirmGender"
            @cancel="()=>showPicker=false"
        />
      </van-popup>

      <div class="container-user-info-logout">
        <van-button
            color="linear-gradient(to right, #ff6034, #ee0a24)"
            @click="doLogout"
        >
          退出登录
        </van-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  justify-content: center;
  align-items: center;
  height: 100%;

  .container-avatar {
    padding: 15px;
    // 设置背景渐变
    text-align: center;
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    box-shadow: 0px 4px 8px #ccc;
    border: 1px solid #ccc;
    // 设置头像组件阴影，更具体
    .van-image {
      box-shadow: 0px 4px 12px #000;
    }
  }

  .container-user-info {
    margin-top: 10px;

    .container-user-info-logout {
      margin-top: 20px;
      text-align: center;

      .van-button {
        width: 50%;
        background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
        box-shadow: 0px 4px 8px #ccc;
        border: 1px solid #ccc;
      }
    }
  }
}
</style>