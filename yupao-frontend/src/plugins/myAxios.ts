import axios, {AxiosInstance} from "axios";
import {showFailToast, showNotify} from "vant";

const isDev = process.env.NODE_ENV === 'development';
const myAxios: AxiosInstance = axios.create({
    baseURL: isDev ? 'http://127.0.0.1:7529/api' : 'http://partner-backend.originai.icu/api',
});

myAxios.defaults.withCredentials = true; // 配置为true 发送请求的时候携带cookie

// Add a request interceptor
myAxios.interceptors.request.use(function (config) {
    //console.log('我要发请求啦', config)
    // Do something before request is sent
    return config;
}, function (error) {
    // Do something with request error
    return Promise.reject(error);
});

// Add a response interceptor
myAxios.interceptors.response.use(function (response) {

    //console.log('我收到你的响应啦', response)
    // 未登录则跳转到登录页
    if (response?.data?.code === 40100) {
        const redirectUrl = window.location.href;
        window.location.href = `/user/login?redirect=${redirectUrl}`;
        showFailToast('请先登录');
    }
    // Do something with response data
    return response.data;
}, function (error) {
    // Do something with response error
    return Promise.reject(error);
});
export default myAxios;