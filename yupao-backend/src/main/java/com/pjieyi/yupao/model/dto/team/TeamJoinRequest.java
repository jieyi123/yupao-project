package com.pjieyi.yupao.model.dto.team;

import lombok.Data;
import java.io.Serializable;

/**
 * 加入队伍DTO
 */
@Data
public class TeamJoinRequest implements Serializable {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

    private static final long serialVersionUID = 1L;
}