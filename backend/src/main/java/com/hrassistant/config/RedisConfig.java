package com.hrassistant.config;

import com.hrassistant.model.CachedResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for semantic response caching.
 */
@Configuration
public class RedisConfig {

    /**
     * Configures RedisTemplate with JSON serialization for CachedResponse objects.
     * Uses StringRedisSerializer for keys and GenericJacksonJsonRedisSerializer for values.
     * Jackson 3 has built-in support for Java 8 date/time types (JSR310).
     *
     * @param connectionFactory Redis connection factory (auto-configured by Spring Boot)
     * @return Configured RedisTemplate for CachedResponse storage
     */
    @Bean
    public RedisTemplate<String, CachedResponse> cachedResponseRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, CachedResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure key serializer
        template.setKeySerializer(new StringRedisSerializer());

        // Configure value serializer with Jackson 3 (JSR310 support is built-in)
        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder()
                .build();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
