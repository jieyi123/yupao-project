import myAxios from "../plugins/myAxios";
import {setCurrentUserState} from "../states/user.ts";


export const getCurrentUser = async () => {
    // const currentUser = getCurrentUserState();
    // if (currentUser) {
    //     return currentUser;
    // }
    // 不存在则从远程获取  如果用户需求量不大 可以每次获取用户从新发送请求加载
    const res = await myAxios.get('/user/current');
    if (res.code === 0) {
        setCurrentUserState(res.data);
        return res.data;
    }
    return null;
}

