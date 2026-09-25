package com.fbguard.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * In-memory caching (Caffeine) for read-heavy, slow-changing endpoints like
 * the app gallery and risk-distribution chart. Each cache entry expires after
 * a short window so data never goes stale for long, but repeated page loads
 * within that window skip the database entirely.
 *
 * To move to Redis for production (needed once you run more than one backend
 * instance, since Caffeine's cache lives in one process's memory only):
 * swap this CacheManager bean for a RedisCacheManager - the @Cacheable
 * annotations in the services don't change at all.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String APP_GALLERY_CACHE = "appGallery";
    public static final String RISK_DISTRIBUTION_CACHE = "riskDistribution";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(APP_GALLERY_CACHE, RISK_DISTRIBUTION_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(500));
        return manager;
    }
}
