/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a custom Jenkins parameter definition that extracts values from a JSON source
 * (either from a local config file or a remote URL) using a JSONPath expression.
 * <p>
 * This class integrates with Jenkins' build parameters system and provides dynamic dropdown
 * population via the UI or CLI.
 *
 * @author Caner Yanbaz
 */
public class JsonParameterDefinition extends AbstractJsonParameterDefinition {

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
        super(name, defaultValue, source, query);
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
