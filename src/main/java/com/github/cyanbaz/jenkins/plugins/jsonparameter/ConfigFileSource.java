/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;

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

    private final boolean folderScoped;
    private final String folderPath;
    private final String configId;

    /**
     * Constructs a new {@link ConfigFileSource} instance.
     *
     * @param folderScoped whether the config file is folder-scoped
     * @param folderPath   the full Jenkins path of the folder (only relevant if folderScoped is true)
     * @param configId     the ID of the config file as defined in the Config File Provider plugin
     */
    @DataBoundConstructor
    public ConfigFileSource(boolean folderScoped, String folderPath, String configId) {
        this.folderScoped = folderScoped;
        this.folderPath = folderPath;
        this.configId = configId;
    }

    public boolean isFolderScoped() {
        return folderScoped;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getConfigId() {
        return configId;
    }

    /**
     * Loads the JSON content from the configured Jenkins config file.
     * <p>
     * If folder-scoped, it checks that the current job is located within the configured folder path.
     *
     * @return the raw JSON content as a string
     * @throws IllegalArgumentException if the config file is not found or not accessible
     * @throws IllegalStateException    if the current job is not within the allowed folder scope
     */
    @Override
    public String loadJson() {
        if (folderScoped) {
            Job<?, ?> job = Stapler.getCurrentRequest2().findAncestorObject(Job.class);
            if (job != null) {
                ItemGroup<?> parent = job.getParent();

                while (parent instanceof Item parentItem) {
                    if (parentItem.getFullName().equals(folderPath)) {
                        FolderConfigFileProperty prop =
                                ((AbstractFolder<?>) parentItem).getProperties().get(FolderConfigFileProperty.class);
                        if (prop != null) {
                            Config config = prop.getById(configId);
                            if (config != null) {
                                return config.content;
                            }
                        }
                        break;
                    }
                    parent = parentItem.getParent();
                }
                throw new IllegalStateException(Messages.error_folder_scope());
            }
        } else {
            Config globalConfig = GlobalConfigFiles.get().getById(configId);
            if (globalConfig != null) {
                return globalConfig.content;
            }
        }

        throw new IllegalArgumentException("Unable to locate config file");
    }

    /**
     * Descriptor for {@link ConfigFileSource}, shown as an option in the dropdown selector.
     */
    @Extension
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
    }
}
