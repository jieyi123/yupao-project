import {UserType} from "./user";

/**
 * 队伍类别
 */
export type TeamType = {
    id: number;
    name: string;
    description: string;
    userId: number;
    expireTime?: Date;
    maxNum: number;
    password?: string,
    // todo 定义枚举值类型，更规范
    status: number;
    createTime: Date;
    updateTime: Date;
    /**
     * 用户是否已加入
     */
    hasJoin?:boolean;
    createUser?: UserType;
    /**
     * 加入人数
     */
    hasJoinNum?: number;
    /**
     * 加入用户的信息列表
     */
    joinUserList?: UserType[]
};
