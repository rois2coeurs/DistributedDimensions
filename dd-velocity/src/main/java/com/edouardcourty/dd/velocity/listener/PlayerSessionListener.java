package com.edouardcourty.dd.velocity.listener;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.velocity.handler.EntityTransferHandler;
import com.edouardcourty.dd.velocity.store.LastServerStore;
import com.edouardcourty.dd.velocity.store.PlayerStateStore;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class PlayerSessionListener {
    private final ProxyServer server;
    private final LastServerStore lastServerStore;
    private final PlayerStateStore playerStateStore;
    private final EntityTransferHandler entityTransferHandler;

    public PlayerSessionListener(ProxyServer server, LastServerStore lastServerStore, PlayerStateStore playerStateStore, EntityTransferHandler entityTransferHandler) {
        this.server = server;
        this.lastServerStore = lastServerStore;
        this.playerStateStore = playerStateStore;
        this.entityTransferHandler = entityTransferHandler;
    }

    @Subscribe
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        lastServerStore.get(event.getPlayer().getUniqueId()).ifPresent(serverName -> {
            Optional<RegisteredServer> serverOpt = server.getServer(serverName);
            if (serverOpt.isEmpty()) {
                event.getPlayer().disconnect(Component.text(
                    "Le serveur " + serverName + " est inaccessible. Reconnectez-vous dans quelques instants.",
                    NamedTextColor.RED
                ));
                return;
            }
            event.setInitialServer(serverOpt.get());
        });
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(conn -> {
            entityTransferHandler.deliverPending(conn.getServer());

            // Restaurer l'état complet du joueur lors de la première connexion (reconnexion)
            if (event.getPreviousServer() == null) {
                playerStateStore.get(event.getPlayer().getUniqueId()).ifPresent(stateBytes ->
                    conn.getServer().sendPluginMessage(
                        com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier.from(Channels.RESTORE_STATE.toString()),
                        stateBytes
                    )
                );
            }
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(conn ->
            lastServerStore.save(event.getPlayer().getUniqueId(), conn.getServerInfo().getName())
        );
    }
}
