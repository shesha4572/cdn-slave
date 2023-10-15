package com.shesha4572.cdnslave.configs;

import com.shesha4572.cdnslave.entities.FileChunk;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory("redis" , 6379);
    }

    @Bean
    public RedisTemplate<String , FileChunk> redisTemplate(){
        RedisTemplate<String , FileChunk> fileChunkRedisTemplate = new RedisTemplate<>();
        fileChunkRedisTemplate.setConnectionFactory(redisConnectionFactory());
        return fileChunkRedisTemplate;
    }

}
