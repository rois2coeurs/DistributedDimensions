package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.paper.DistributedDimensions;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Ensures that no player remains in a world that is not the dimension
 * managed by this server.
 *
 * <ul>
 *   <li>On join: checks after a short delay (enough time for an eventual DIM_SWITCH
 *       to occur) that the player is in the correct world.</li>
 *   <li>On world change: corrects immediately if the new world is
 *       incorrect (e.g. /world command by an admin).</li>
 * </ul>
 */
public class DimensionGuardListener implements Listener {
    private final DistributedDimensions plugin;
    private final List<Dimension> dimensions;

    public DimensionGuardListener(DistributedDimensions plugin, List<Dimension> dimensions) {
        this.plugin = plugin;
        this.dimensions = dimensions;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        // Delay: gives DimensionSwitchListener time to teleport the player
        // before checking. 20 ticks = 1 second, which is more than enough.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) guard(player);
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        guard(e.getPlayer());
    }

    private void guard(Player player) {
        if (dimensions.isEmpty()) return;

        String currentWorld = player.getWorld().getName();
        for (Dimension dim : dimensions) {
            if (currentWorld.equals(dim.toBukkitWorldName())) {
                return; // Player in an authorized world
            }
        }

        // Player in an unauthorized world, send them to the first world in the list
        Dimension fallbackDimension = dimensions.get(0);
        String expected = fallbackDimension.toBukkitWorldName();
        World correct = plugin.getServer().getWorld(expected);
        if (correct == null) {
            plugin.getLogger().severe("[DimensionGuard] Fallback world '" + expected + "' could not be found !");
            return;
        }

        plugin.getLogger().warning("[DimensionGuard] " + player.getName()
            + " was in '" + currentWorld
            + "' (not managed by this server). Teleporting to '" + expected + "'.");

        player.teleport(correct.getSpawnLocation());
    }
}
