<script setup lang="ts">
import BasicLayout from "./layouts/BasicLayout.vue";
import {useRoute} from "vue-router";
import {computed} from "vue";
let route = useRoute();
const default_layout = "default"
const isRouterAlive = true
const layout = computed(() => {
  return (route.meta.layout || default_layout) + '-layout'
})
</script>

<template>
  <div id="app">
    <component :is="layout">
      <router-view v-if="isRouterAlive"/>
    </component>
  </div>
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
