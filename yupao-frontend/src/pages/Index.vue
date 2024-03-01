<script setup lang="ts">
import {showFailToast} from "vant";
import myAxios from "../plugins/myAxios.ts";
import {ref} from "vue";
import UserCardList from "../components/UserCardList.vue";

const isMatchMode = ref<boolean>(false);

const userList = ref([]);
const userSearch = ref("")
const currentPage = ref(0)
const listLoading = ref(false)
const searching = ref(false)
const listFinished = ref(false)
const refreshLoading = ref(false)

//主页图片
const images = [
  'https://pjieyi.oss-cn-chengdu.aliyuncs.com/YUPAO/bbc58c3d998d4f389fd.jpg',
  'https://pjieyi.oss-cn-chengdu.aliyuncs.com/YUPAO/895a30549fc64e57a8b0b09f0740e98c.jpg',
  'https://pjieyi.oss-cn-chengdu.aliyuncs.com/YUPAO/a6fec42e882111ebb6edd017c2d2eca2.jpg',
  'https://pjieyi.oss-cn-chengdu.aliyuncs.com/YUPAO/f0b7222a7fe3437381651484a898cd31.jpg'
]


const xindong = async () => {
  // 心动模式，根据标签匹配用户
  if (isMatchMode.value) {
    //前10个标签相似的用户
    const num = 10;
    const userDataList = await myAxios.get('/user/match', {
      params: {
        num,
      },
    })
    console.log(userDataList)
    if (userDataList?.data) {
      userDataList.data.forEach(user => {
        if (user.tags) {
          user.tags = JSON.parse(user.tags)
        }
      })
    }
    userList.value = [];
    for (let i = 0; i < userDataList.data.length; i++) {
      userList.value.push(userDataList.data[i])
    }
  } else {
    getUserList(1)
  }
}


async function getUserList(currentPage) {
  const userListData = await myAxios.get("/user/list/page", {
    params: {
      currentPage: currentPage,
      userName: userSearch.value
    }
  })
  if (userListData?.code === 0) {
  } else {
    showFailToast("加载失败" + (userListData.message ? `,${userListData.message}` : ''))
  }
  if (userListData?.data.records.length === 0) {
    listFinished.value = true
    return
  }
  if (userListData?.data.records) {
    userListData.data.records.forEach(user => {
      if (user.tags) {
        user.tags = JSON.parse(user.tags)
      }
    })
    for (let i = 0; i < userListData.data.records.length; i++) {
      userList.value.push(userListData.data.records[i])
    }
  }
}

const onLoad = async () => {
  if (isMatchMode.value) {
    listLoading.value = false;
    return
  }
  currentPage.value++
  await getUserList(currentPage.value)
  listLoading.value = false;
}
const onRefresh = async () => {
  currentPage.value = 1
  userList.value = []
  listFinished.value = false
  await getUserList(currentPage.value)
  refreshLoading.value = false
  listLoading.value = false;
}

const searchUser = async () => {
  searching.value = true
  userList.value = []
  currentPage.value = 1
  await getUserList(currentPage.value)
  searching.value = false
}
</script>

<template>

  <van-notice-bar
      color="#1989fa"
      background="#ecf9ff"
      left-icon="volume-o"
      style="margin-bottom: 10px"
      text="富强、民主、文明、和谐；自由、平等、公正、法治；爱国、敬业、诚信、友善。"
  />
  <div style="position: relative;height: 100%;width: 100%">
    <!--    <div class="home-swipe">-->
    <!--      <van-swipe :autoplay="5000" lazy-render>-->
    <!--        <van-swipe-item v-for="image in images" :key="image">-->
    <!--          <img :src="image" alt=""/>-->
    <!--        </van-swipe-item>-->
    <!--      </van-swipe>-->
    <!--    </div>-->
    <van-swipe class="my-swipe" :autoplay="5000" indicator-color="white" lazy-render
               style="width: 90%;height: 150px;margin: 0 auto">
      <van-swipe-item v-for="image in images" :key="image">
        <img :src="image" style="width: 100%;height: 150px" alt=""/>
      </van-swipe-item>

    </van-swipe>
    <van-search v-model="userSearch" placeholder="请输入搜索关键词" @search="searchUser"/>
    <div class="home-recommend">
      <h4>
        推荐用户
        <span>心动模式&nbsp;<van-switch v-model="isMatchMode" :onclick="xindong"/></span>

      </h4>
      <van-pull-refresh
          v-model="refreshLoading"
          success-text="刷新成功"
          @refresh="onRefresh">
        <van-list
            v-model:loading="listLoading"
            :finished="listFinished"
            offset="0"
            @load="onLoad"
        >
          <template #finished>
            <span v-if="!searching">没有更多了</span>
          </template>
          <UserCardList :user-list="userList" :loading="listLoading"/>
        </van-list>
        <van-back-top right="20px" bottom="60px"/>
        <van-empty v-if="(!userList ||　userList.length===0) && !listLoading && !searching" image="search"
                   description="暂无用户"/>
      </van-pull-refresh>
      <!--      <van-empty v-if="!userList || userList.length < 1" description="暂无用户"/>-->
    </div>
  </div>
</template>

<style scoped lang="scss">

.home-top {
  padding: 10px;
}

.home-swipe {
  height: 200px;

  img {
    width: 100%;
    height: 100%;
  }
}

.home-recommend {
  padding: 10px;
  padding-bottom: 80px;

  h4 {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 5px;
    color: #7232dd;
    font-size: large;
    margin-top: -5px;

    span {
      display: flex;
      align-items: center;
    }
  }
}

.my-swipe .van-swipe-item {
  color: #fff;
  font-size: 20px;
  line-height: 150px;
  text-align: center;
  background-color: #39a9ed;
}

.my-swipe {
  margin: 15px;
  border-radius: 3%;
}
</style>