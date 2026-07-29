package com.edouardcourty.dd.paper.service;

import com.edouardcourty.dd.common.model.Dimension;

/**
 * Applies the 1:8 coordinate ratio between the Overworld and the Nether.
 */
public final class NetherCoordinateScaler {
    private NetherCoordinateScaler() {}

    public record ScaledCoords(double x, double y, double z, Dimension target) {}

    /**
     * Calculates the destination coordinates by applying the Overworld↔Nether ratio.
     *
     * @param fromDim source dimension (must be OVERWORLD or NETHER)
     * @param x       source X coordinate
     * @param y       source Y coordinate
     * @param z       source Z coordinate
     * @return scaled coordinates and target dimension
     */
    public static ScaledCoords scale(Dimension fromDim, double x, double y, double z) {
        return switch (fromDim) {
            case OVERWORLD -> new ScaledCoords(x / 8.0, y, z / 8.0, Dimension.NETHER);
            case NETHER    -> new ScaledCoords(x * 8.0, y, z * 8.0, Dimension.OVERWORLD);
            default        -> throw new IllegalArgumentException("NetherCoordinateScaler only supports OVERWORLD and NETHER, got: " + fromDim);
        };
    }
}
