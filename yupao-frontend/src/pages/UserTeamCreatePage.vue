<template>
  <div id="teamPage">
<!--    <van-search v-model="searchText" show-action placeholder="搜索队伍" @search="onSearch" >-->
<!--    </van-search>-->
    <!-- 通知 -->
    <van-notice-bar left-icon="volume-o" :text="tips" />
    <team-card-list :teamList="teamList" />
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
<!--    <van-button class="add-button" type="primary" icon="plus" @click="doJoinTeam" />-->
  </div>
</template>

<script setup lang="ts">

import TeamCardList from "../components/TeamCardList.vue";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getLove} from "../api/common.ts";

// 获取滚动通知
const tips = ref()
const love = async () => {
  const res = await getLove()
  tips.value = res.ishan
}


const teamList = ref([]);

/**
 * 搜索队伍
 * @param val
 * @returns {Promise<void>}
 */
const listTeam = async (val = '') => {
  const res = await myAxios.get("/team/list/my/create", {
    params: {
      searchText: val,
      pageNum: 1,
    },
  });
  if (res?.code === 0) {
    teamList.value = res.data;
  } else {
    Toast.fail('加载队伍失败，请刷新重试');
  }
}


// 页面加载时只触发一次
onMounted( () => {
  listTeam();
  love();
})

const onSearch = (val) => {
  listTeam(val);
};

</script>

<style scoped>
</style>
