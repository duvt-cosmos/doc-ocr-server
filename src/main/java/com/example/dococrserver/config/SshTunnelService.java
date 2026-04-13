package com.example.dococrserver.config;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.LocalPortForwarder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshTunnelService {

    private final SshTunnelConfig config;
    private SSHClient sshClient;
    private LocalPortForwarder portForwarder;

    @PostConstruct
    public void startTunnel() {
        try {
            log.info("Attempting to connect to bastion host: {}...", config.getBastion().getHost());
            sshClient = new SSHClient();
            sshClient.addHostKeyVerifier(new net.schmizz.sshj.common.PromiscuousVerifier());
            sshClient.connect(config.getBastion().getHost());

            log.info("Authenticating with key: {}...", config.getBastion().getKeyPath());
            sshClient.authPublickey(config.getBastion().getUser(), config.getBastion().getKeyPath());

            log.info("Establishing local port forward {} -> {}:{}...",
                config.getLocalPort(), config.getRemoteHost(), config.getRemotePort());
            portForwarder = sshClient.getLocalPortForwarder();
            portForwarder.addLocalPortForwarding(config.getLocalPort(), config.getRemoteHost(), config.getRemotePort());

            log.info("SSH Tunnel successfully established.");
        } catch (IOException e) {
            log.error("Failed to establish SSH tunnel: {}", e.getMessage());
            throw new RuntimeException("Database connectivity failed due to SSH tunnel error", e);
        }
    }

    @PreDestroy
    public void stopTunnel() {
        log.info("Closing SSH tunnel...");
        try {
            if (portForwarder != null) portForwarder.close();
            if (sshClient != null) sshClient.disconnect();
            log.info("SSH tunnel closed.");
        } catch (IOException e) {
            log.error("Error while closing SSH tunnel: {}", e.getMessage());
        }
    }
}
