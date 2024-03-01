<script setup lang="ts">
import {TeamType} from "../models/team";
import {onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {getCurrentUser} from "../services/user";
import myAxios from "../plugins/myAxios";
import {showFailToast, showSuccessToast} from "vant";
import {teamStatusEnum} from "../constants/team.ts";
import ikun from "../assets/ikun.png"
import dayjs from 'dayjs'
import useUserTeamInfoStore from '../stores/store'
const userTeamInfoStore=useUserTeamInfoStore();

interface TeamCardListProps {
  teamList: TeamType[];
}

const props = withDefaults(defineProps<TeamCardListProps>(), {
  // @ts-ignore
  teamList: [] as TeamType[],
});

const showPasswordDialog = ref(false);
const password = ref('');
const joinTeamId = ref(0);
const currentUser = ref();

const router = useRouter();

onMounted(async () => {
  currentUser.value = await getCurrentUser();
})

const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true;
  }
}

const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
}

/**
 * 加入队伍
 */
const doJoinTeam = async () => {
  if (!joinTeamId.value) {
    return;
  }
  const res = await myAxios.post('/team/join', {
    teamId: joinTeamId.value,
    password: password.value
  });
  if (res?.code === 0) {
    showSuccessToast('加入成功');
    doJoinCancel();
    window.location.reload();
  } else {
    showFailToast('加入失败' + (res.message ? `，${res.message}` : ''));
  }
}

// 查看队伍详情
const doCheckInfo = (userTeam : TeamType) => {
  //const team = teamList.value.find((item: TeamType) => item.id === id)
  userTeamInfoStore.setInfo(userTeam);
  if (userTeam) {
    router.push({
      path: '/teamInfo',
      // query: {
      //   teamInfo: JSON.stringify(userTeam)
      // }
    })
  }
}

/**
 * 跳转至更新队伍页
 * @param id
 */
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/update',
    query: {
      id,
    }
  })
}

/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quit', {
    id: id
  });
  if (res?.code === 0) {
    showSuccessToast('操作成功');
    window.location.reload();
  } else {
    showFailToast('操作失败' + (res.message ? `，${res.message}` : ''));
  }
}

/**
 * 解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/delete', {
    id,
  });
  if (res?.code === 0) {
    showSuccessToast('操作成功');
    window.location.reload();
  } else {
    showFailToast('操作失败' + (res.message ? `，${res.message}` : ''));
  }
}
</script>

<template>
  <div
      id="teamCardList"
  >
      <van-card
          v-for="team in props.teamList"
          :thumb="ikun"
          :desc="team.description"
          :title="`${team.name}`"
      >
        <template #tags>
          <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px">
            {{
              teamStatusEnum[team.status]
            }}
          </van-tag>
        </template>
        <template #bottom>
          <div>
            {{ `最大人数: ${team.maxNum}` }}
            &nbsp;&nbsp;
            {{ '已加入人数: ' + team.hasJoinNum }}
          </div>
          <div>
            {{ team.expireTime===null
              ? '过期时间: 永久'
              :
              '过期时间: ' + dayjs(team.expireTime).format('YYYY-MM-DD HH:mm')}}
          </div>
          <div>
            {{ '创建时间: ' + dayjs(team.createTime).format('YYYY-MM-DD HH:mm') }}
          </div>
        </template>
        <template #footer>
          <van-button
              size="small"
              plain
              type="success"
              @click="doCheckInfo(team)"
          >
            查看详情</van-button
          >

          <van-button size="small" type="primary" v-if="team.userId !== currentUser?.id && !team.hasJoin" plain
                      @click="preJoinTeam(team)">
            加入队伍
          </van-button>
          <van-button v-if="team.userId === currentUser?.id" size="small" plain
                      @click="doUpdateTeam(team.id)">更新队伍
          </van-button>
          <!-- 仅加入队伍可见 -->
          <van-button v-if="team.userId !== currentUser?.id && team.hasJoin" size="small" plain
                      @click="doQuitTeam(team.id)">退出队伍
          </van-button>
          <van-button v-if="team.userId === currentUser?.id" size="small" type="danger" plain
                      @click="doDeleteTeam(team.id)">解散队伍
          </van-button>
        </template>
      </van-card>
    <van-dialog v-model:show="showPasswordDialog" title="请输入密码" show-cancel-button @confirm="doJoinTeam"
                @cancel="doJoinCancel">
      <van-field v-model="password" placeholder="请输入密码"/>
    </van-dialog>
  </div>
</template>

<style scoped>

</style>