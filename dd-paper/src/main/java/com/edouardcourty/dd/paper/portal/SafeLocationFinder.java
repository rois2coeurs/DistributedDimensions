package com.edouardcourty.dd.paper.portal;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Orientable;

import java.util.Set;

/**
 * Finds a safe teleport location from the given coordinates.
 *
 * Two modes:
 * - {@link #findAndBuildPortal} : first looks for an existing portal in a radius,
 *   otherwise finds valid ground and builds a portal, otherwise builds with a platform.
 * - {@link #findOnly} : returns just a safe location, without building (respawn).
 *
 * "Safe" = solid ground + 2 blocks of pure air above (no lava, fire, water...).
 * In the Nether, the search is capped at Y={@value #NETHER_MAX_Y} to avoid the roof.
 * If no ground is found in the starting column, the search expands in a spiral
 * up to {@value #MAX_SEARCH_RADIUS} blocks.
 */
public class SafeLocationFinder {

    private static final Set<Material> SAFE_AIR = Set.of(
        Material.AIR, Material.CAVE_AIR, Material.VOID_AIR
    );

    private static final int MAX_SEARCH_RADIUS = 16;
    private static final int NETHER_MAX_Y = 115;

    private SafeLocationFinder() {}

    /**
     * Finds a safe ground AND builds a portal on it.
     * First checks if an existing portal is nearby — if yes, reuses it.
     * Checks that the 4×5 space of the portal is free (2×3 inside is pure air, frame without bedrock).
     * If no valid location found after spiral: builds a portal with platform.
     */
    public static Location findAndBuildPortal(World world, double x, double y, double z, float yaw, float pitch) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        int maxY = getMaxSafeY(world);

        // 1. Look for an existing portal nearby (128 blocks in the Overworld, 16 in the Nether)
        int searchRadius = world.getEnvironment() == World.Environment.NETHER ? 16 : 128;
        Location existing = findExistingPortal(world, ix, iz, searchRadius, maxY, yaw, pitch);
        if (existing != null) return existing;

        // 2. Find a valid ground and build a new portal
        for (int[] offset : spiral(MAX_SEARCH_RADIUS)) {
            int cx = ix + offset[0];
            int cz = iz + offset[1];
            Integer safeY = findSafeY(world, cx, cz, maxY);
            if (safeY != null && hasPortalSpace(world, cx, safeY + 1, cz)) {
                return PortalBuilder.buildOnGround(world, cx, safeY, cz, yaw, pitch);
            }
        }

