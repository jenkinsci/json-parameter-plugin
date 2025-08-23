/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.ListBoxModel;
import java.util.List;
import org.jenkinsci.Symbol;
import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.ConfigFiles;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.verb.POST;

/**
 * A {@link JsonSource} implementation that loads JSON content from a Jenkins Config File.
 * <p>
 * Resolution is hierarchical: Jenkins searches the current folder and its parents,
 * falling back to globally defined configuration files if no folder-local match is found.
 * <p>
 * This class uses the Config File Provider plugin for resolution and validation.
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
    public ConfigFileSource(@NonNull String configId) {
        this.configId = configId;
    }

    public String getConfigId() {
        return configId;
    }

    /**
     * Loads the JSON content from the configured Jenkins config file.
     * <p>
     * The config is resolved hierarchically in the context of the current Jenkins item:
     * nearest folder → parent folders → global.
     *
     * @return the raw JSON content as a string
     * @throws IllegalArgumentException if the config file or Jenkins item is not found
     * @throws IllegalStateException if no Jenkins item is available in the current request context
     */
    @Override
    public String loadJson() {
        Item item = Stapler.getCurrentRequest2() != null
                ? Stapler.getCurrentRequest2().findAncestorObject(Item.class)
                : null;
        if (item != null) {
            Config cfg = ConfigFiles.getByIdOrNull(item, configId);
            if (cfg == null) {
                throw new IllegalArgumentException(Messages.error_config_id_not_found(configId));
            }
            return cfg.content;
        }
        throw new IllegalStateException(Messages.error_jenkins_item_not_found());
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
            return "Config File";
        }

        /**
         * Provides a list of available config file IDs for selection in the UI.
         * <p>
         * This uses {@link ConfigFiles#getConfigsInContext} to collect all configs visible
         * to the given item, including folder-scoped and global definitions.
         */
        @POST
        public ListBoxModel doFillConfigIdItems(@AncestorInPath Item item) {
            ListBoxModel items = new ListBoxModel();
            items.add(Messages.placeholder_config_file_id(), "");

            if (item != null) {
                item.checkPermission(Item.CONFIGURE);
                ItemGroup<?> itemGroup = item.getParent();
                for (ConfigProvider provider : ConfigProvider.all()) {
                    List<Config> configIds = ConfigFiles.getConfigsInContext(itemGroup, provider.getClass());
                    if (!configIds.isEmpty()) {
                        for (Config cfg : configIds) {
                            addConfigIfValid(cfg, items);
                        }
                    }
                }
            }
            return items;
        }

        private void addConfigIfValid(Config cfg, ListBoxModel items) {
            if (cfg != null && cfg.id != null) {
                items.add(cfg.name + " (ID: " + cfg.id + ")", cfg.id);
            }
        }
    }
}
