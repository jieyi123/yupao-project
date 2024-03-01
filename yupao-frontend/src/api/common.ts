import {request} from "../utils/http.ts";


/** 搜索结果页上方情话 GET https://api.vvhan.com/api/love */
export async function getLove() {
    return await request.get<any>('https://api.vvhan.com/api/love?type=json')
}