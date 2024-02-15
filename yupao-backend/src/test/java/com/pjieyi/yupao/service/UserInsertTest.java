package com.pjieyi.yupao.service;
import java.util.ArrayList;
import java.util.Date;

import com.pjieyi.yupao.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 一次性插入数据
 *
 * @author pjieyi
 */
@SpringBootTest
class UserInsertTest {

    @Resource
    private UserService userService;


    /**
     * 一条一条的数据插入
     */
    @Test
    public void doInsertUser(){
        //spring工具 计时
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        //原生for一次性插入10000条数据  20s
        int totalNum=10000;
        for (int i=0;i<totalNum;i++){
            User user=new User();
            user.setId(0L);
            user.setUserName("假数据"+i);
            user.setUserAccount("假数据"+i);
            user.setUserAvatar("https://pjieyi.oss-cn-chengdu.aliyuncs.com/public/dc7635eb-c154-4417-b0cd-9b741c37777d.png");
            user.setUserPassword("56191e5f69f082ef3e10280f2ad31672");
            userService.save(user);
        }
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(totalTimeMillis);
    }

    /**
     * 批量插入数据
     */
    @Test
    public void doInsertUserSaveBanth(){
        //spring工具 计时
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        //批量插入10000条数据  4s
        int totalNum=10000;
        //批量分段插入10000条数据 每100条插入一次 5s  每1000条插入一次 4s
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
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(totalTimeMillis);
    }

    /**
     * 并发批量插入数据
     */
    @Test
    public void doInsertUserFuture(){
        //并发插入10000条数据  2s
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        //分10组
        int batchSize=1000;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();
        int j=0;
        for (int i=0;i<10;i++){
            List<User> userList=new ArrayList<>();
            while (true){
                j++;
                User user=new User();
                user.setId(0L);
                user.setUserName("批量插入"+i);
                user.setUserAccount("批量插入"+i);
                user.setUserAvatar("https://pjieyi.oss-cn-chengdu.aliyuncs.com/public/dc7635eb-c154-4417-b0cd-9b741c37777d.png");
                user.setUserPassword("56191e5f69f082ef3e10280f2ad31672");
                userList.add(user);
                if (j%batchSize==0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName:" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(totalTimeMillis);
    }

}