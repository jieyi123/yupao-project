<script setup lang="ts">
import {onMounted, ref} from "vue";
import {showFailToast} from "vant";
import myAxios from "../plugins/myAxios.ts";
import TeamCardList from "../components/TeamCardList.vue";
import {useRouter} from "vue-router";
const router=useRouter();

// const active = ref('public')
const searchText = ref('');


/**
 * 切换查询状态
 * @param name
 */
// const onTabChange = (name) => {
//   // 查公开
//   if (name === 'public') {
//     listTeam(searchText.value, 0);
//   } else {
//     // 查加密
//     listTeam(searchText.value, 2);
//   }
// }


const teamList = ref([]);

/**
 * 搜索队伍
 * @param val
 * @param status
 * @returns {Promise<void>}
 */
const listTeam = async (val = '' ) => {
  const res = await myAxios.get("/team/list", {
    params: {
      searchText: val,
      pageNum: 1,
    },
  });
  if (res?.code === 0) {
    teamList.value = res.data;
  } else {
    showFailToast('加载队伍失败，请刷新重试')
  }
}

// 搜索
const search = () => {
  router.push({
    path: '/teamResult',
    query: {
      value: searchText.value
    }
  })
}

// 页面加载时只触发一次
onMounted(() => {
  listTeam();
})


// const onSearch = (val) => {
//   listTeam(val);
// };

//创建队伍
const jumpAddTeam = () => {
  router.push({
    path: "/team/add"
  })
}

// 已加入队伍
const jumpAddTeamResult = () => {
  router.push({
    path: '/user/team/join'
  })
}

// 创建的队伍列表
const jumpTeamCreatePage = () => {
  router.push({
    path: '/user/team/create'
  })
}

</script>

<template>
  <div id="teamPage">
    <div class="container">
      <div class="search">
        <van-search v-model="searchText" show-action placeholder="搜索队伍">
          <template #action>
            <span @click="search">搜索</span>
          </template>
        </van-search>
      </div>
      <div class="button">
        <van-row justify="center">
          <van-col span="8">
            <van-button type="primary" @click="jumpAddTeam()"
            >创建队伍</van-button
            ></van-col
          >
          <van-col span="8">
            <van-button type="primary" @click="jumpAddTeamResult()"
            >已加入队伍</van-button
            ></van-col
          >
          <van-col span="8">
            <van-button type="primary" @click="jumpTeamCreatePage()"
            >已创建队伍</van-button
            ></van-col
          >
        </van-row>
      </div>
      <div class="team-list">
        <h2>推荐队伍</h2>
        <team-card-list :teamList="teamList"/>
        <van-empty v-if="teamList?.length < 1" description="数据为空"/>
      </div>
<!--      <van-tabs v-model:active="active" @change="onTabChange">-->
<!--        <van-tab title="公开" name="public"/>-->
<!--        <van-tab title="加密" name="private"/>-->
<!--      </van-tabs>-->
<!--      <div style="margin-bottom: 16px"/>-->

    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  .button {
    margin-top: 14px;

    .van-button {
      box-shadow: 0 2px 10px 0 rgba(24, 22, 27, 0.5);
    }

    .van-col {
      text-align: center;
    }
  }

  .team-list {
    padding: 10px;
    margin-top: 10px;

    h2 {
      margin-bottom: 20px;
      color: #7232dd;
      font-size: large;
    }
  }
}
</style>