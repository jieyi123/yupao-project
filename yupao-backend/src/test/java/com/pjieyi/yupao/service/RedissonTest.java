package com.pjieyi.yupao.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author pjieyi
 * @desc
 */
@SpringBootTest
@Slf4j
public class RedissonTest {


    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        RList<String> user = redissonClient.getList("test-list");

        RMap<String, String> map = redissonClient.getMap("test-map3");
        map.put("zhansgan","123");
        map.put("李四","你好");

        //user.add("你好");
        //user.add("hello");
        //String s = user.get(0);
        //System.out.println(s);
        //String s1 = user.get(2);
        //System.out.println(s1);
        user.remove("你好");
    }

    @Test
    public void testLock(){
        RLock lock = redissonClient.getLock("yupao:precache:job:docache:lock");
        //锁的存在时间要设置为-1（开启开门狗），默认锁的过期时间是30秒，每 10 秒续期一次（补到 30 秒）
        try {
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                log.info(Thread.currentThread().getId()+"拿到锁");
                Thread.sleep(3000000);
            }
        } catch (InterruptedException e) {
            log.error("lock error:"+e.getMessage());
        }finally {
            //释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
            log.error(Thread.currentThread().getId()+"释放锁");
        }
    }
}
