package com.pjieyi.yupao.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author pjieyi
 * @desc 推荐用户 缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //重点用户  要实时更改
    private final List<Long> mainUserList= Arrays.asList(1L, 2L);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 12 10 * * ? ")
    public void doCacheRecommendUser(){
        for (Long userId:mainUserList){
            //为每个用户预热20条推荐数据
            Page<User> userPage = userService.page(new Page<>(1, 10));
            String redisKey=String.format("yupao:user:recommend:%s",userId);
            try {
                //写缓存
                redisTemplate.opsForValue().set(redisKey,userPage,300, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("redis set key error",e);
            }
        }
    }
}
