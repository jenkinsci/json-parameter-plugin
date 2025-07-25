/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import com.github.cyanbaz.jenkins.plugins.jsonparameter.enumeration.SourceValue;
import com.github.cyanbaz.jenkins.plugins.jsonparameter.model.Source;
import com.github.cyanbaz.jenkins.plugins.jsonparameter.resolver.JsonResolver;
import com.jayway.jsonpath.JsonPath;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.verb.POST;

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

    private static final Logger LOGGER = Logger.getLogger(JsonParameterDefinition.class.getName());

    @Serial
    private static final long serialVersionUID = 1L;

    private final String defaultValue;
    private final Source source;
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
    public JsonParameterDefinition(String name, String defaultValue, Source source, String query) {
        super(name);
        this.defaultValue = defaultValue;
        this.source = source;
        this.query = query;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Source getSource() {
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
    public String getEmptyOptionMessage() {
        return Messages.option_empty();
    }

    /**
     * Returns the default parameter value as configured.
     *
     * @return JsonParameterValue with default value
     */
    @Override
    public ParameterValue getDefaultParameterValue() {
        LOGGER.info(defaultValue);
        return new JsonParameterValue(getName(), defaultValue);
    }

    /**
     * Creates a parameter value from the submitted JSON object in the web form.
     *
     * @param req The stapler request
     * @param jo  The submitted JSON object
     * @return A new JsonParameterValue instance
     */
    @Override
    public ParameterValue createValue(StaplerRequest2 req, JSONObject jo) {
        Object value = jo.get("value");
        String stringValue = "";
        if (value instanceof String) {
            stringValue = (String) value;
        }
        return new JsonParameterValue(getName(), stringValue);
    }

    /**
     * Creates a parameter value from a standard web form submission.
     *
     * @param req The stapler request
     * @return A new JsonParameterValue instance
     */
    @Override
    public ParameterValue createValue(StaplerRequest2 req) {
        String[] value = req.getParameterValues(getName());
        String result = (value == null || value.length == 0 || value[0].isEmpty()) ? defaultValue : value[0];
        return new JsonParameterValue(getName(), result);
    }

    /**
     * Creates a parameter value from a CLI command.
     *
     * @param command The CLI command
     * @param value   The CLI argument provided
     * @return A new JsonParameterValue instance
     */
    @Override
    public ParameterValue createValue(CLICommand command, String value) {
        if (value == null || value.isEmpty()) {
            return getDefaultParameterValue();
        } else {
            return new JsonParameterValue(getName(), value);
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

        /**
         * Populates the dropdown in the UI with values extracted from the configured JSON source.
         *
         * @param source The configured source (file or remote)
         * @param query  The JSONPath query to apply
         * @return A ListBoxModel containing the dropdown options
         * @throws IOException          If an error occurs reading the source
         * @throws InterruptedException If the operation is interrupted
         */
        @POST
        public ListBoxModel doFillValueItems(@QueryParameter Source source, @QueryParameter String query)
                throws IOException, InterruptedException {

            Jenkins.get().checkPermission(Jenkins.READ);

            ListBoxModel model = new ListBoxModel();

            String json = "";
            if (source.getValue() == SourceValue.CONFIG) {
                json = JsonResolver.getJsonDataFromConfigFile(source.getConfig());
            } else if (source.getValue() == SourceValue.REMOTE) {
                json = JsonResolver.getJsonDataFromUrl(source.getRemote());
            }

            List<String> values = JsonPath.read(json, query);

            for (String value : values) {
                model.add(value, value);
            }
            return model;
        }
    }
}
