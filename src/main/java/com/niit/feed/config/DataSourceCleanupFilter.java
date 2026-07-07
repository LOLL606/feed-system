package com.niit.feed.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 每个请求结束后清除 ThreadLocal 中的数据源标记
 * 防止 ThreadLocal 泄露导致下一个请求拿到错误的连接
 */
@Component
public class DataSourceCleanupFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(DataSourceCleanupFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            RoutingDataSource.clear();
            log.debug("ThreadLocal 已清除");
        }
    }
}
