package com.example.dococrserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "ssh")
public class SshTunnelConfig {
    private boolean enabled;
    private Bastion bastion;
    private int localPort = 5432;
    private String remoteHost;
    private int remotePort = 5432;

    @Data
    public static class Bastion {
        private String host;
        private Integer port;
        private String user;
        private String keyPath;
    }
}