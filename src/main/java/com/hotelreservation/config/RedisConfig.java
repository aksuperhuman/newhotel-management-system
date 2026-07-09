package com.hotelreservation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        return template;
    }

    @Bean
    public StringRedisTemplateHolder stringRedisTemplateHolder(RedisConnectionFactory factory) {
        return new StringRedisTemplateHolder(factory);
    }

    /** Thin wrapper so the distributed lock service can inject a String-typed template. */
    public static class StringRedisTemplateHolder {
        public final org.springframework.data.redis.core.StringRedisTemplate template;
        public StringRedisTemplateHolder(RedisConnectionFactory factory) {
            this.template = new org.springframework.data.redis.core.StringRedisTemplate(factory);
        }
    }
}
