<script setup lang="ts">
import {onMounted, ref} from "vue";
import {useRouter,useRoute} from "vue-router";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";


const router = useRouter();
const route = useRoute();

const id = route.query.id;
// 需要用户填写的表单数据
const addTeamData = ref({})

//展示日期选择
const showCalendar = ref(false);
const onConfirm = (date) => {
  let month: string | number = date.getMonth() + 1;
  month = month < 10 ? "0" + month : month;
  let day = date.getDate();
  day = day < 10 ? "0" + day : day;
  addTeamData.value.expireTime = `${date.getFullYear()}-${month}-${day}`;
  showCalendar.value = false;
};

// 获取之前的队伍信息
onMounted(async () => {
  if (id <= 0) {
    showFailToast('加载队伍失败')
    return;
  }
  const res = await myAxios.get("/team/get", {
    params: {
      id,
    }
  });
  if (res?.code === 0) {
    addTeamData.value = res.data;
    addTeamData.value.status=String(res.data.status)
    addTeamData.value.expireTime=res.data.expireTime.substring(0, res.data.expireTime.indexOf('T'))
    addTeamData.value.password=''
  } else {
    showFailToast('加载队伍失败，请刷新重试');
  }}
)


// 提交  修改队伍信息
const onSubmit = async () => {
  const postData = {
    ...addTeamData.value,
    status: Number(addTeamData.value.status)
  }
  // todo 前端参数校验
  const res = await myAxios.post("/team/update", postData);
  if (res?.code === 0 && res.data){
    showSuccessToast("修改成功");
     router.push({
      path: '/user/team/create',
       //不能回退
      replace: true,
    });
  } else {
    showFailToast("添加失败")
  }
}
</script>

<template>
  <van-notice-bar color="#1989fa" background="#ecf9ff" left-icon="info-o">
    若不设置过期时间则永久有效
  </van-notice-bar>
  <div id="teamAddPage">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
            v-model="addTeamData.name"
            name="name"
            label="队伍名"
            placeholder="请输入队伍名"
            :rules="[{ required: true, message: '请输入队伍名' }]"
        />
        <van-field
            v-model="addTeamData.description"
            rows="4"
            autosize
            label="队伍描述"
            type="textarea"
            placeholder="请输入队伍描述"
        />
        <van-field
            v-model="addTeamData.expireTime"
            is-link
            readonly
            name="calendar"
            label="过期时间"
            placeholder="点击选择过期时间"
            @click="showCalendar = true"
        />
        <van-calendar v-model:show="showCalendar" @confirm="onConfirm" />
        <van-field name="radio" label="队伍状态">
          <template #input>
            <van-radio-group v-model="addTeamData.status" direction="horizontal">
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">私有</van-radio>
              <van-radio name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
            v-if="Number(addTeamData.status) === 2"
            v-model="addTeamData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入队伍密码"
            :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>

</style>