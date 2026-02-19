package com.edouardcourty.dd.velocity.handler;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

public class ChatBroadcastHandler {
    private final ProxyServer server;

    public ChatBroadcastHandler(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        // Depuis Minecraft 1.19.1, les messages sont signés et ne peuvent pas être annulés par le proxy.
        // On laisse l'événement passer (le backend diffuse le message aux joueurs du même serveur),
        // et on le relaie manuellement aux joueurs connectés sur les AUTRES serveurs.
        RegisteredServer currentServer = event.getPlayer().getCurrentServer()
                .map(ServerConnection::getServer)
                .orElse(null);

        Component message = Component.translatable(
                "chat.type.text",
                Component.text(event.getPlayer().getUsername()),
                Component.text(event.getMessage())
        );

        server.getAllPlayers().stream()
                .filter(p -> !p.equals(event.getPlayer()))
                .filter(p -> p.getCurrentServer()
                        .map(conn -> !conn.getServer().equals(currentServer))
                        .orElse(false))
                .forEach(p -> p.sendMessage(message));
    }
}
