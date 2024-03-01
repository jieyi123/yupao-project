<script setup lang="ts">
import {useRouter} from "vue-router";
import {ref} from "vue";
import routes from "../config/route.ts";
import myAxios from "../plugins/myAxios.ts";

const router = useRouter();
const onClickLeft = () => {
  //返回上一页
  router.back()
};
const onClickRight = () => {
  router.push('/search')
};
// const active = ref('index');

const DEFAULT_TITLE = '伙伴匹配';
const title = ref(DEFAULT_TITLE);
const hasMessage = ref(false)
const active = ref(0)
/**
 * 根据路由切换标题
 */
router.beforeEach(async (to) => {
  const toPath = to.path;
  const route = routes.find((route) => {
    return toPath == route.path;
  })
  title.value = route?.title ?? DEFAULT_TITLE;
  // if (to.path !== '/user/login' ) {
  //   let res = await myAxios.get("/user/current");
  //   if (res?.code === 0) {
  //     if (res.data) {
  //       hasMessage.value = true
  //     } else {
  //       hasMessage.value = false
  //     }
  //   }
  // }
})
</script>

<template>

  <van-sticky>
    <van-nav-bar
        :title="title"
        left-text="返回"
        left-arrow
        @click-left="onClickLeft"
        @click-right="onClickRight"
    >
      <template #right>
        <van-icon name="search" size="18"/>
      </template>
    </van-nav-bar>
  </van-sticky>

  <div id="content">
    <router-view/>
  </div>
  <van-tabbar v-model="active" route>
    <van-tabbar-item to="/" name="index">
      <span>主页</span>
      <template #icon>
        <van-icon name="wap-home"/>
      </template>
    </van-tabbar-item>
    <van-tabbar-item to="/team" name="team" badge="3">队伍
      <template #icon>
        <van-icon name="font"/>
      </template>
    </van-tabbar-item>
    <van-tabbar-item to="/user" name="user">
      个人
      <template #icon>
        <van-icon name="user"/>
      </template>
    </van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>
#content {
  padding-bottom: 50px;
}
</style>