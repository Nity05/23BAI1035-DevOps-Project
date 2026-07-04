package com.example.hr_portal.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.graphite.GraphiteProtocol;

@Configuration
public class GraphiteConfiguration {

    @Bean
    public GraphiteMeterRegistry graphiteMeterRegistry() {

        GraphiteConfig config = new GraphiteConfig() {

            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String host() {
                return "graphite";
            }

            @Override
            public int port() {
                return 2003;
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
                return true;
            }

            @Override
            public String prefix() {
                return "hr_portal";
            }
        };

        return new GraphiteMeterRegistry(config, Clock.SYSTEM);
    }
}