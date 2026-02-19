package com.edouardcourty.dd.velocity.handler;

import com.edouardcourty.dd.common.messaging.Channels;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Transfère un message ENTITY_TRANSFER vers le serveur de la dimension cible.
 *
 * Comme targetServer.sendPluginMessage() exige un joueur sur le serveur cible,
 * on queue les messages et on les livre dès qu'un joueur se connecte au serveur
 * (via {@link #deliverPending}).
 */
public class EntityTransferHandler {
    private final ProxyServer server;
    private final MinecraftChannelIdentifier channel = MinecraftChannelIdentifier.from(Channels.ENTITY_TRANSFER.toString());
    private final Map<String, Queue<byte[]>> pendingTransfers = new ConcurrentHashMap<>();

    public EntityTransferHandler(ProxyServer server) {
        this.server = server;
    }

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EntityTransferHandler.class);

    public void handle(PluginMessageEvent event) {
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String targetServerName = in.readUTF().toLowerCase();

        RegisteredServer targetServer = server.getServer(targetServerName).orElse(null);
        if (targetServer == null) {
            logger.warn("[EntityTransfer] Unknown target server: '{}'", targetServerName);
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        long playersOnTarget = targetServer.getPlayersConnected().size();
        boolean sent = targetServer.sendPluginMessage(channel, event.getData());
        logger.info("[EntityTransfer] target={} players={} sent={}", targetServerName, playersOnTarget, sent);

        if (!sent) {
            pendingTransfers
                .computeIfAbsent(targetServerName, k -> new ConcurrentLinkedQueue<>())
                .add(event.getData());
        }
    }

    /** Appelé quand un joueur arrive sur un serveur — livre les entités en attente. */
    public void deliverPending(RegisteredServer targetServer) {
        Queue<byte[]> pending = pendingTransfers.get(targetServer.getServerInfo().getName());
        if (pending == null) return;
        byte[] data;
        while ((data = pending.poll()) != null) {
            targetServer.sendPluginMessage(channel, data);
        }
    }
}
