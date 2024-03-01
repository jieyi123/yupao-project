package com.pjieyi.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pjieyi
 * @desc Redisson客户端
 */
@Configuration
//引用yml文件中的redis配置
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String port;
    private String host;

    @Bean
    public RedissonClient redissonClient(){
        //1.创建配置
        Config config = new Config();
        // use "redis://127.0.0.1:7181 for SSL connection
        //String redisAddress="redis://47.108.67.9:6379";
        //String redisAddress="redis://127.0.0.1:6379";
        String redisAddress=String.format("redis://%s:%s",host,port);
        config.useSingleServer().setDatabase(3).setAddress(redisAddress);
        //2.创建Redisson实例
        return Redisson.create(config);
    }
}
