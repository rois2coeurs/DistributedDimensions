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
 * Côté Paper :
 * - Supprime les messages join/quit par défaut (générés à chaque connect/disconnect Paper,
 *   y compris lors des switches de dimension).
 * - Reçoit dd:player_broadcast de Velocity et les affiche aux joueurs en ligne.
 *   Seul Velocity émet les vrais messages join/quit (PostLoginEvent / DisconnectEvent proxy).
 */
public class PlayerBroadcastListener implements Listener, PluginMessageListener {
    private final JavaPlugin plugin;

    public PlayerBroadcastListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null); // supprime le message Paper par défaut
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null); // supprime le message Paper par défaut
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player ignored, byte @NotNull [] bytes) {
        if (!channel.equals(Channels.PLAYER_BROADCAST.toString())) return;
        String raw = ByteStreams.newDataInput(bytes).readUTF();
        Component message = LegacyComponentSerializer.legacySection().deserialize(raw);
        plugin.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }
}
