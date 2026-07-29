package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.common.model.PortalConstants;
import com.edouardcourty.dd.paper.store.PrePortalPositionStore;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Receives dd:switch_failed from Velocity when the target server is unreachable.
 * Returns the player to their pre-portal position via {@link PrePortalPositionStore}.
 */
public class SwitchFailedListener implements PluginMessageListener {
    private final JavaPlugin plugin;
    private final PrePortalPositionStore positionStore;

    public SwitchFailedListener(JavaPlugin plugin, PrePortalPositionStore positionStore) {
        this.plugin = plugin;
        this.positionStore = positionStore;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player ignored, byte @NotNull [] bytes) {
        if (!channel.equals(Channels.SWITCH_FAILED.toString())) return;

        UUID uuid = UUID.fromString(ByteStreams.newDataInput(bytes).readUTF());
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return;

        positionStore.remove(uuid).ifPresent(player::teleport);
        player.setPortalCooldown(PortalConstants.PORTAL_COOLDOWN_TICKS);
        player.sendMessage(Component.text(
            "Destination server is unreachable. Please try again later.",
            NamedTextColor.RED
        ));
        plugin.getLogger().warning("[SwitchFailed] Player " + player.getName() + " could not switch dimension — server offline.");
    }
}

