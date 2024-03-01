<template>
  <div id="teamPage">
    <!-- 通知 -->
    <van-notice-bar left-icon="volume-o" :text="tips" />
    <team-card-list :teamList="teamList" />
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>

<script setup lang="ts">

import TeamCardList from "../components/TeamCardList.vue";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {showFailToast} from "vant";
import {getLove} from "../api/common.ts";

const teamList = ref([]);

// 获取滚动通知
const tips = ref()
const love = async () => {
  const res = await getLove()
  tips.value = res.ishan
}

/**
 * 搜索队伍
 * @param val
 * @returns {Promise<void>}
 */
const listTeam = async () => {
  const res = await myAxios.get("/team/list/my/join", {
    // params: {
    //   pageNum: 1,
    // },
  });
  if (res?.code === 0) {
    teamList.value = res.data;
  } else {
    showFailToast('加载队伍失败，请刷新重试');
  }
}


// 页面加载时只触发一次
onMounted( () => {
  listTeam();
  love()
})



</script>

<style scoped>

</style>