        // 3. Fallback: build with platform
        return PortalBuilder.buildWithPlatform(world, ix, iz, yaw, pitch);
    }

    /**
     * Finds the arrival for a non-player entity: looks for an existing portal first,
     * otherwise a safe ground. Does not build anything.
     */
    public static Location findEntityArrival(World world, double x, double y, double z) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        int maxY = getMaxSafeY(world);

        int searchRadius = world.getEnvironment() == World.Environment.NETHER ? 16 : 128;
        Location existing = findExistingPortal(world, ix, iz, searchRadius, maxY, 0f, 0f);
        if (existing != null) return existing;

        return findOnly(world, x, y, z, 0f, 0f);
    }

    /**
     * Finds a safe ground without building anything (e.g. respawn).
     * Fallback: Y+1 at the given coordinates.
     */
    public static Location findOnly(World world, double x, double y, double z, float yaw, float pitch) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        int maxY = getMaxSafeY(world);

        for (int[] offset : spiral(MAX_SEARCH_RADIUS)) {
            int cx = ix + offset[0];
            int cz = iz + offset[1];
            Integer safeY = findSafeY(world, cx, cz, maxY);
            if (safeY != null) {
                return new Location(world, cx + 0.5, safeY + 1, cz + 0.5, yaw, pitch);
            }
        }

        return new Location(world, ix + 0.5, y + 1, iz + 0.5, yaw, pitch);
    }

    // Looks for an existing NETHER_PORTAL block in a spiral radius, returns the bottom inside of the portal
    private static Location findExistingPortal(World world, int ix, int iz, int radius, int maxY, float yaw, float pitch) {
        for (int[] offset : spiral(radius)) {
            int cx = ix + offset[0];
            int cz = iz + offset[1];
            for (int cy = world.getMinHeight() + 1; cy < maxY; cy++) {
                if (world.getBlockAt(cx, cy, cz).getType() == Material.NETHER_PORTAL) {
                    return portalSpawnLocation(world, cx, cy, cz, yaw, pitch);
                }
            }
        }
        return null;
    }

    // From a found NETHER_PORTAL block, returns the spawn position OUTSIDE the frame
    private static Location portalSpawnLocation(World world, int px, int py, int pz, float yaw, float pitch) {
        // Go down to the bottom of the portal
        while (py > world.getMinHeight() + 1 && world.getBlockAt(px, py - 1, pz).getType() == Material.NETHER_PORTAL) {
            py--;
        }
        // Place the entity outside the portal according to its axis
        var blockData = world.getBlockAt(px, py, pz).getBlockData();
        if (blockData instanceof Orientable orientable && orientable.getAxis() == Axis.X) {
            // X oriented portal (faces on Z): exit on -Z or +Z side
            while (world.getBlockAt(px - 1, py, pz).getType() == Material.NETHER_PORTAL) px--;
            // Find the width of the portal on X to center
            int maxPx = px;
            while (world.getBlockAt(maxPx + 1, py, pz).getType() == Material.NETHER_PORTAL) maxPx++;
            double centerX = (px + maxPx) / 2.0 + 0.5;
            // Exit on -Z side if possible, otherwise +Z
            double exitZ = SAFE_AIR.contains(world.getBlockAt(px, py, pz - 1).getType())
                ? pz - 0.5 : pz + 1.5;
            return new Location(world, centerX, py, exitZ, yaw, pitch);
        } else {
            // Z oriented portal (faces on X): exit on -X or +X side
            while (world.getBlockAt(px, py, pz - 1).getType() == Material.NETHER_PORTAL) pz--;
            int maxPz = pz;
            while (world.getBlockAt(px, py, maxPz + 1).getType() == Material.NETHER_PORTAL) maxPz++;
            double centerZ = (pz + maxPz) / 2.0 + 0.5;
            // Exit on -X side if possible, otherwise +X
            double exitX = SAFE_AIR.contains(world.getBlockAt(px - 1, py, pz).getType())
                ? px - 0.5 : px + 1.5;
            return new Location(world, exitX, py, centerZ, yaw, pitch);
        }
    }

    // Returns the safe ground Y by searching from top to bottom (priority to the surface)
    private static Integer findSafeY(World world, int ix, int iz, int maxY) {
        for (int iy = maxY - 2; iy >= world.getMinHeight() + 1; iy--) {
            if (world.getBlockAt(ix, iy, iz).getType().isSolid()
                    && SAFE_AIR.contains(world.getBlockAt(ix, iy + 1, iz).getType())
                    && SAFE_AIR.contains(world.getBlockAt(ix, iy + 2, iz).getType())) {
                return iy;
            }
        }
        return null;
    }

    /**
     * Checks that a 4×5 portal can be built cleanly at (ix, frameBaseY, iz):
     * - The 2×3 inside (ix+1..ix+2, frameBaseY+1..frameBaseY+3) must be pure air
     * - The frame positions must not be bedrock (indestructible)
     * - The column above the frame (frameBaseY+5) must have space (no bedrock)
     */
    private static boolean hasPortalSpace(World world, int ix, int frameBaseY, int iz) {
        // 2x3 inside must be pure air
        for (int dx = 1; dx <= 2; dx++) {
            for (int dy = 1; dy <= 3; dy++) {
                if (!SAFE_AIR.contains(world.getBlockAt(ix + dx, frameBaseY + dy, iz).getType())) {
                    return false;
                }
            }
        }
        // The frame (4x5) must not contain bedrock
        for (int dx = 0; dx <= 3; dx++) {
            if (world.getBlockAt(ix + dx, frameBaseY,     iz).getType() == Material.BEDROCK) return false;
            if (world.getBlockAt(ix + dx, frameBaseY + 4, iz).getType() == Material.BEDROCK) return false;
        }
        for (int dy = 0; dy <= 4; dy++) {
            if (world.getBlockAt(ix,     frameBaseY + dy, iz).getType() == Material.BEDROCK) return false;
            if (world.getBlockAt(ix + 3, frameBaseY + dy, iz).getType() == Material.BEDROCK) return false;
        }
        return true;
    }

    // Maximum spawn Y according to the environment (Nether capped at 115 to avoid the roof)
    private static int getMaxSafeY(World world) {
        if (world.getEnvironment() == World.Environment.NETHER) return NETHER_MAX_Y;
        return world.getMaxHeight() - 2;
    }

    // Generates offsets (dx, dz) in a square spiral: (0,0), then perimeter at r=1, r=2...
    private static int[][] spiral(int maxRadius) {
        int size = (2 * maxRadius + 1) * (2 * maxRadius + 1);
        int[][] offsets = new int[size][2];
        int idx = 0;
        for (int r = 0; r <= maxRadius; r++) {
            if (r == 0) {
                offsets[idx++] = new int[]{0, 0};
                continue;
            }
            for (int dx = -r; dx <= r; dx++) {
                offsets[idx++] = new int[]{dx, -r};
                offsets[idx++] = new int[]{dx,  r};
            }
            for (int dz = -r + 1; dz <= r - 1; dz++) {
                offsets[idx++] = new int[]{-r, dz};
                offsets[idx++] = new int[]{ r, dz};
            }
        }
        return offsets;
    }
}
