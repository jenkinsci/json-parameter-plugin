/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.Descriptor;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link JsonSource} implementation that retrieves JSON content from a remote HTTP(S) endpoint.
 * <p>
 * Supports Jenkins proxy configuration automatically via {@link ProxyConfiguration}.
 * The configured URL must return a valid JSON response body. This source type is intended
 * for scenarios where external services expose data for dynamic parameterization.
 *
 * @author Caner Yanbaz
 */
public class RemoteSource extends JsonSource {

    private final String url;

    /**
     * Constructs a new remote JSON source.
     *
     * @param url the remote URL to fetch the JSON from
     */
    @DataBoundConstructor
    public RemoteSource(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Fetches the JSON content from the configured remote URL using Java's {@link HttpClient}.
     * <p>
     * Automatically honors the Jenkins global proxy configuration if set.
     * If the HTTP response returns a status code of 400 or higher, an {@link IOException} is thrown.
     *
     * @return the raw JSON response body as a string
     * @throws IOException          if the HTTP request fails or the status code indicates an error
     * @throws InterruptedException if the request is interrupted
     */
    @Override
    public String loadJson() throws IOException, InterruptedException {
        URI uri = URI.create(url);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpClient client = ProxyConfiguration.newHttpClient();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status >= 400) {
            throw new IOException("Failed to fetch JSON from URL: HTTP " + status + " - " + response.body());
        }

        return response.body();
    }

    /**
     * Descriptor for the {@link RemoteSource}, used to display this option in the Jenkins UI dropdown.
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
            return "Remote URL";
        }
    }
}
