package com.pjieyi.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pjieyi.yupao.common.BaseResponse;
import com.pjieyi.yupao.common.DeleteRequest;
import com.pjieyi.yupao.common.ErrorCode;
import com.pjieyi.yupao.common.ResultUtils;
import com.pjieyi.yupao.exception.BusinessException;
import com.pjieyi.yupao.model.dto.team.TeamAddRequest;
import com.pjieyi.yupao.model.dto.team.TeamJoinRequest;
import com.pjieyi.yupao.model.dto.team.TeamQueryRequest;
import com.pjieyi.yupao.model.dto.team.TeamUpdateRequest;
import com.pjieyi.yupao.model.entity.Team;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.model.entity.UserTeam;
import com.pjieyi.yupao.model.vo.TeamUserVO;
import com.pjieyi.yupao.model.vo.UserVO;
import com.pjieyi.yupao.service.TeamService;
import com.pjieyi.yupao.service.UserService;
import com.pjieyi.yupao.service.UserTeamService;
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

    @Resource
    private UserTeamService userTeamService;

    // region 增删改查
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
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改队伍信息失败");
        }
        return ResultUtils.success(true);
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
        boolean result=teamService.joinTeam(teamJoinRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"加入队伍失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 退出队伍
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if (deleteRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result=teamService.quitTeam(deleteRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"退出失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 队长解散队伍
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if (deleteRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result=teamService.deleteTeam(deleteRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解散队伍失败");
        }
        return ResultUtils.success(true);
    }


    /**
     * 获取当前用户创建的队伍信息
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO>teamUserVOList=teamService.listMyCreateTeams(loginUser);
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 获取当前用户加入的队伍信息(不包含自己创建的队伍)
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamUserVOList=teamService.listMyJoinTeams(loginUser);
        return ResultUtils.success(teamUserVOList);
    }

    // endregion
}
