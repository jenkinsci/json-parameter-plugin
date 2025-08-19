/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.ListBoxModel;
import java.util.HashSet;
import java.util.Set;
import org.jenkinsci.Symbol;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.ConfigFiles;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.verb.POST;

/**
 * A {@link JsonSource} implementation that loads JSON content from a Jenkins Config File,
 * either globally scoped or folder-scoped.
 * <p>
 * When folder-scoped, the parameter is only available to jobs located within the specified
 * folder or its subfolders.
 * <p>
 * This class supports validation to prevent unauthorized access to configuration files
 * outside the designated folder path.
 *
 * @author Caner Yanbaz
 */
public class ConfigFileSource extends JsonSource {

    private final String configId;

    /**
     * Constructs a new {@link ConfigFileSource} instance.
     *
     * @param configId     the ID of the config file as defined in the Config File Provider plugin
     */
    @DataBoundConstructor
    public ConfigFileSource(String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return configId;
    }

    /**
     * Loads the JSON content from the configured Jenkins config file.
     * <p>
     * If {@code folderScoped} is true, the method resolves the folder using the configured path,
     * ensures the current job is inside that folder, and retrieves the corresponding config.
     * If {@code folderScoped} is false, it attempts to resolve the config globally.
     *
     * @return the raw JSON content as a string
     * @throws IllegalArgumentException if the config file is not found or not accessible
     * @throws IllegalStateException if folder-scoped access fails (e.g. folder mismatch or missing permission)
     */
    @Override
    public String loadJson() {
        Item item = Stapler.getCurrentRequest2() != null
                ? Stapler.getCurrentRequest2().findAncestorObject(Item.class)
                : null;
        if (item != null) {
            Config cfg = ConfigFiles.getByIdOrNull(item, configId);
            if (cfg != null) {
                return cfg.content;
            }
        }
        throw new IllegalArgumentException(Messages.error_config_id_not_found(configId));
    }

    /**
     * Descriptor for {@link ConfigFileSource}, shown as an option in the dropdown selector.
     */
    @Extension
    @Symbol({"configFileSource"})
    public static class DescriptorImpl extends Descriptor<JsonSource> {

        /**
         * Returns the display name shown in the dropdown for this source type.
         *
         * @return a human-readable label for the UI
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return "Jenkins Config File";
        }

        /**
         * Provides a list of available config file IDs for selection in the UI.
         * <p>
         * This method retrieves all config files defined in the Config File Provider plugin,
         * including those defined at the folder level.
         *
         * @param item the current Jenkins item (job or folder) to check permissions against
         * @return a ListBoxModel containing available config file IDs
         */
        @POST
        public ListBoxModel doFillConfigIdItems(@AncestorInPath Item item) {
            ListBoxModel items = new ListBoxModel();
            items.add(Messages.placeholder_config_file_id(), "");

            if (item != null) {
                item.checkPermission(Item.CONFIGURE);
                Set<String> seenIds = new HashSet<>();
                addFolderConfigs(item, items, seenIds);
                addGlobalConfigs(items, seenIds);
            }

            return items;
        }

        private void addFolderConfigs(Item item, ListBoxModel items, Set<String> seenIds) {
            ItemGroup<?> parent = item.getParent();

            while (parent instanceof AbstractFolder<?> folder) {
                FolderConfigFileProperty prop = folder.getProperties().get(FolderConfigFileProperty.class);
                if (prop != null) {
                    for (Config cfg : prop.getConfigs()) {
                        addConfigIfValid(cfg, seenIds, items);
                    }
                }
                parent = folder.getParent();
            }
        }

        private void addGlobalConfigs(ListBoxModel items, Set<String> seenIds) {
            for (Config cfg : GlobalConfigFiles.get().getConfigs()) {
                addConfigIfValid(cfg, seenIds, items);
            }
        }

        private void addConfigIfValid(Config cfg, Set<String> seenIds, ListBoxModel items) {
            if (cfg != null && cfg.id != null && seenIds.add(cfg.id)) {
                items.add(cfg.name + " (ID: " + cfg.id + ")", cfg.id);
            }
        }
    }
}
