package com.smooth.driving_analysis_service.global.config;

import java.time.Duration;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
@Profile("!test")
public class RedisConfig {

    @Value("${spring.data.redis.host}")     private String host;
    @Value("${spring.data.redis.port}")     private int port;


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port); // 기본: DB=0, 비번 없음
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplateObject(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        StringRedisSerializer s = new StringRedisSerializer();
        template.setKeySerializer(s);
        template.setHashKeySerializer(s);
        // 값은 Object니까 JSON 직렬화 추천
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }


    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        StringRedisTemplate t = new StringRedisTemplate();
        t.setConnectionFactory(cf);
        StringRedisSerializer s = new StringRedisSerializer();
        t.setKeySerializer(s);
        t.setValueSerializer(s);
        t.setHashKeySerializer(s);
        t.setHashValueSerializer(s);
        t.afterPropertiesSet();
        return t;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        StringRedisSerializer s = new StringRedisSerializer();
        t.setKeySerializer(s);
        t.setValueSerializer(s);
        t.setHashKeySerializer(s);
        t.setHashValueSerializer(s);
        t.afterPropertiesSet();
        return t;
    }

    @Bean
    public StreamOperations<String, String, String> streamOperations(StringRedisTemplate template) {
        return template.opsForStream();
    }

    @Bean
    @Profile("!test")
    public StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamContainerOptions() {
        return StreamMessageListenerContainerOptions.<String, MapRecord<String, String, String>>builder()
                .batchSize(10)
                .pollTimeout(Duration.ofSeconds(2))
                .executor(Executors.newFixedThreadPool(2))
                // .targetType(String.class)  // 이 메서드가 없다면 주석 처리해도 됩니다
                .build(); // 3.x는 build()
    }

    @Bean
    @Profile("!test")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer(
            RedisConnectionFactory cf,
            StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> opts) {
        return StreamMessageListenerContainer.create(cf, opts);
    }
}