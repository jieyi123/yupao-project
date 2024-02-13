// 2. 定义一些路由
// 每个路由都需要映射到一个组件。
// 我们后面再讨论嵌套路由。
import Index from "../pages/Index.vue";
import Team from "../pages/TeamPage.vue";
import User from "../pages/UserPage.vue";
import Search from "../pages/SearchPage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import SearchResultPage from "../pages/SearchResultPage.vue";

const routes = [
    { path: '/',  component: Index },
    { path: '/team',title: '找队伍', component: Team },
    { path: '/user',title: '个人信息', component: User },
    { path: '/search',title: '找伙伴', component: Search },
    { path: '/user/update', title: '更新信息', component: UserUpdatePage },
    { path: '/user/edit', title: '编辑信息', component: UserEditPage },
    { path: '/user/list', title: '用户列表', component: SearchResultPage },
]
export default routes;