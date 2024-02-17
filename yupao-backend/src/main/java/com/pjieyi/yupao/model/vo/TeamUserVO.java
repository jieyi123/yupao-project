package com.pjieyi.yupao.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author pjieyi
 * @desc 队伍用户信息封装类
 */
@Data
public class TeamUserVO implements Serializable {
    private static final long serialVersionUID = -2664875296233182882L;

    /**
     * id
     */
    private Long id;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人信息
     */
    private UserVO createUser;

}
