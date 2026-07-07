package com.niit.feed.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.concurrent.ConcurrentHashMap;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(RoutingDataSource.class);

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private static final long MASTER_WINDOW_MS = 5000;
    private static final ConcurrentHashMap<Long, Long> LAST_POST_TIME = new ConcurrentHashMap<>();

    public static void setMaster() {
        CONTEXT.set("master");
        log.debug("数据源切换 → master");
    }

    public static void setSlave() {
        CONTEXT.set("slave");
        log.debug("数据源切换 → slave");
    }

    public static String getCurrent() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static void recordPostTime(Long userId) {
        LAST_POST_TIME.put(userId, System.currentTimeMillis());
        log.debug("记录发帖时间: userId={}", userId);
    }

    public static boolean shouldReadMaster(Long userId) {
        Long lastPost = LAST_POST_TIME.get(userId);
        if (lastPost == null) return false;
        long elapsed = System.currentTimeMillis() - lastPost;
        return elapsed < MASTER_WINDOW_MS;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String key = CONTEXT.get();
        if (key == null) {
            key = "master";
        }
        return key;
    }
}
