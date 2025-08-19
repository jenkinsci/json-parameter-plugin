/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.verb.POST;

/**
 * A {@link JsonSource} implementation that retrieves JSON content from a remote HTTP(S) endpoint.
 * <p>
 * Supports Jenkins proxy configuration automatically via {@link ProxyConfiguration}.
 * <p>
 * Authentication is optional:
 * - If no credentials are provided, a plain HTTP(S) request is sent.
 * - If a credentials ID is provided, the plugin supports:
 *   - {@link StandardUsernamePasswordCredentials}: Sent as HTTP Basic Auth or Bearer if username is empty.
 *   - {@link StringCredentials}: Sent as Bearer token in the Authorization header.
 * <p>
 * The configured URL must return a valid JSON response body. This source type is ideal for
 * external services that expose dynamic data for parameter injection.
 *
 * @author Caner Yanbaz
 */
public class RemoteSource extends JsonSource {

    private final String url;
    private final String credentialsId;

    /**
     * Constructs a new {@link RemoteSource}.
     *
     * @param url the remote URL returning JSON content
     * @param credentialsId optional credentials ID for authentication (username/password)
     */
    @DataBoundConstructor
    public RemoteSource(String url, String credentialsId) {
        this.url = url;
        this.credentialsId = credentialsId;
    }

    public String getUrl() {
        return url;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Fetches the JSON content from the configured remote URL using Java's {@link HttpClient}.
     * <p>
     * - If {@code credentialsId} is defined, the appropriate Authorization header is added based on
     *   the resolved credentials type (username/password or secret text).
     * - Jenkins global proxy configuration is used automatically.
     * - Throws an {@link IOException} if the response code is 400 or higher.
     *
     * @return the raw JSON response body as a string
     * @throws IOException          if the HTTP request fails or the status code indicates an error
     * @throws InterruptedException if the request is interrupted
     * @throws IllegalArgumentException if the credentials ID is invalid or not found
     */
    @Override
    public String loadJson() throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        if (credentialsId != null && !credentialsId.isEmpty()) {
            Credentials credentials = resolveCredentials();
            if (credentials == null) {
                throw new IllegalArgumentException("Credentials not found: " + credentialsId);
            }
            String authHeader = buildAuthorizationHeader(credentials);
            builder.header("Authorization", authHeader);
        }

        return executeRequest(builder.build());
    }

    /**
     * Resolves the credentials (Username/Password or Secret Text) from the configured {@code credentialsId}.
     * The search is scoped to the current {@link Job} context to ensure correct permissions and folder scoping.
     *
     * @return the resolved {@link Credentials} instance or {@code null} if not found
     */
    private Credentials resolveCredentials() {
        Job<?, ?> job = Stapler.getCurrentRequest2().findAncestorObject(Job.class);
        if (job == null) {
            throw new IllegalStateException("No job context found");
        }

        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentialsInItem(
                        Credentials.class,
                        job,
                        ACL.SYSTEM2,
                        URIRequirementBuilder.fromUri(url).build()),
                CredentialsMatchers.withId(credentialsId));
    }

    /**
     * Builds an HTTP Authorization header value based on the provided credentials type.
     * <p>
     * Supported:
     * - {@link StandardUsernamePasswordCredentials}: Returns "Basic base64(username:password)"
     *   or "Bearer password" if username is empty.
     * - {@link StringCredentials}: Returns "Bearer <token>"
     *
     * @param credentials the credentials object to encode
     * @return the full Authorization header value
     * @throws IllegalArgumentException if the credentials type is unsupported
     */
    private String buildAuthorizationHeader(Credentials credentials) {
        if (credentials instanceof StandardUsernamePasswordCredentials userPass) {
            String user = userPass.getUsername();
            String pass = userPass.getPassword().getPlainText();
            return !user.isEmpty()
                    ? "Basic "
                            + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8))
                    : "Bearer " + pass;
        } else if (credentials instanceof StringCredentials token) {
            String secret = token.getSecret().getPlainText();
            return "Bearer " + secret;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported credentials type: " + credentials.getClass().getName());
        }
    }

    private String executeRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpClient client = ProxyConfiguration.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status >= 400) {
            throw new IOException("Failed to fetch JSON from URL '" + url + "' using credentials '"
                    + credentialsId + "': HTTP "
                    + status + " - " + response.body());
        }
        return response.body();
    }

    /**
     * Descriptor for the {@link RemoteSource}, used to display this option in the Jenkins UI dropdown.
     */
    @Extension
    @Symbol({"remoteSource"})
    public static class DescriptorImpl extends Descriptor<JsonSource> {

        /**
         * Returns the display name shown in the dropdown for this source type.
         *
         * @return a human-readable label for the UI
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return "Remote";
        }

        /**
         * Populates the credentials dropdown in the UI with available username/password credentials.
         *
         * <p>Includes:
         * <ul>
         *   <li>Global credentials</li>
         *   <li>Folder credentials</li>
         *   <li>Only credentials matching the URI scope</li>
         * </ul>
         *
         * @param context the current item context (job, folder, etc.)
         * @return a {@link ListBoxModel} of available credentials
         */
        @POST
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context) {
            if (context == null || !context.hasPermission(Item.CONFIGURE)) {
                return new ListBoxModel();
            }

            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            context instanceof Queue.Task
                                    ? Tasks.getAuthenticationOf2((Queue.Task) context)
                                    : ACL.SYSTEM2,
                            context,
                            StandardCredentials.class,
                            URIRequirementBuilder.create().build(),
                            CredentialsMatchers.always());
        }
    }
}
