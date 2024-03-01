package com.pjieyi.yupao.service;

import com.pjieyi.yupao.exception.BusinessException;
import com.pjieyi.yupao.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

/**
 * 用户服务测试
 *
 * @author pjieyi
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;


    @Test
    public void testSearchUserByTagsQuery(){
        List<String> tags= Arrays.asList("java","python");
        userService.searchUserByTagsQuery(tags);
    }
    @Test
    public void testSearchUserByTagsMemory(){
        List<String> tags= Arrays.asList("java");
        List<User> users = userService.searchUserByTagsMemory(tags);
        System.out.println(users);
    }

}