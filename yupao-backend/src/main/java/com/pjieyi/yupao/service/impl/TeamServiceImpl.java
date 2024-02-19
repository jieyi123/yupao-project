package com.pjieyi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pjieyi.yupao.common.DeleteRequest;
import com.pjieyi.yupao.common.ErrorCode;
import com.pjieyi.yupao.exception.BusinessException;
import com.pjieyi.yupao.mapper.TeamMapper;
import com.pjieyi.yupao.model.dto.team.TeamJoinRequest;
import com.pjieyi.yupao.model.dto.team.TeamQueryRequest;
import com.pjieyi.yupao.model.dto.team.TeamUpdateRequest;
import com.pjieyi.yupao.model.entity.Team;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.model.entity.UserTeam;
import com.pjieyi.yupao.model.enums.TeamStatusEnum;
import com.pjieyi.yupao.model.vo.TeamUserVO;
import com.pjieyi.yupao.model.vo.UserVO;
import com.pjieyi.yupao.service.TeamService;
import com.pjieyi.yupao.service.UserService;
import com.pjieyi.yupao.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.pjieyi.yupao.constant.CommonConstant.SALT;
import static com.pjieyi.yupao.constant.UserConstant.ADMIN_ROLE;

/**
* @author pjy17
* @desc 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-02-17 09:56:10
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
    @Resource
    private UserTeamService userTeamService;
    //注意循环调用
    @Resource
    private UserService userService;

    /**
     * 新增队伍
     * @return 当前队伍id
     */
    @Override
    @Transactional
    public Long addTeam(Team team, User loginUser){
        //1.验证是否登录
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        team.setUserId(loginUser.getId());
        //2.校验参数
        checkAddTeamParams(team);
        //  g. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
        Long userId = team.getUserId();
        queryWrapper.eq("userId", userId);
        long teamCount = this.count(queryWrapper);
        if (teamCount>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多创建5个队伍");
        }
        //3.插入队伍信息到队伍表
        boolean result = this.save(team);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //4.插入数据队伍用户关系表
        UserTeam userTeam=new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return team.getId();
    }

    /**
     * 查询队伍信息
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, User loginUser) {
        if (teamQueryRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
       //根据队伍名称进行查询
        String name = teamQueryRequest.getName();
        if (StringUtils.isNotBlank(name)){
            queryWrapper.like("name",name);
        }
        //根据队伍描述进行查询
        String desc = teamQueryRequest.getDescription();
        if (StringUtils.isNotBlank(desc)){
            queryWrapper.like("description",desc);
        }
        //根据队伍人数进行查询
        Integer maxNum = teamQueryRequest.getMaxNum();
        if (maxNum!=null){
            queryWrapper.eq("maxNum",maxNum);
        }
        //根据创建人id进行查询
        Long userId = teamQueryRequest.getUserId();
        if (userId!=null){
            queryWrapper.eq("userId",userId);
        }
        //根据队伍名称和描述进行联合查询
        // name like %searchText% or description like %searchText%
        String searchText = teamQueryRequest.getSearchText();
        if (StringUtils.isNotBlank(searchText)){
            queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
        }
        //不展示过期的队伍expireTime is null or expireTime > now()
        queryWrapper.and(qw->qw.gt("expireTime",new Date()).or().isNull("expireTime"));

        //只有管理员才能查看加密还有非公开的房间
        if (ADMIN_ROLE.equals(loginUser.getUserRole())){
            Integer status = teamQueryRequest.getStatus();
            if (status!=null){
                queryWrapper.eq("status",status);
            }
        }

        List<Team> teamList = list(queryWrapper);
        //根据队伍查询创建队伍人信息
        return getTeamUserVoList(teamList);
    }



    /**
     * 修改队伍信息
     */
    @Override
    @Transactional
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //1.查询队伍是否存在
        Long teamId = teamUpdateRequest.getId();
        if (teamId==null || teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        Team newTeam=new Team();
        BeanUtils.copyProperties(teamUpdateRequest,newTeam);
        //2.校验参数
        checkAddTeamParams(newTeam);
        //3.只有管理员或者队伍的创建者可以修改
        if (!Objects.equals(team.getUserId(), loginUser.getId()) && !ADMIN_ROLE.equals(loginUser.getUserRole())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //如果用户传入的新值和老值一致，就不用 update 了（降低数据库使用次数）
        return this.updateById(newTeam);
    }

    /**
     * 加入队伍
     */
    @Override
    @Transactional
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId= loginUser.getId();
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId==null || teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //1.队伍必须存在，未过期的队伍
        Team team = isExistTeam(teamId);
        //2.不能加入自己的队伍
        if (team.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能加入自己的队伍");
        }
        //3.用户最多加入 5 个队伍
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long teamCount = userTeamService.count(queryWrapper);
        if (teamCount>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多有5个队伍");
        }

        QueryWrapper<UserTeam> userTeamQueryWrapper=new QueryWrapper<>();
        //只能加入未满的队伍
        userTeamQueryWrapper.eq("teamId",teamId);
        long userNum = userTeamService.count(userTeamQueryWrapper);
        if (userNum>=team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已满");
        }
        //4.不能重复加入已加入的队伍（幂等性)
        userTeamQueryWrapper.eq("userId",userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        //todo 优化 如果用户在同一时间大量点击加入同一队伍
        if (count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能重复加入队伍");
        }
        //4.禁止加入私有的队伍
        if (TeamStatusEnum.PRIVATE.getValue()==team.getStatus()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有的队伍");
        }
        //5.如果加入的队伍是加密的，必须密码匹配才可以
        if (TeamStatusEnum.SECRET.getValue()==team.getStatus()){
            String password = Optional.ofNullable(teamJoinRequest.getPassword()).orElse("");
            password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            if (!password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不正确");
            }
        }
        //新增队伍 - 用户关联信息
        UserTeam userTeam=new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }


    /**
     * 退出队伍
     */
    @Override
    @Transactional
    public boolean quitTeam(DeleteRequest deleteRequest, User loginUser) {
        //1.校验请求参数
        Long teamId = checkDeleteTeamId(deleteRequest, loginUser);
        //2.校验队伍是否存在
        Team team = isExistTeam(teamId);
        //3.校验当前用户是否已经已经加入了队伍
        QueryWrapper<UserTeam> teamQueryWrapper=new QueryWrapper<>();
        Long userId = loginUser.getId();
        teamQueryWrapper.eq("userId", userId);
        teamQueryWrapper.eq("teamId",teamId);
        UserTeam userTeam = userTeamService.getOne(teamQueryWrapper);
        if (userTeam==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"还未加入队伍");
        }
        //4.队伍当前有多少人
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        //上面判断了 userTeamList不可能为空
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //如果队伍只剩一个人 解散队伍
        if (userTeamList.size()==1){
            this.removeById(teamId);
        }else { //队伍两个人及以上
            //队长退出队伍
            if (Objects.equals(team.getUserId(), userId)){
                //id是自增的 所以查询出来越靠前的用户 加入队伍的时间越长
                //如果id不是自增可以通过创建时间升序排序 只查询出来前两条数据 取第二条数据就是下一个用户
                //队长权限转移给第二早加入的用户
                UpdateWrapper<Team> updateWrapper=new UpdateWrapper<>();
                updateWrapper.eq("id",teamId);
                updateWrapper.set("userId",userTeamList.get(1).getUserId());
                //更新当前队伍信息
                boolean res = this.update(updateWrapper);
                if (!res){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }
            }
            //非队长 直接删除对应关系
        }
        //删除对应关系
        return userTeamService.remove(teamQueryWrapper);
    }

    /**
     * 解散队伍
     */
    @Override
    @Transactional
    public boolean deleteTeam(DeleteRequest deleteRequest, User loginUser) {
        //1.校验参数
        Long teamId = checkDeleteTeamId(deleteRequest, loginUser);
        //2.校验队伍是否存在
        Team team=isExistTeam(teamId);
        //3.是否是自己创建的队伍
        if (!Objects.equals(team.getUserId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }
        //4.移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        boolean res = userTeamService.remove(queryWrapper);
        if (!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //5.删除队伍
        return this.removeById(teamId);
    }

    /**
     *获取当前用户创建的队伍信息
     */
    @Override
    public List<TeamUserVO> listMyCreateTeams(User loginUser) {
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        LambdaQueryWrapper<Team> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId,loginUser.getId());
        List<Team> teamList = this.list(queryWrapper);
        //根据队伍信息查询创建队伍人信息
        return getTeamUserVoList(teamList);
    }

    /**
     *获取当前用户加入的队伍信息
     */
    @Override
    public List<TeamUserVO> listMyJoinTeams(User loginUser) {
        if (loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        LambdaQueryWrapper<UserTeam> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,loginUser.getId());
        //队伍信息关系
        List<UserTeam> teamUserList = userTeamService.list(queryWrapper);
        //1 2
        //1 3
        //2 3
        //1=>2,3
        //2=>3
        //所有队伍（包含自己创建的队伍）
        Map<Long, List<UserTeam>> listMap = teamUserList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        //所有队伍 teamId
        Set<Long> teamIds = listMap.keySet();
        LambdaQueryWrapper<Team> teamQueryWrapper=new LambdaQueryWrapper<>();
        teamQueryWrapper.in(Team::getId,teamIds);
        //所有队伍信息
        List<Team> teamList = this.list(teamQueryWrapper);
        //筛选出不是自己创建的队伍
        teamList=teamList.stream().filter(team -> {
            Long teamId = team.getId();
            List<UserTeam> userTeams = listMap.get(teamId);
            for (UserTeam userTeam:userTeams){
                if (!Objects.equals(userTeam.getUserId(), team.getUserId())){
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        return getTeamUserVoList(teamList);
    }

    /**
     * 根据队伍信息查询创建队伍人信息
     * @param teamList 队伍信息列表
     * @return List<TeamUserVO>
     */
    private List<TeamUserVO> getTeamUserVoList(List<Team> teamList) {
        List<TeamUserVO> teamUserVOList=new ArrayList<>();
        //根据队伍查询创建队伍人信息
        if (teamList !=null) {
            teamList.forEach(team -> {
                TeamUserVO vo = new TeamUserVO();
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(team, vo);
                User user = userService.getById(team.getUserId());
                BeanUtils.copyProperties(user,userVO);
                vo.setCreateUser(userVO);
                teamUserVOList.add(vo);
            });
        }
        return teamUserVOList;
    }

    /**
     * 校验DeleteRequest参数
     * @return teamId
     */
    private  Long checkDeleteTeamId(DeleteRequest deleteRequest, User loginUser) {
        if (deleteRequest ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser ==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");
        }
        Long teamId = deleteRequest.getId();
        if (teamId==null || teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return teamId;
    }

    /**
     * 校验添加队伍参数
     */
    private void checkAddTeamParams(Team team) {
        //a.队伍人数>1 且 <=20
        int maxNum =Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum<1 || maxNum>20 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不符合要求");
        }
        //b.队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称不合法");
        }
        //c.描述<=512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述不合法");
        }
        //d.status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (enumByValue==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //e.队伍为加密状态 必须要有密码
        if (enumByValue.equals(TeamStatusEnum.SECRET)){
            //检验密码
            String password=team.getPassword();
            if (StringUtils.isBlank(password)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
            if (password.length()<6 || password.length()>16){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不合法");
            }
            //加密密码
            password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            team.setPassword(password);
        }
        //f.超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime!=null&&new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"过期时间要大于当前时间");
        }
    }

    /**
     * 判断队伍是否存在或队伍已过期
     */
    private Team isExistTeam(Long teamId) {
        LambdaQueryWrapper<Team> teamQueryWrapper = new LambdaQueryWrapper<>();
        teamQueryWrapper.eq(Team::getId, teamId);
        teamQueryWrapper.and(qw -> qw.isNull(Team::getExpireTime).or().gt(Team::getExpireTime, new Date()));
        Team team = this.getOne(teamQueryWrapper);
        if (team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在或已过期");
        }
        return team;
    }
}




