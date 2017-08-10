package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

/**
 * This abstract class is the base for all defined Handlebars helper in wiremock. It basically allows simpler error
 * handling.
 *
 * @param <T> Type used as context for the Handlebars helper.
 * @author Christopher Holomek
 */
public abstract class HandlebarsHelper<T> implements Helper<T> {
    public static final String ERROR_PREFIX = "Handlebars Helper Error: ";


    /**
     * Handle invalid helper data without exception details or because none was thrown.
     *
     * @param message message to log and return
     * @return a message which will be used as content
     */
    protected String handleError(final String message) {
        notifier().error(ERROR_PREFIX + message);
        return ERROR_PREFIX + message;
    }

    /**
     * Handle invalid helper data with exception details in the log message.
     *
     * @param message message to log and return
     * @param cause   which occurred during application of the helper
     * @return a message which will be used as content
     */
    protected String handleError(final String message, final Throwable cause) {
        notifier().error(ERROR_PREFIX + message, cause);
        return ERROR_PREFIX + message;
    }

    /**
     * Handle invalid helper data with exception details in the log message. Also additional information regarding the
     * issue is written in the logs.
     *
     * @param message      message to log and return
     * @param logExclusive additional information just for the log
     * @param cause        which occured during application of the helper
     * @return a message which will be used as content
     */
    protected String handleError(final String message, final String logExclusive, final Throwable cause) {
        notifier().error(ERROR_PREFIX + message + " - " + logExclusive, cause);
        return ERROR_PREFIX + message;
    }
}
