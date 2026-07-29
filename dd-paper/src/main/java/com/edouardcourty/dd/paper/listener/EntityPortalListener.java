package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.PortalConstants;
import com.edouardcourty.dd.paper.messaging.EntityTransferSerializer;
import com.edouardcourty.dd.paper.service.NetherCoordinateScaler;
import com.edouardcourty.dd.paper.util.DimensionUtil;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transfers non-player entities (mobs, items, minecarts...) to the target
 * dimension's server.
 *
 * - Nether Portal: via EntityPortalEvent (OVERWORLD ↔ NETHER, 1:8 scale)
 * - End Portal   : via EntityInsideBlockEvent on END_PORTAL (since vanilla does
 *   not trigger EntityPortalEvent for non-players in End portals)
 */
public class EntityPortalListener implements Listener {
    private final JavaPlugin plugin;

    /** UUIDs of recently transferred entities — ignored for 15s to prevent immediate re-trigger. */
    private static final Map<UUID, Long> RECENTLY_TRANSFERRED = new ConcurrentHashMap<>();
    private static final long TRANSFER_GRACE_MS = PortalConstants.PORTAL_COOLDOWN_TICKS * 50L; // ticks → ms

    public static void markTransferred(UUID uuid) {
        RECENTLY_TRANSFERRED.put(uuid, System.currentTimeMillis());
    }

    public EntityPortalListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Nether Portal (EntityPortalEvent) ────────────────────────────────────

    @EventHandler
    public void onEntityPortal(EntityPortalEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player) return;
        
        Dimension dimension = DimensionUtil.fromWorld(e.getFrom().getWorld());
        if (dimension == Dimension.END) return; // no nether portal in vanilla End

        if (isRecentlyTransferred(entity)) { e.setCancelled(true); return; }

        e.setCancelled(true);

        NetherCoordinateScaler.ScaledCoords dest = NetherCoordinateScaler.scale(
            dimension, e.getFrom().getX(), e.getFrom().getY(), e.getFrom().getZ()
        );

        sendTransfer(entity, dest.target(), dest.x(), dest.y(), dest.z());
    }

    // ── End Portal (EntityInsideBlockEvent) ──────────────────────────────────
    // Paper does not trigger EntityPortalEvent for non-players in End portals.
    // We use EntityInsideBlockEvent to detect contact with END_PORTAL / END_GATEWAY.

    @EventHandler
    public void onEntityInsideBlock(EntityInsideBlockEvent e) {
        Material type = e.getBlock().getType();
        if (type != Material.END_PORTAL && type != Material.END_GATEWAY) return;
        Entity entity = e.getEntity();
        if (entity instanceof Player) return; // handled by EndPortalListener

        if (isRecentlyTransferred(entity)) return;

        Dimension dimension = DimensionUtil.fromWorld(e.getBlock().getWorld());
        Dimension target;
        double destX, destY, destZ;

        if (type == Material.END_PORTAL) {
            if (dimension == Dimension.OVERWORLD) {
                target = Dimension.END;
                destX = PortalConstants.END_PLATFORM_X;
                destY = PortalConstants.END_PLATFORM_Y;
                destZ = PortalConstants.END_PLATFORM_Z;
            } else if (dimension == Dimension.END) {
                // End → Overworld via exit portal (at origin 0,0,0 of the End)
                target = Dimension.OVERWORLD;
                destX = 0; destY = 64; destZ = 0;
            } else return;
        } else {
            // END_GATEWAY : not handled yet
            return;
        }

        sendTransfer(entity, target, destX, destY, destZ);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isRecentlyTransferred(Entity entity) {
        Long t = RECENTLY_TRANSFERRED.get(entity.getUniqueId());
        if (t == null) return false;
        if (System.currentTimeMillis() - t < TRANSFER_GRACE_MS) return true;
        RECENTLY_TRANSFERRED.remove(entity.getUniqueId());
        return false;
    }

    private void sendTransfer(Entity entity, Dimension target, double destX, double destY, double destZ) {
        Collection<? extends Player> online = plugin.getServer().getOnlinePlayers();
        if (online.isEmpty()) {
            plugin.getLogger().warning("[EntityPortal] No player online, removing entity.");
            entity.remove();
            return;
        }
        Player carrier = online.iterator().next();

        plugin.getLogger().info("[EntityPortal] Transferring " + entity.getType() + " → " + target);

        var out = ByteStreams.newDataOutput();
        EntityTransferSerializer.writeRoot(out, entity, target, destX, destY, destZ);
        carrier.sendPluginMessage(plugin, Channels.ENTITY_TRANSFER.toString(), out.toByteArray());

        entity.getPassengers().forEach(entity::removePassenger);
        entity.remove();
    }
}
