package com.edouardcourty.dd.velocity.util;

import org.slf4j.Logger;

/**
 * Wrapper autour du logger SLF4J qui filtre les messages debug selon la config.
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

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }
}
