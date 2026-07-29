package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.paper.messaging.EntityTransferSerializer;
import com.edouardcourty.dd.paper.portal.SafeLocationFinder;
import com.google.common.io.ByteStreams;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

/**
 * Receives an ENTITY_TRANSFER message sent by Velocity and spawns the entity
 * (along with its passengers recursively) in the correct world.
 */
public class EntitySpawnListener implements PluginMessageListener {
    private final JavaPlugin plugin;

    public EntitySpawnListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull org.bukkit.entity.Player player, byte @NotNull [] bytes) {
        if (!channel.equals(Channels.ENTITY_TRANSFER.toString())) return;

        EntityTransferSerializer.EntityData data = EntityTransferSerializer.readRoot(ByteStreams.newDataInput(bytes));
        plugin.getLogger().info("[EntitySpawn] Received transfer for " + data.entityType() + " → world " + data.targetDimension().toBukkitWorldName());

        World world = plugin.getServer().getWorld(data.targetDimension().toBukkitWorldName());
        if (world == null) {
            plugin.getLogger().severe("[EntitySpawn] World not found: " + data.targetDimension() + " (available: " + plugin.getServer().getWorlds().stream().map(World::getName).toList() + ")");
            return;
        }

        // Find the nearest portal or a safe location
        Location spawnLoc;
        if (data.targetDimension() == com.edouardcourty.dd.common.model.Dimension.END) {
            // Rebuild vanilla platform just like for players
            spawnLoc = com.edouardcourty.dd.paper.portal.PortalBuilder.buildEndPlatform(
                world, (int) data.destX(), (int) data.destY(), (int) data.destZ());
        } else {
            spawnLoc = SafeLocationFinder.findEntityArrival(world, data.destX(), data.destY(), data.destZ());
        }
        Entity spawned = EntityTransferSerializer.spawn(world, spawnLoc, data);
        // Mark the entity (and its passengers) as recently transferred to avoid immediate re-trigger
        markTransferred(spawned);
    }

    private void markTransferred(Entity entity) {
        EntityPortalListener.markTransferred(entity.getUniqueId());
        entity.getPassengers().forEach(this::markTransferred);
    }
}
