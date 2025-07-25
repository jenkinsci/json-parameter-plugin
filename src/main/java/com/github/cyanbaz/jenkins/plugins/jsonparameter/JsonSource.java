/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import com.jayway.jsonpath.JsonPath;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Base class for all JSON data sources used in the {@code json-parameter-plugin}.
 * <p>
 * This abstract class defines the contract for loading JSON data from different sources,
 * such as remote URLs or Jenkins Config File Provider entries.
 * <p>
 * Implementations must provide logic to retrieve JSON content via {@link #loadJson()},
 * and can rely on the provided {@link #loadOptions(String)} method to extract
 * dropdown entries using a JSONPath expression.
 *
 * <p>
 * Subclasses must be registered as Jenkins {@link ExtensionPoint}s and extend
 * {@link hudson.model.Descriptor} to be selectable in the UI via
 * {@code dropdownDescriptorSelector}.
 *
 * @author Caner Yanbaz
 */
public abstract class JsonSource extends AbstractDescribableImpl<JsonSource> implements ExtensionPoint, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Loads the raw JSON string from the configured data source.
     *
     * @return a valid JSON string (never null)
     * @throws IOException              if the data could not be retrieved
     * @throws InterruptedException     if the request is interrupted
     */
    public abstract String loadJson() throws IOException, InterruptedException;

    /**
     * Extracts a list of values from the loaded JSON using the given JSONPath query.
     * <p>
     * This method returns a {@link ListBoxModel} suitable for use in Jenkins dropdown parameters.
     * If {@link #loadJson()} throws {@link IllegalStateException}, the model will contain
     * a single disabled "Invalid" option based on a localized message.
     *
     * @param query a valid JSONPath expression
     * @return a populated {@link ListBoxModel}
     * @throws IOException          if reading JSON fails
     * @throws InterruptedException if the operation is interrupted
     */
    public ListBoxModel loadOptions(String query) throws IOException, InterruptedException {
        ListBoxModel model = new ListBoxModel();
        try {
            String json = loadJson();
            List<String> values = JsonPath.read(json, query);
            for (String value : values) {
                model.add(value, value);
            }
        } catch (IllegalStateException e) {
            model.add("", Messages.folder_invalid());
        }
        return model;
    }
}
