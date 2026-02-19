package com.edouardcourty.dd.paper.portal;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Orientable;

/**
 * Construit un portail du Nether style vanilla.
 *
 * Deux variantes :
 * - {@link #buildOnGround} : cadre 4×5 posé sur un sol existant (pas de plateforme)
 * - {@link #buildWithPlatform} : cadre 4×5 + plateforme obsidienne 1×2 de chaque côté,
 *   utilisé quand il n'y a pas de terrain en dessous (vide, lave...)
 *
 * Structure du cadre :
 *   - Obsidienne bas/haut (4 blocs) + montants gauche/droite (5 blocs)
 *   - 6 blocs NETHER_PORTAL à l'intérieur (2 colonnes × 3 rangées, axe X)
 *
 * Le joueur est téléporté au centre-bas de l'intérieur du portail.
 */
public class PortalBuilder {

    private PortalBuilder() {}

    /**
     * Construit le portail sur un sol existant (baseY = Y du sol, le portail part de baseY+1).
     */
    public static Location buildOnGround(World world, int ix, int baseY, int iz, float yaw, float pitch) {
        buildFrame(world, ix, baseY + 1, iz);
        return new Location(world, ix + 1.5, baseY + 2, iz + 0.5, yaw, pitch);
    }

    /**
     * Construit le portail avec une plateforme d'obsidienne de chaque côté,
     * quand il n'y a pas de sol disponible (vide, lave...).
     * La plateforme est placée à baseY, le cadre part de baseY.
     */
    public static Location buildWithPlatform(World world, int ix, int iz, float yaw, float pitch) {
        int baseY = Math.max(world.getMinHeight() + 5, Math.min(64, world.getMaxHeight() - 10));

        // Plateforme : 1×2 de chaque côté du cadre, à la même hauteur que le bas du cadre
        world.getBlockAt(ix - 1, baseY, iz    ).setType(Material.OBSIDIAN);
        world.getBlockAt(ix - 1, baseY, iz + 1).setType(Material.OBSIDIAN);
        world.getBlockAt(ix + 4, baseY, iz    ).setType(Material.OBSIDIAN);
        world.getBlockAt(ix + 4, baseY, iz + 1).setType(Material.OBSIDIAN);

        buildFrame(world, ix, baseY, iz);
        return new Location(world, ix + 1.5, baseY + 1, iz + 0.5, yaw, pitch);
    }

    /**
     * Construit (ou régénère) la plateforme d'arrivée de l'End vanilla : 5×5 obsidienne à (x-2, y, z-2).
     * Appelé systématiquement à chaque arrivée dans l'End pour reproduire le comportement vanilla
     * (la plateforme est toujours reconstruite, même si elle a été détruite).
     */
    public static Location buildEndPlatform(World world, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.OBSIDIAN);
                // Vider les 3 blocs d'air au-dessus
                for (int dy = 1; dy <= 3; dy++) {
                    world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                }
            }
        }
        return new Location(world, x + 0.5, y + 1, z + 0.5, 90f, 0f);
    }

    private static void buildFrame(World world, int ix, int baseY, int iz) {
        // Cadre obsidienne 4×5
        for (int px = ix; px <= ix + 3; px++) {
            world.getBlockAt(px, baseY,     iz).setType(Material.OBSIDIAN);
            world.getBlockAt(px, baseY + 4, iz).setType(Material.OBSIDIAN);
        }
        for (int py = baseY; py <= baseY + 4; py++) {
            world.getBlockAt(ix,     py, iz).setType(Material.OBSIDIAN);
            world.getBlockAt(ix + 3, py, iz).setType(Material.OBSIDIAN);
        }

        // Blocs portail 2×3 à l'intérieur (axe X)
        Orientable portalData = (Orientable) Material.NETHER_PORTAL.createBlockData();
        portalData.setAxis(Axis.X);
        for (int py = baseY + 1; py <= baseY + 3; py++) {
            world.getBlockAt(ix + 1, py, iz).setBlockData(portalData.clone());
            world.getBlockAt(ix + 2, py, iz).setBlockData(portalData.clone());
        }
    }
}

