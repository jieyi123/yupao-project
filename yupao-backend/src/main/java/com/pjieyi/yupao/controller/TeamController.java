package com.pjieyi.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pjieyi.yupao.common.BaseResponse;
import com.pjieyi.yupao.common.ErrorCode;
import com.pjieyi.yupao.common.ResultUtils;
import com.pjieyi.yupao.exception.BusinessException;
import com.pjieyi.yupao.model.dto.team.TeamAddRequest;
import com.pjieyi.yupao.model.dto.team.TeamJoinRequest;
import com.pjieyi.yupao.model.dto.team.TeamQueryRequest;
import com.pjieyi.yupao.model.dto.team.TeamUpdateRequest;
import com.pjieyi.yupao.model.entity.Team;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.model.vo.TeamUserVO;
import com.pjieyi.yupao.model.vo.UserVO;
import com.pjieyi.yupao.service.TeamService;
import com.pjieyi.yupao.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pjieyi
 * @desc 队伍相关接口
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    /**
     * 新增队伍
     * @param teamAddRequest
     * @return 队伍id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest,HttpServletRequest request){
        if (teamAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team=new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        Long teamId = teamService.addTeam(team,userService.getLoginUser(request));
        if (teamId<=0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(teamId);
    }

    /**
     * 查询队伍信息
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if (teamQueryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(teamService.listTeams(teamQueryRequest,loginUser));
    }

    /**
     * 分页查询队伍信息
     * @param teamQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listPageTeams(TeamQueryRequest teamQueryRequest, HttpServletRequest request){
        if (teamQueryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Team team=new Team();
        BeanUtils.copyProperties(team,teamQueryRequest);
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        Page<Team> teamPage=teamService.page(new Page<>(teamQueryRequest.getCurrent(),teamQueryRequest.getPageSize()),queryWrapper);
        //需要优化
        //List<TeamUserVO> teamUserVOList = teamPage.getRecords().stream().map(teamInfo -> {
        //    TeamUserVO teamUserVO = new TeamUserVO();
        //    BeanUtils.copyProperties(teamInfo, teamUserVO);
        //    return teamUserVO;
        //}).collect(Collectors.toList());
        return ResultUtils.success(teamPage);
    }

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param request
     * @return 修改结果
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean res=teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(res);
    }
}
