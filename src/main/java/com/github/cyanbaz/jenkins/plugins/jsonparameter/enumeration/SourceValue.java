/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter.enumeration;

/**
 * Enumeration defining the available JSON source types.
 * <p>
 * Used in {@link com.github.cyanbaz.jenkins.plugins.jsonparameter.model.Source}
 * to determine whether to load data from a Jenkins config file or a remote URL.
 *
 * @author Caner Yanbaz
 */
public enum SourceValue {
    /**
     * Load JSON data from a Jenkins config file (folder or global scope).
     */
    CONFIG("config"),

    /**
     * Load JSON data from an external HTTP(S) endpoint.
     */
    REMOTE("remote");

    private final String value;

    SourceValue(String value) {
        this.value = value;
    }

    /**
     * Returns the raw string representation of the enum (used for binding).
     *
     * @return the lowercase enum value
     */
    public String getValue() {
        return value;
    }
}
