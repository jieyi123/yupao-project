<script setup lang="ts">
import dayjs from "dayjs";
import {teamStatusEnum} from "../constants/team.ts";
import ikun from "../assets/ikun.png"
import useUserTeamInfoStore from '../stores/store'


const userTeamInfoStore=useUserTeamInfoStore();

const teamInfoParse = userTeamInfoStore.info
teamInfoParse.joinUserList.forEach((item: any) => {
  item.tags = JSON.parse(item.tags)
})
</script>

<template>
  <div class="container">
    <div class="top">
      <div class="top-cover">
        <van-image
            width="10rem"
            height="10rem"
            fit="cover"
            position="center"
            :src="ikun"
        />
      </div>
      <div class="top-title">
        <van-row>
          <van-col span="24">
            <div class="title">{{ teamInfoParse.name }}</div>
            <div class="time">
              {{
                '创建时间: ' +
                dayjs(teamInfoParse.createTime).format('YYYY-MM-DD')
              }}
              <br />
              {{
                '过期时间: ' +
                (teamInfoParse.expireTime === null
                    ? '永久'
                    : dayjs(teamInfoParse.expireTime).format('YYYY-MM-DD'))
              }}
            </div>
            <div class="max">
              {{ '最大人数: ' + teamInfoParse.maxNum }}
              <br />
              {{ '已加入人数: ' + teamInfoParse.hasJoinNum }}
            </div>
            <div class="creater">
              创建者:
              {{
                teamInfoParse.createUser.userName
                    ? teamInfoParse.createUser.userName
                    : teamInfoParse.createUser.userAccount
              }}
            </div>
            <div class="status">
              状态:
              <van-tag color="#7232dd" plain>{{
                  teamStatusEnum[
                      teamInfoParse.status as keyof typeof teamStatusEnum
                      ]
                }}</van-tag>
            </div>
          </van-col>
        </van-row>
      </div>
    </div>
    <div class="description">
      简介：<span>{{ teamInfoParse.description }}</span>
    </div>
    <div class="join-info">
      <h2>小队成员</h2>
      <van-card
          v-for="user in teamInfoParse.joinUserList"
          :desc="user.profile"
          :title="`${user.userName}`"
          :thumb="user.userAvatar"
          :key="user.id"
      >
        <template #tags>
          <van-tag
              plain
              type="danger"
              v-for="(tag, index) in user.tags"
              style="margin-right: 8px; margin-top: 8px"
              :key="index"
          >
            {{ tag }}
          </van-tag>
        </template>
        <template #footer>
          <van-button size="mini">联系我</van-button>
        </template>
      </van-card>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  .top {
    display: flex;
    .top-cover {
      padding: 10px;
    }
    .top-title {
      .title {
        font-size: 20px;
        font-weight: bold;
        text-align: center;
        margin-top: 10px;
      }
      .time {
        margin-top: 10px;
      }
    }
  }
  .description {
    padding: 10px;
    span {
      color: #8a8a8a;
    }
  }
  .join-info {
    padding: 10px;
    margin-bottom: 50px;
    h2 {
      margin-bottom: 20px;
      color: #7232dd;
      font-size: large;
    }
  }
}
</style>