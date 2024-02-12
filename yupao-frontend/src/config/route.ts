// 2. 定义一些路由
// 每个路由都需要映射到一个组件。
// 我们后面再讨论嵌套路由。
import Index from "../pages/Index.vue";
import Team from "../pages/TeamPage.vue";
import User from "../pages/UserPage.vue";
import Search from "../pages/SearchPage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import UserEditPage from "../pages/UserEditPage.vue";

const routes = [
    { path: '/', component: Index },
    { path: '/team', component: Team },
    { path: '/user', component: User },
    { path: '/search', component: Search },
    { path: '/user/update', title: '更新信息', component: UserUpdatePage },
    { path: '/user/edit', title: '编辑信息', component: UserEditPage },
]
export default routes;