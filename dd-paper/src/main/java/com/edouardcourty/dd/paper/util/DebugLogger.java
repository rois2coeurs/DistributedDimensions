package com.edouardcourty.dd.paper.util;

import java.util.logging.Logger;

/**
 * Wrapper autour du logger plugin qui filtre les messages debug selon la config.
 */
public class DebugLogger {
    private final Logger logger;
    private final boolean debugEnabled;

    public DebugLogger(Logger logger, boolean debugEnabled) {
        this.logger = logger;
        this.debugEnabled = debugEnabled;
    }

    public void debug(String message) {
        if (debugEnabled) logger.info("[DEBUG] " + message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void severe(String message) {
        logger.severe(message);
    }
}
