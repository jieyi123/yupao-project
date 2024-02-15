package com.pjieyi.yupao.task;

import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pjieyi
 * @description 定时任务插入数据
 */
@Component
public class InsertUserTask {

    @Resource
    private UserService userService;

    //启动项目5s后执行定时任务 fixedRate
    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public void doInsertUser(){
        System.out.println("定时任务开始执行");
        int totalNum=10000;
        //批量分段插入10000条数据
        int insertNum=1000;
        List<User> userList=new ArrayList<>();
        for (int i=0;i<totalNum;i++){
            User user=new User();
            user.setId(0L);
            user.setUserName("批量插入"+i);
            user.setUserAccount("批量插入"+i);
            user.setUserAvatar("https://pjieyi.oss-cn-chengdu.aliyuncs.com/public/dc7635eb-c154-4417-b0cd-9b741c37777d.png");
            user.setUserPassword("56191e5f69f082ef3e10280f2ad31672");
            userList.add(user);
        }
        userService.saveBatch(userList,insertNum);
        System.out.println("定时任务执行完成");
    }
}
