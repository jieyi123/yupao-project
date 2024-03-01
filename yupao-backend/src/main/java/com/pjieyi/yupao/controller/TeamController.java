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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * 推荐队伍
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
        List<TeamUserVO> teamUserVOList = teamService.listTeams(teamQueryRequest, loginUser);
        getJoinNumAndIsJoin(loginUser, teamUserVOList);
        //筛选出未加入的队伍
        teamUserVOList=teamUserVOList.stream().filter(teamUserVO -> !teamUserVO.isHasJoin()).collect(Collectors.toList());
        return ResultUtils.success(teamUserVOList);
    }


    //  前端界面展示 队伍人数 和当前用户是否加入队伍 队伍用户信息
    private void getJoinNumAndIsJoin(User loginUser, List<TeamUserVO> teamUserVOList) {
        if (teamUserVOList==null || teamUserVOList.size()==0){
            return;
        }

        //队伍id集合
        List<Long> userTeamIds = teamUserVOList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        //获取队伍已加入人数
        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        userTeamQueryWrapper.in("teamId",userTeamIds);
        //key-->teamId  value-->UserTeamList
        Map<Long, List<UserTeam>> listMap = userTeamService.list(userTeamQueryWrapper).stream().collect(Collectors.groupingBy(UserTeam::getTeamId));


        //判断用户是否加入队伍 (isJoin前端按钮展示)
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.in("teamId",userTeamIds);
        //当前用户已加入的队伍的teamId集合(包括自己创建的用户)
        Set<Long> hasJoinTeamIdSet = userTeamService.list(queryWrapper).stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamUserVOList.forEach(teamUserVO -> {
            //是否加入队伍
            boolean hasJoin=hasJoinTeamIdSet.contains(teamUserVO.getId());
            teamUserVO.setHasJoin(hasJoin);
            Long teamId = teamUserVO.getId();
            //队伍人数
            teamUserVO.setHasJoinNum(listMap.getOrDefault(teamId,new ArrayList<>()).size());
            //队伍用户信息
            //队伍人员信息
            List<UserVO> teamUsers=new ArrayList<>();
            Set<Long> userIds = listMap.get(teamUserVO.getId()).stream().map(UserTeam::getUserId).collect(Collectors.toSet());
            userService.listByIds(userIds).forEach(user -> {
                UserVO userVO=new UserVO();
                BeanUtils.copyProperties(user,userVO);
                teamUsers.add(userVO);
            });
            teamUserVO.setJoinUserList(teamUsers);
        });
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id,HttpServletRequest request){
        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(teamService.getById(id));
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
        List<TeamUserVO> teamUserVOList=teamService.listMyCreateTeams(loginUser);
        getJoinNumAndIsJoin(loginUser,teamUserVOList);
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
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<TeamUserVO> teamUserVOList=teamService.listMyJoinTeams(loginUser);
            getJoinNumAndIsJoin(loginUser, teamUserVOList);
        return ResultUtils.success(teamUserVOList);
    }

    // endregion
}
