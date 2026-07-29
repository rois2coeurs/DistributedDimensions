package com.edouardcourty.dd.paper.portal;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Orientable;

/**
 * Builds a vanilla-style Nether portal.
 *
 * Two variants:
 * - {@link #buildOnGround} : 4×5 frame placed on an existing ground (no platform)
 * - {@link #buildWithPlatform} : 4×5 frame + 1×2 obsidian platform on each side,
 *   used when there is no terrain below (void, lava...)
 *
 * Frame structure:
 *   - Obsidian bottom/top (4 blocks) + left/right pillars (5 blocks)
 *   - 6 NETHER_PORTAL blocks inside (2 columns × 3 rows, X axis)
 *
 * The player is teleported to the bottom center of the inside of the portal.
 */
public class PortalBuilder {

    private PortalBuilder() {}

    /**
     * Builds the portal on an existing ground (baseY = ground Y, the portal starts at baseY+1).
     */
    public static Location buildOnGround(World world, int ix, int baseY, int iz, float yaw, float pitch) {
        buildFrame(world, ix, baseY + 1, iz);
        return new Location(world, ix + 1.5, baseY + 2, iz + 0.5, yaw, pitch);
    }

    /**
     * Builds the portal with an obsidian platform on each side,
     * when there is no ground available (void, lava...).
     * The platform is placed at baseY, the frame starts at baseY.
     */
    public static Location buildWithPlatform(World world, int ix, int iz, float yaw, float pitch) {
        int baseY = Math.max(world.getMinHeight() + 5, Math.min(64, world.getMaxHeight() - 10));

        // Platform: 1×2 on each side of the frame, at the same height as the bottom of the frame
        world.getBlockAt(ix - 1, baseY, iz    ).setType(Material.OBSIDIAN);
        world.getBlockAt(ix - 1, baseY, iz + 1).setType(Material.OBSIDIAN);
        world.getBlockAt(ix + 4, baseY, iz    ).setType(Material.OBSIDIAN);
        world.getBlockAt(ix + 4, baseY, iz + 1).setType(Material.OBSIDIAN);

        buildFrame(world, ix, baseY, iz);
        return new Location(world, ix + 1.5, baseY + 1, iz + 0.5, yaw, pitch);
    }

    /**
     * Builds (or regenerates) the vanilla End arrival platform: 5×5 obsidian at (x-2, y, z-2).
     * Called systematically at each arrival in the End to reproduce vanilla behavior
     * (the platform is always rebuilt, even if it was destroyed).
     */
    public static Location buildEndPlatform(World world, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.OBSIDIAN);
                // Empty the 3 blocks of air above
                for (int dy = 1; dy <= 3; dy++) {
                    world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                }
            }
        }
        return new Location(world, x + 0.5, y + 1, z + 0.5, 90f, 0f);
    }

    private static void buildFrame(World world, int ix, int baseY, int iz) {
        // 4×5 Obsidian frame
        for (int px = ix; px <= ix + 3; px++) {
            world.getBlockAt(px, baseY,     iz).setType(Material.OBSIDIAN);
            world.getBlockAt(px, baseY + 4, iz).setType(Material.OBSIDIAN);
        }
        for (int py = baseY; py <= baseY + 4; py++) {
            world.getBlockAt(ix,     py, iz).setType(Material.OBSIDIAN);
            world.getBlockAt(ix + 3, py, iz).setType(Material.OBSIDIAN);
        }

        // 2×3 portal blocks inside (X axis)
        Orientable portalData = (Orientable) Material.NETHER_PORTAL.createBlockData();
        portalData.setAxis(Axis.X);
        for (int py = baseY + 1; py <= baseY + 3; py++) {
            world.getBlockAt(ix + 1, py, iz).setBlockData(portalData.clone());
            world.getBlockAt(ix + 2, py, iz).setBlockData(portalData.clone());
        }
    }
}

