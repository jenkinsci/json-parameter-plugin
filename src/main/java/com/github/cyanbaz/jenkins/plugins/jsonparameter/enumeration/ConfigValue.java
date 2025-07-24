/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter.enumeration;

/**
 * Enumeration indicating the configuration scope in Jenkins.
 * <p>
 * Used by {@link com.github.cyanbaz.jenkins.plugins.jsonparameter.model.Config}
 * to determine whether the JSON config should be resolved from a global config file
 * or from a folder-scoped configuration.
 *
 * @author Caner Yanbaz
 */
public enum ConfigValue {
    /**
     * Use a global configuration file defined in Jenkins' global settings.
     */
    GLOBAL("global"),

    /**
     * Use a configuration file that is defined at the folder level.
     */
    FOLDER("folder");

    private final String value;

    ConfigValue(String value) {
        this.value = value;
    }

    /**
     * Returns the raw string representation of the enum value (used for data binding).
     *
     * @return the enum value as a lowercase string
     */
    public String getValue() {
        return value;
    }
}
