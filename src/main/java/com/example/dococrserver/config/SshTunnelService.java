package com.example.dococrserver.config;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@Slf4j
@Component("sshTunnel")
@RequiredArgsConstructor
public class SshTunnelService {

    private final SshTunnelConfig config;
    private SSHClient sshClient;
    private ServerSocket serverSocket;
    private Thread tunnelThread;

    @PostConstruct
    public void startTunnel() throws IOException {
        if (!config.isEnabled()) {
            log.info("SSH tunnel is disabled, skipping.");
            return;
        }

        log.info("Opening SSH tunnel {}:{} -> {}:{}",
                "localhost", config.getLocalPort(),
                config.getRemoteHost(), config.getRemotePort());

        sshClient = new SSHClient();

        // In production, replace it with a known_hosts verifier
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.connect(config.getBastion().getHost(), config.getBastion().getPort());

        // Authenticate with private key (PEM file)
        String keyPath = config.getBastion().getKeyPath();
        sshClient.authPublickey(config.getBastion().getUser(), keyPath);

        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress("localhost", config.getLocalPort()));

        Parameters params = new Parameters(
                "localhost", config.getLocalPort(),
                config.getRemoteHost(), config.getRemotePort()
        );

        LocalPortForwarder forwarder = sshClient.newLocalPortForwarder(params, serverSocket);

        // Run the forwarder in a background thread — it blocks while the tunnel is open
        tunnelThread = new Thread(() -> {
            try {
                forwarder.listen();
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    log.error("SSH tunnel error", e);
                }
            }
        }, "ssh-tunnel-thread");

        tunnelThread.setDaemon(true);
        tunnelThread.start();

        log.info("SSH tunnel established successfully.");
    }

    @PreDestroy
    public void stopTunnel() {
        log.info("Closing SSH tunnel...");
        if (tunnelThread != null) {
            tunnelThread.interrupt();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        if (sshClient != null && sshClient.isConnected()) {
            try { sshClient.disconnect(); } catch (IOException ignored) {}
        }
        log.info("SSH tunnel closed.");
    }
}
