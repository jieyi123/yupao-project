import { createApp } from 'vue'
import App from './App.vue'
import * as VueRouter from 'vue-router';
import routes from "./config/route.ts";
import 'vant/es/toast/style';
import {Skeleton} from "vant";
import '../global.css'
import 'vant/es/notify/style';
import {createPinia} from "pinia";
import { Notify } from 'vant';
import './ct4.js'
import BasicLayout from "./layouts/BasicLayout.vue";
import UpdatePasswordPage from "./pages/UpdatePasswordPage.vue";
import UserEditPage from "./pages/UserEditPage.vue";
import ForgetPasswordPage from "./pages/ForgetPasswordPage.vue";

const app=createApp(App);

const pinia=createPinia();

app.use(pinia)


// 3. 创建路由实例并传递 `routes` 配置
const router = VueRouter.createRouter({
    // 4. 内部提供了 history 模式的实现
    history: VueRouter.createWebHistory(),
    routes, // `routes: routes` 的缩写
})
app.use(Notify);
app.use(router);
app.use(Skeleton);
app.component("default-layout", BasicLayout)
app.component("password-layout", UpdatePasswordPage)
app.component("userEdit-layout", UserEditPage)
app.component("forget-layout", ForgetPasswordPage)
app.mount('#app');
