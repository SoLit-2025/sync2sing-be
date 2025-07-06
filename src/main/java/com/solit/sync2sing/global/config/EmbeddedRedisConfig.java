package com.solit.sync2sing.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@Profile({"dev","local"})  // local 프로필에서만 활성화
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        System.out.println("Starting Embedded Redis on port " + redisPort);

        // Windows 환경 특별 처리
        if (isRunningOnWindows()) {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("maxmemory 128M")
                    .build();
        } else {
            redisServer = new RedisServer(redisPort);
        }

        try {
            redisServer.start();
            System.out.println("Embedded Redis started successfully");
        } catch (Exception e) {
            System.err.println("Failed to start Embedded Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            System.out.println("Stopping Embedded Redis");
            redisServer.stop();
        }
    }

    private boolean isRunningOnWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
