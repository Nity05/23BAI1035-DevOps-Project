package com.example.hr_portal.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.graphite.GraphiteProtocol;

@Configuration
public class GraphiteConfiguration {

    @Value("${management.metrics.export.graphite.host:graphite}")
    private String graphiteHost;

    @Value("${management.metrics.export.graphite.port:2003}")
    private int graphitePort;

    @Value("${management.metrics.export.graphite.enabled:false}")
    private boolean graphiteEnabled;

    private String resolveHost() {
        try {
            java.net.InetAddress.getByName(graphiteHost);
            return graphiteHost;
        } catch (java.net.UnknownHostException e) {
            return "localhost";
        }
    }

    @Bean
    public GraphiteMeterRegistry graphiteMeterRegistry() {

        GraphiteConfig config = new GraphiteConfig() {

            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String host() {
                return resolveHost();
            }

            @Override
            public int port() {
                return graphitePort;
            }

            @Override
            public GraphiteProtocol protocol() {
                return GraphiteProtocol.PLAINTEXT;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public boolean enabled() {
                return graphiteEnabled;
            }

            @Override
            public String prefix() {
                return "hr_portal";
            }
        };

        return new GraphiteMeterRegistry(config, Clock.SYSTEM);
    }
}