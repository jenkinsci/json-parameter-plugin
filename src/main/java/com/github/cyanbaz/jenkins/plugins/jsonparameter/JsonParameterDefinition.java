/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.*;
import java.io.Serial;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Represents a custom Jenkins parameter definition that extracts values from a JSON source
 * (either from a local config file or a remote URL) using a JSONPath expression.
 * <p>
 * This class integrates with Jenkins' build parameters system and provides dynamic dropdown
 * population via the UI or CLI.
 *
 * @author Caner Yanbaz
 */
public class JsonParameterDefinition extends ParameterDefinition {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String defaultValue;
    private final JsonSource source;
    private final String query;

    /**
     * Data-bound constructor called by Jenkins when binding UI input.
     *
     * @param name         The name of the parameter
     * @param defaultValue The default value used when none is provided
     * @param source       The JSON source definition
     * @param query        JSONPath query used to extract dropdown values
     */
    @DataBoundConstructor
    public JsonParameterDefinition(String name, String defaultValue, JsonSource source, String query) {
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
     * Returns a localized message used when no options are available in the dropdown.
     *
     * @return A localized empty option message
     */
    public String getPlaceholderMessage() {
        return Messages.placeholder_select_option();
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
     * Creates a parameter value from the submitted JSON object in the web form.
     *
     * @param req The stapler request
     * @param jo  The submitted JSON object
     * @return A new StringParameterValue instance
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

    /**
     * Descriptor for this parameter type, defines how it is represented in Jenkins UI.
     */
    @Extension
    @Symbol({"jsonParam"})
    public static class DescriptorImpl extends ParameterDescriptor {

        /**
         * Returns the display name shown in the Jenkins UI.
         *
         * @return A string representing the UI name
         */
        @Override
        @NonNull
        public String getDisplayName() {
            return "JSON Parameter";
        }
    }
}
