package com.pjieyi.yupao.model.dto.team;

import com.pjieyi.yupao.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询队伍DTO
 */
@Data
public class TeamQueryRequest extends PageRequest implements Serializable {


    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    private String searchText;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

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
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}