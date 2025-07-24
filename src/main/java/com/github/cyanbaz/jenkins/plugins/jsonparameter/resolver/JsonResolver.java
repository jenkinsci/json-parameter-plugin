/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter.resolver;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.github.cyanbaz.jenkins.plugins.jsonparameter.enumeration.ConfigValue;
import com.github.cyanbaz.jenkins.plugins.jsonparameter.model.Config;
import com.github.cyanbaz.jenkins.plugins.jsonparameter.model.Remote;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty;

/**
 * Utility class that resolves and fetches JSON data from different configuration sources
 * for use in parameter dropdowns or dynamic inputs.
 * <p>
 * This class supports loading JSON from:
 * - Remote HTTP(S) URLs
 * - Folder-scoped config files
 * - Global config files (via Jenkins Config File Provider plugin)
 * <p>
 * It provides helper methods used by {@link com.github.cyanbaz.jenkins.plugins.jsonparameter.JsonParameterDefinition}.
 * <p>
 * This class is not instantiable.
 *
 * @author Caner Yanbaz
 */
public final class JsonResolver {

    /**
     * Fetches raw JSON data from a remote URL using Java's built-in HTTP client.
     *
     * @param remote The remote source object containing the URL to fetch
     * @return The body content of the HTTP response as a String
     * @throws IOException          If the HTTP call fails or cannot be executed
     * @throws InterruptedException If the operation is interrupted
     */
    public static String getJsonDataFromUrl(Remote remote) throws IOException, InterruptedException {
        URI uri = URI.create(remote.getUrl());

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Retrieves JSON content from a configured Jenkins config file, either scoped to a folder or globally.
     *
     * @param config The config definition specifying where to load the JSON from
     * @return The resolved JSON string from the config file
     * @throws IllegalArgumentException If the config cannot be found or resolved
     */
    public static String getJsonDataFromConfigFile(Config config) {
        if (config.getValue() == ConfigValue.FOLDER && config.getFolder().getPath() != null) {
            AbstractFolder<?> folder =
                    Jenkins.get().getItemByFullName(config.getFolder().getPath(), AbstractFolder.class);
            if (folder != null) {
                FolderConfigFileProperty prop = folder.getProperties().get(FolderConfigFileProperty.class);
                if (prop != null) {
                    org.jenkinsci.lib.configprovider.model.Config folderConfig =
                            prop.getById(config.getFolder().getId());
                    if (folderConfig != null) {
                        return folderConfig.content;
                    }
                }
            }
        }

        org.jenkinsci.lib.configprovider.model.Config globalConfig =
                GlobalConfigFiles.get().getById(config.getGlobal().getId());
        if (globalConfig != null) {
            return globalConfig.content;
        }

        throw new IllegalArgumentException(
                "Config with ID " + config.getGlobal().getId() + " not found in folder or global context.");
    }
}
