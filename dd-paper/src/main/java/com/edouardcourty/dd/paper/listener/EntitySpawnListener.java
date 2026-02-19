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
 * Reçoit un message ENTITY_TRANSFER envoyé par Velocity et spawne l'entité
 * (ainsi que ses passagers récursivement) dans le bon monde.
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

        // Chercher le portail le plus proche ou une position sûre
        Location spawnLoc;
        if (data.targetDimension() == com.edouardcourty.dd.common.model.Dimension.END) {
            // Reconstruire la plateforme vanilla comme pour les joueurs
            spawnLoc = com.edouardcourty.dd.paper.portal.PortalBuilder.buildEndPlatform(
                world, (int) data.destX(), (int) data.destY(), (int) data.destZ());
        } else {
            spawnLoc = SafeLocationFinder.findEntityArrival(world, data.destX(), data.destY(), data.destZ());
        }
        Entity spawned = EntityTransferSerializer.spawn(world, spawnLoc, data);
        // Marquer l'entité (et ses passagers) comme récemment transférée pour éviter re-trigger immédiat
        markTransferred(spawned);
    }

    private void markTransferred(Entity entity) {
        EntityPortalListener.markTransferred(entity.getUniqueId());
        entity.getPassengers().forEach(this::markTransferred);
    }
}
