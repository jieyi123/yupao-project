<script setup lang="ts">
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";


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

const router = useRouter();

onMounted(async () => {
  //user.value = await getCurrentUser();
})

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

<template>
  <template v-if="user">
    <van-cell title="当前用户" :value="user?.username" />
    <van-cell title="修改信息" is-link to="/user/update" />
    <van-cell title="我创建的队伍" is-link to="/user/team/create" />
    <van-cell title="我加入的队伍" is-link to="/user/team/join" />
  </template>
</template>

<style scoped>

</style>