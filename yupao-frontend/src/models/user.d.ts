/**
 * 用户类别
 */
export type UserType = {
    id: number;
    userName: string;
    userAccount: string;
    userAvatar?: string;
    profile?: string;
    gender:number;
    phone: string;
    email: string;
    userStatus: number;
    userRole: string;
    tags: string;
    createTime: Date;
};
