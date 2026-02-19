package com.edouardcourty.dd.paper.service;

import com.edouardcourty.dd.common.model.Dimension;

/**
 * Applique le ratio de coordonnées 1:8 entre l'Overworld et le Nether.
 */
public final class NetherCoordinateScaler {
    private NetherCoordinateScaler() {}

    public record ScaledCoords(double x, double y, double z, Dimension target) {}

    /**
     * Calcule les coordonnées de destination en appliquant le ratio Overworld↔Nether.
     *
     * @param fromDim dimension source (doit être OVERWORLD ou NETHER)
     * @param x       coordonnée X source
     * @param y       coordonnée Y source
     * @param z       coordonnée Z source
     * @return coordonnées scalées et dimension cible
     */
    public static ScaledCoords scale(Dimension fromDim, double x, double y, double z) {
        return switch (fromDim) {
            case OVERWORLD -> new ScaledCoords(x / 8.0, y, z / 8.0, Dimension.NETHER);
            case NETHER    -> new ScaledCoords(x * 8.0, y, z * 8.0, Dimension.OVERWORLD);
            default        -> throw new IllegalArgumentException("NetherCoordinateScaler only supports OVERWORLD and NETHER, got: " + fromDim);
        };
    }
}
