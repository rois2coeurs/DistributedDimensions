package com.edouardcourty.dd.velocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

/**
 * Utility to broadcast a plugin message to all servers registered in the proxy.
 */
public class ServerBroadcast {

    private ServerBroadcast() {}

    public static void toAll(ProxyServer server, String channel, byte[] payload) {
        MinecraftChannelIdentifier id = MinecraftChannelIdentifier.from(channel);
        server.getAllServers().forEach(s -> s.sendPluginMessage(id, payload));
    }
}
