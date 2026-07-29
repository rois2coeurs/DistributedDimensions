package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.messaging.Channels;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

/**
 * Paper side:
 * - Removes default join/quit messages (generated on every Paper connect/disconnect,
 *   including during dimension switches).
 * - Receives dd:player_broadcast from Velocity and broadcasts them to online players.
 *   Only Velocity emits the real join/quit messages (proxy PostLoginEvent / DisconnectEvent).
 */
public class PlayerBroadcastListener implements Listener, PluginMessageListener {
    private final JavaPlugin plugin;

    public PlayerBroadcastListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null); // delete default Paper message
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null); // delete default Paper message
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player ignored, byte @NotNull [] bytes) {
        if (!channel.equals(Channels.PLAYER_BROADCAST.toString())) return;
        String raw = ByteStreams.newDataInput(bytes).readUTF();
        Component message = LegacyComponentSerializer.legacySection().deserialize(raw);
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }
}
