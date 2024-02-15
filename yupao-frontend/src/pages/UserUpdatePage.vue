<template>
  <template v-if="user">
    <van-cell title="账号" :value="user.userAccount"/>
    <van-cell title="昵称" is-link to="/user/edit" :value="user.userName"  @click="toEdit('userName', '昵称', user.userName)"/>
    <van-cell title="头像" is-link>
      <van-uploader v-model="fileList" :max-count="1" :after-read="updateAvatar" preview-size="60px">
        <template #preview-delete/>
        <img :src="user.userAvatar" style="height: 48px" alt=""/>
      </van-uploader>
    </van-cell>
    <van-cell title="性别" is-link :value="user.gender" @click="()=>showPicker=true">
      <span v-if="user.gender===1">男</span>
      <span v-else-if="user.gender===0">女</span>
      <span v-else>{{ "还没有填写性别" }}</span>
    </van-cell>
    <van-cell title="电话" is-link to="/user/edit" :value="user.phone" @click="toEdit('phone', '电话', user.phone)"/>
    <van-cell title="邮箱" is-link to="/user/edit" :value="user.email" @click="toEdit('email', '邮箱', user.email)"/>
    <van-cell title="注册时间" :value="time"/>
    <van-popup v-model:show="showPicker" round position="bottom">
      <van-picker
          title="性别"
          :columns="genders"
          @confirm="onConfirmGender"
          @cancel="()=>showPicker=false"
      />
    </van-popup>
  </template>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";

const user = ref();
const time=ref();

onMounted(async () => {
  user.value = await getCurrentUser();
  time.value=user.value.createTime.substring(0, user.value.createTime.indexOf('T'))
})
const fileList = ref([]);
const router = useRouter();
const showPicker = ref(false);
const genders = [
  {text: '男', value: '1'},
  {text: '女', value: '0'},
];



const onConfirmGender = async ({selectedValues}) => {
  const currentUser=await  getCurrentUser();
  const res = await myAxios.post("/user/update", {
    'id': currentUser.id,
    gender: selectedValues[0]
  })
  console.log("修改性别"+res)
  if (res?.code === 0) {
    showSuccessToast("修改成功")
  } else {
    showFailToast("修改失败" + (res.data.description ? `,${res.data.description}` : ''))
  }
  await refresh();
  showPicker.value = false
};

const refresh = async () => {
  user.value = await getCurrentUser();
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
    user.value.userAvatar = res?.data;
     await myAxios.post("/user/update", {
      'id': user.value.id,
      'userAvatar': res?.data,
    })
  } else {
    showFailToast("头像更新失败" + (res.data.description ? `,${res.data.description}` : ''))
  }
  fileList.value = []
}
</script>

<style scoped>

</style>