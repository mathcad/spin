package com.consultant;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache配置
 * <p>Created by xuweinan on 2017/5/11.</p>
 *
 * @author xuweinan
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    public enum Caches {
        Moment(5, 50000),
        Short(10, 20000),
        Medium(20, 8000),
        Long(30, 5000);

        Caches(int expiredIn, int maxSize) {
            this.expiredIn = expiredIn;
            this.maxSize = maxSize;
        }

        private int maxSize;
        private int expiredIn;

        public int getMaxSize() {
            return maxSize;
        }

        public int getExpiredIn() {
            return expiredIn;
        }
    }

    /**
     * 创建基于Caffeine的Cache Manager
     *
     * @return
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = new ArrayList<>();
        for (Caches c : Caches.values()) {
            caches.add(new CaffeineCache(c.name(),
                Caffeine.newBuilder().recordStats()
                    .expireAfterWrite(c.getExpiredIn(), TimeUnit.SECONDS)
                    .maximumSize(c.getMaxSize())
                    .build())
            );
        }

        cacheManager.setCaches(caches);

        return cacheManager;
    }

}
