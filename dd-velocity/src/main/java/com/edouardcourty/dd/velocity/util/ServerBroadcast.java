package com.edouardcourty.dd.velocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

/**
 * Utilitaire pour broadcaster un plugin message sur tous les serveurs enregistrés dans le proxy.
 */
public class ServerBroadcast {

    private ServerBroadcast() {}

    public static void toAll(ProxyServer server, String channel, byte[] payload) {
        MinecraftChannelIdentifier id = MinecraftChannelIdentifier.from(channel);
        server.getAllServers().forEach(s -> s.sendPluginMessage(id, payload));
    }
}
