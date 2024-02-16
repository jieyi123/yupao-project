package com.pjieyi.yupao.service;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.pjieyi.yupao.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author pjieyi
 * @description
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisString(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("name","pjieyi");
        Object o = valueOperations.get("name");
        System.out.println(o);
        User user=new User();
        user.setUserName("31231");
        user.setUserAccount("213231");
        user.setPhone("1231123");
        user.setUserPassword("1231");
        valueOperations.set("user",user,1, TimeUnit.MINUTES);
        Object o1 = valueOperations.get("user");
        System.out.println(o1);

    }
}
