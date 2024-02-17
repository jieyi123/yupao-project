package com.pjieyi.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pjieyi.yupao.model.entity.UserTeam;
import com.pjieyi.yupao.service.UserTeamService;
import com.pjieyi.yupao.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author pjy17
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-02-17 09:59:01
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




