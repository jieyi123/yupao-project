package com.pjieyi.yupao.service;

import com.pjieyi.yupao.common.BaseResponse;
import com.pjieyi.yupao.model.dto.team.TeamAddRequest;
import com.pjieyi.yupao.model.dto.team.TeamJoinRequest;
import com.pjieyi.yupao.model.dto.team.TeamQueryRequest;
import com.pjieyi.yupao.model.dto.team.TeamUpdateRequest;
import com.pjieyi.yupao.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author pjy17
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-02-17 09:56:10
*/
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team
     * @param loginUser
     * @return 当前队伍id
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 查询队伍信息
     * @param teamQueryRequest
     * @param loginUser
     * @return
     */
    List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, User loginUser);

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
}
