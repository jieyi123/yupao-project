package com.pjieyi.yupao.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pjieyi.yupao.model.entity.User;
import com.pjieyi.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    @Resource
    private RedissonClient redissonClient;

    //重点用户  要实时更改
    private final List<Long> mainUserList= Arrays.asList(1L, 2L);

    //每天00:00执行，预热推荐用户
    @Scheduled(cron = "0 0 0 * * ? ")
    public void doCacheRecommendUser(){
        log.info("定时任务开启");
        RLock lock = redissonClient.getLock("yupao:precache:job:docache:lock");
        //锁的存在时间要设置为-1（开启开门狗），默认锁的过期时间是30秒，每 10 秒续期一次（补到 30 秒）
        try {
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                log.info(Thread.currentThread().getId()+"拿到锁");
                for (Long userId:mainUserList){
                    //为热点用户预热20条推荐数据
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
        } catch (InterruptedException e) {
            log.error("lock error:"+e.getMessage());
        }finally {
            //用完锁一定要释放
            //释放自己的锁
            if (lock.isHeldByCurrentThread()){ //是否是当前线程
                lock.unlock();
            }
            log.error(Thread.currentThread().getId()+"释放锁");
        }
        log.info("定时任务结束");
    }
}
