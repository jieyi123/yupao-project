<template>
<!--  <template v-if="user">-->
    <van-cell title="昵称" is-link to="/user/edit" :value="user.username"  @click="toEdit('username', '昵称', user.username)"/>
    <van-cell title="账号" :value="user.userAccount"/>
    <van-cell title="头像" is-link to="/user/edit">
      <img style="height: 48px" :src="user.avatarUrl"/>
    </van-cell>
    <van-cell title="性别" is-link :value="user.gender" @click="toEdit('gender', '性别', user.gender)"/>
    <van-cell title="电话" is-link to="/user/edit" :value="user.phone" @click="toEdit('phone', '电话', user.phone)"/>
    <van-cell title="邮箱" is-link to="/user/edit" :value="user.email" @click="toEdit('email', '邮箱', user.email)"/>
    <van-cell title="星球编号" :value="user.planetCode"/>
    <van-cell title="注册时间" :value="user.createTime"/>
<!--  </template>-->
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";

const user = ref({
  id: 1,
  username: '鱼皮',
  userAccount: 'dogYupi',
  avatarUrl: 'https://pjieyi.oss-cn-chengdu.aliyuncs.com/public/ffd16276-422b-43f0-b1d1-a83473322ce4.jpg',
  gender: '男',
  phone: '123112312',
  email: '12345@qq.com',
  planetCode: '1234',
  createTime: new Date().toDateString(),
});

onMounted(async () => {
  //user.value = await getCurrentUser();
})

const router = useRouter();

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
</script>

<style scoped>

</style>