/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import hudson.cli.CLICommand;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import java.io.Serial;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Base class for JSON-backed build parameters.
 *
 * <p>Implementations provide a {@link JsonSource} and a JSONPath {@code query} which,
 * together, define how the selectable values are derived for a dropdown (or any
 * UI that consumes those options). This class itself does <b>not</b> perform JSON
 * evaluation; it only stores configuration and handles value binding for web and CLI.</p>
 *
 * <p><b>Responsibilities</b></p>
 * <ul>
 *   <li>Holds the configured {@code defaultValue}, {@link JsonSource}, and JSONPath {@code query}.</li>
 *   <li>Creates {@link StringParameterValue}s from Web UI submissions and CLI invocations.</li>
 *   <li>Exposes a localized placeholder message for empty option lists.</li>
 * </ul>
 *
 * <h3>Value binding</h3>
 * <ul>
 *   <li><b>Web UI (form submit):</b> expects a single form field named {@code value}.
 *       If empty/missing, {@code defaultValue} is used.</li>
 *   <li><b>Web UI (JSON submit):</b> expects a JSON property {@code "value"} (string).</li>
 *   <li><b>CLI:</b> if the argument is empty or {@code null}, the parameter falls back
 *       to {@code defaultValue}.</li>
 * </ul>
 *
 * <p><b>Note:</b> Option loading (AJAX/Stapler endpoints) and JSON evaluation are handled
 * by concrete subclasses/descriptors and are out of scope for this base class.</p>
 *
 * @author Caner Yanbaz
 * @since 1.0
 */
public abstract class AbstractJsonParameterDefinition extends ParameterDefinition {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String defaultValue;
    private final JsonSource source;
    private final String query;

    /**
     * Creates a new JSON-backed parameter definition.
     *
     * @param name         the parameter name as shown in Jenkins
     * @param defaultValue the fallback value when no user input is provided (can be {@code null} or empty)
     * @param source       the JSON source implementation providing raw JSON content (must not be {@code null})
     * @param query        the JSONPath used to extract option values (may contain placeholders for refs)
     */
    public AbstractJsonParameterDefinition(String name, String defaultValue, JsonSource source, String query) {
        super(name);
        this.defaultValue = defaultValue;
        this.source = source;
        this.query = query;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public JsonSource getSource() {
        return source;
    }

    public String getQuery() {
        return query;
    }

    /**
     * Returns the default parameter value as configured.
     *
     * @return StringParameterValue with default value
     */
    @Override
    public ParameterValue getDefaultParameterValue() {
        return new StringParameterValue(getName(), defaultValue);
    }

    /**
     * Creates a parameter value from a JSON-backed web form submission.
     *
     * <p>Expects a {@code "value"} property in the submitted JSON. If it's not present
     * or not a string, falls back to an empty string which, depending on consumers, may
     * trigger the use of {@link #getDefaultParameterValue()} downstream.</p>
     *
     * @param req the Stapler request (unused here)
     * @param jo  the submitted JSON object (must contain {@code "value"} as a string)
     * @return a {@link StringParameterValue} for Jenkins build execution
     */
    @Override
    public ParameterValue createValue(StaplerRequest2 req, JSONObject jo) {
        Object value = jo.get("value");
        String stringValue = "";
        if (value instanceof String) {
            stringValue = (String) value;
        }
        return new StringParameterValue(getName(), stringValue);
    }

    /**
     * Creates a parameter value from a standard web form submission.
     *
     * @param req The stapler request
     * @return A new StringParameterValue instance
     */
    @Override
    public ParameterValue createValue(StaplerRequest2 req) {
        String[] value = req.getParameterValues(getName());
        String result = (value == null || value.length == 0 || value[0].isEmpty()) ? defaultValue : value[0];
        return new StringParameterValue(getName(), result);
    }

    /**
     * Creates a parameter value from a CLI command.
     *
     * @param command The CLI command
     * @param value   The CLI argument provided
     * @return A new StringParameterValue instance
     */
    @Override
    public ParameterValue createValue(CLICommand command, String value) {
        if (value == null || value.isEmpty()) {
            return getDefaultParameterValue();
        } else {
            return new StringParameterValue(getName(), value);
        }
    }
}
