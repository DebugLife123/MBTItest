package com.debuglife.mbti.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * 统一缓存配置：管理端统计、人格分布、AI 会话缓存等共用同一个 CacheManager。
 *
 * <p>序列化统一用 JSON，缓存键前缀清晰，便于在 Redis 中直接定位与运维。</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** 管理端统计缓存 5 分钟：数据变化不敏感，缓存收益高 */
    public static final String CACHE_ADMIN_STATISTICS = "admin:statistics";
    /** 人格类型分布缓存 10 分钟：变动频率更低 */
    public static final String CACHE_ADMIN_DISTRIBUTION = "admin:distribution";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // 复用 Spring 容器中的 ObjectMapper，自带 JSR310 时间模块，避免 LocalDateTime 序列化失败
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper.copy());
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCache = Map.of(
                CACHE_ADMIN_STATISTICS, base.entryTtl(Duration.ofMinutes(5)),
                CACHE_ADMIN_DISTRIBUTION, base.entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
