package com.edouardcourty.dd.common.model;

public enum Dimension {
    OVERWORLD, NETHER, END;

    public String toBukkitWorldName() {
        return switch (this) {
            case OVERWORLD -> "world";
            case NETHER -> "world_nether";
            case END -> "world_the_end";
        };
    }
}
