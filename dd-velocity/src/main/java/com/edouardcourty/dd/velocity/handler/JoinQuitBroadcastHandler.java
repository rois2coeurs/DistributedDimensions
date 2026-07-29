package com.edouardcourty.dd.velocity.handler;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.velocity.util.ServerBroadcast;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Broadcasts real join/quit messages to all Paper servers via dd:player_broadcast.
 * - Join : ServerPostConnectEvent without previous server (first proxy connection)
 * - Quit : DisconnectEvent (full disconnect from proxy)
 * These events DO NOT trigger during dimension switches.
 */
public class JoinQuitBroadcastHandler {
    private final ProxyServer server;

    public JoinQuitBroadcastHandler(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onFirstServerConnect(ServerPostConnectEvent event) {
        if (event.getPreviousServer() != null) return;
        broadcast(Component.text(event.getPlayer().getUsername(), NamedTextColor.YELLOW)
            .append(Component.text(" joined the server.", NamedTextColor.WHITE)));
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        broadcast(Component.text(event.getPlayer().getUsername(), NamedTextColor.YELLOW)
            .append(Component.text(" left the server.", NamedTextColor.WHITE)));
    }

    private void broadcast(Component message) {
        var out = ByteStreams.newDataOutput();
        out.writeUTF(PlainTextComponentSerializer.plainText().serialize(message));
        ServerBroadcast.toAll(server, Channels.PLAYER_BROADCAST.toString(), out.toByteArray());
    }
}
