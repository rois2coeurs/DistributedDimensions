package com.edouardcourty.dd.common.model;

public final class PortalConstants {
    private PortalConstants() {}

    /**
     * Cooldown applied to the player after ARRIVING in a dimension (via setPortalCooldown).
     * 60 ticks = 3 seconds — just enough to avoid an immediate re-trigger without blocking
     * normal use of the return portal.
     */
    public static final int PLAYER_PORTAL_COOLDOWN_TICKS = 60;

    /**
     * Grace period for recently transferred entities (in ticks, converted to ms on the listener side).
     * 300 ticks = 15 seconds — prevents an entity from immediately crossing back.
     */
    public static final int PORTAL_COOLDOWN_TICKS = 300;

    /** Coordinates of the End arrival platform (vanilla). */
    public static final int END_PLATFORM_X = 100;
    public static final int END_PLATFORM_Y = 49;
    public static final int END_PLATFORM_Z = 0;
}
