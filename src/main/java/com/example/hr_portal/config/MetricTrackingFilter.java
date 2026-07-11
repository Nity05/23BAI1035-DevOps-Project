package com.example.hr_portal.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class MetricTrackingFilter implements Filter {
    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong totalDuration = new AtomicLong(0);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (request instanceof HttpServletRequest httpRequest) {
                String path = httpRequest.getRequestURI();
                // Skip static assets to measure actual controller latency
                if (!path.startsWith("/css/") && !path.startsWith("/js/") && !path.startsWith("/images/") && !path.endsWith(".ico")) {
                    totalRequests.incrementAndGet();
                    totalDuration.addAndGet(duration);
                }
            }
        }
    }

    public static long getTotalRequests() {
        return totalRequests.get();
    }

    public static long getAverageResponseTime() {
        long reqs = totalRequests.get();
        return reqs == 0 ? 45 : totalDuration.get() / reqs; // Default to 45 ms if no hits
    }
}
