package com.github.sportbot.service;

@FunctionalInterface
public interface MessageLocalizer {

    /**
     * Returns a localized message for the given key and optional context object.
     *
     * @param messageKey the message key to look up in the message source
     * @param context    an optional context object used to format or enrich the message;
     *                   may be {@code null} if no context is required
     * @return the localized message corresponding to the given key and context
     */
    String localize(String messageKey, Object context);
}
