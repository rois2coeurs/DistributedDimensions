package com.edouardcourty.dd.common.model;

public final class PortalConstants {
    private PortalConstants() {}

    /**
     * Cooldown appliqué au joueur après son ARRIVÉE dans une dimension (via setPortalCooldown).
     * 60 ticks = 3 secondes — juste assez pour éviter un re-trigger immédiat sans bloquer
     * l'utilisation normale du portail retour.
     */
    public static final int PLAYER_PORTAL_COOLDOWN_TICKS = 60;

    /**
     * Grace period entités récemment transférées (en ticks, converti en ms côté listener).
     * 300 ticks = 15 secondes — empêche une entité de re-traverser immédiatement.
     */
    public static final int PORTAL_COOLDOWN_TICKS = 300;

    /** Coordonnées de la plateforme d'arrivée de l'End (vanilla). */
    public static final int END_PLATFORM_X = 100;
    public static final int END_PLATFORM_Y = 49;
    public static final int END_PLATFORM_Z = 0;
}
