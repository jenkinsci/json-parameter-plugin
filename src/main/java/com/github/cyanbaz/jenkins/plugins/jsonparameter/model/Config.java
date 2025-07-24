/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter.model;

import com.github.cyanbaz.jenkins.plugins.jsonparameter.enumeration.ConfigValue;
import java.io.Serial;
import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a JSON source based on Jenkins' Config File Provider plugin.
 * <p>
 * The source can be either a global config file or one scoped to a specific folder.
 * This model is used when {@code SourceValue.CONFIG} is selected.
 * <p>
 * It wraps both folder-level and global-level config references.
 *
 * @author Caner Yanbaz
 */
public class Config implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ConfigValue value;
    private final Folder folder;
    private final Global global;

    /**
     * Constructs a config definition.
     *
     * @param value  Enum indicating folder or global config
     * @param folder The folder-level config reference (nullable)
     * @param global The global config reference (nullable)
     */
    @DataBoundConstructor
    public Config(ConfigValue value, Folder folder, Global global) {
        this.value = value;
        this.folder = folder;
        this.global = global;
    }

    public ConfigValue getValue() {
        return value;
    }

    public Folder getFolder() {
        return folder;
    }

    public Global getGlobal() {
        return global;
    }

    /**
     * Represents a folder-scoped configuration reference.
     * <p>
     * The folder must be identified by its Jenkins full path and the config ID.
     */
    public static class Folder implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final String path;
        private final String id;

        /**
         * Constructs a folder-level config reference.
         *
         * @param path Full Jenkins folder path
         * @param id   ID of the folder config file
         */
        @DataBoundConstructor
        public Folder(String path, String id) {
            this.path = path;
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Represents a global configuration reference.
     * <p>
     * Refers to a config file ID defined in the Jenkins global scope.
     */
    public static class Global implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final String id;

        /**
         * Constructs a global-level config reference.
         *
         * @param id ID of the global config file
         */
        @DataBoundConstructor
        public Global(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
