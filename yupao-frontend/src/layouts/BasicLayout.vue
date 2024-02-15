<script setup lang="ts">
import {showSuccessToast} from "vant";
  import {useRouter} from "vue-router";
  const router=useRouter();
  const onClickLeft=()=>{
    //返回上一页
    router.back()
  };
  const onClickRight=()=>{
    router.push('/search')
  };
  // const active = ref('index');
  const onChange = (index) => showSuccessToast(`标签 ${index}`);
</script>

<template>
  <van-nav-bar
      title="标题"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
  >
    <template #right>
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>
    <div id="content">
      <router-view/>
    </div>
  <van-tabbar  @change="onChange" route>
    <van-tabbar-item to="/" icon="home-o" name="index" >主页</van-tabbar-item>
    <van-tabbar-item to="/team" icon="font-o" name="team" badge="3">队伍</van-tabbar-item>
    <van-tabbar-item to="/user" icon="user-o" name="user">个人</van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>

</style>