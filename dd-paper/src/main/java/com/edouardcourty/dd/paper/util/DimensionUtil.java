package com.edouardcourty.dd.paper.util;

import com.edouardcourty.dd.common.model.Dimension;
import org.bukkit.World;

public final class DimensionUtil {
    private DimensionUtil() {}

    public static Dimension fromWorld(World world) {
        return switch (world.getEnvironment()) {
            case NETHER -> Dimension.NETHER;
            case THE_END -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }
}
