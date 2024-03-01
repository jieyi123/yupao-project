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
import UserLoginPage from "../pages/UserLoginPage.vue";
import CreateTeamPage from "../pages/CreateTeamPage.vue";
import UpdateTeamPage from "../pages/UpdateTeamPage.vue";
import UserTeamCreatePage from "../pages/UserTeamCreatePage.vue";
import UserTeamJoinPage from "../pages/UserTeamJoinPage.vue";
import TeamInfo from "../pages/TeamInfo.vue";
import UserRegisterPage from "../pages/UserRegisterPage.vue";
import UserTagPage from "../pages/UserTagPage.vue";
import UpdatePasswordPage from "../pages/UpdatePasswordPage.vue";
import ForgetPasswordPage from "../pages/ForgetPasswordPage.vue";

const routes = [
    { path: '/',  component: Index },
    { path: '/team',title: '找队伍', component: Team },
    { path: '/team/add', title: '创建队伍', component: CreateTeamPage },
    { path: '/team/update', title: '修改队伍', component: UpdateTeamPage },
    {path: '/user/tag', title: "标签", component: UserTagPage},
    { path: '/user',title: '个人信息', component: User },
    { path: '/user/login', title: '登录', component: UserLoginPage },
    { path: '/user/register', title: '注册', component: UserRegisterPage, meta: { allowAnonymous: true }, },
    { path: '/user/update/password', title: '修改密码', component: UpdatePasswordPage, meta: {layout: 'password'} },
    { path: '/search',title: '找伙伴', component: Search },
    { path: '/user/update', title: '更新信息', component: UserUpdatePage },
    { path: '/user/edit', title: '编辑信息', component: UserEditPage ,meta: {layout: 'userEdit'}},
    {path: '/user/forget', title: '找回密码', component: ForgetPasswordPage, meta: {layout: 'forget'}},
    { path: '/user/list', title: '用户列表', component: SearchResultPage },
    { path: '/user/team/join', title: '我加入的队伍', component: UserTeamJoinPage },
    { path: '/user/team/create', title: '我创建的队伍', component: UserTeamCreatePage },
    { path: '/teamInfo', title: '队伍详情', component: TeamInfo },
]
export default routes;