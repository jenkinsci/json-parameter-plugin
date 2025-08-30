/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.HttpResponses;
import hudson.util.ListBoxModel;
import java.io.Serial;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

/**
 * A JSON-backed Jenkins parameter that can reference the value of another parameter.
 *
 * <p>This behaves like {@link JsonParameterDefinition}, but its JSONPath {@code query}
 * may contain a placeholder in the form <code>${REF}</code>. The placeholder is dynamically
 * substituted with the current value of the referenced parameter (as configured in {@link #getRef()}).
 *
 * <h3>Example</h3>
 * <pre>{@code
 * Query: $[?(@.user == "${USERS}")].email
 * Ref:   USERS
 * }</pre>
 * → The dropdown options will filter e.g. by the currently selected "USERS" parameter.
 *
 * <p>AJAX requests from the build form call {@link DescriptorImpl#doLoadOptions(Job, String, String)}
 * to resolve the reference and load the appropriate dropdown options.</p>
 *
 * @author Caner
 */
public class JsonParameterReferenceDefinition extends AbstractJsonParameterDefinition {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String ref;

    /**
     * Data-bound constructor called by Jenkins when binding UI input.
     *
     * @param name         The name of the parameter
     * @param defaultValue The default value used when none is provided
     * @param source       The JSON source definition
     * @param query        JSONPath query used to extract dropdown values
     * @param ref          Reference to another parameter to get the value from
     */
    @DataBoundConstructor
    public JsonParameterReferenceDefinition(
            String name, String defaultValue, JsonSource source, String query, String ref) {
        super(name, defaultValue, source, query);
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    /**
     * Descriptor for {@link JsonParameterReferenceDefinition}.
     *
     * <p>Registers the parameter type under the symbol {@code jsonParamRef} and provides
     * the Stapler endpoint {@code doLoadOptions}, which Jenkins uses to fetch the filtered
     * options at runtime when the referenced parameter changes.</p>
     */
    @Extension
    @Symbol({"jsonParamRef"})
    public static class DescriptorImpl extends ParameterDescriptor {

        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        /**
         * Returns the display name shown in the Jenkins UI.
         *
         * @return A string representing the UI name
         */
        @Override
        @NonNull
        public String getDisplayName() {
            return "JSON Reference Parameter";
        }

        /**
         * AJAX endpoint used by the build form to populate the dropdown of a
         * {@link JsonParameterReferenceDefinition}.
         *
         * <p>This method is invoked via {@code descriptorByName/.../loadOptions}
         * whenever the referenced parameter changes. It resolves the query by
         * substituting the placeholder (e.g. <code>${USERS}</code>) with the current
         * reference value, evaluates the JSON source, and returns a JSON array of
         * option objects:
         *
         * <pre>
         * [
         *   {"name":"Alice","value":"alice","selected":false},
         *   {"name":"Bob","value":"bob","selected":true}
         * ]
         * </pre>
         *
         * <p>Errors are returned as an array with a single object containing an
         * explanatory {@code name} and empty {@code value}.</p>
         *
         * @param job       the job context (injected by Stapler)
         * @param paramName the name of the parameter definition to resolve
         * @param refValue  the current value of the referenced parameter (may be empty)
         * @return JSON array of dropdown options or an error placeholder
         */
        @RequirePOST
        @POST
        public HttpResponse doLoadOptions(
                @AncestorInPath Job<?, ?> job, @QueryParameter String paramName, @QueryParameter String refValue) {
            if (job == null) return HttpResponses.errorJSON(Messages.error_no_job_context());
            job.checkPermission(Item.READ);

            ParametersDefinitionProperty parametersDefinitionProperty =
                    job.getProperty(ParametersDefinitionProperty.class);
            if (parametersDefinitionProperty == null) return HttpResponses.errorJSON(Messages.error_no_parameter());

            ParameterDefinition parameterDefinition = parametersDefinitionProperty.getParameterDefinition(paramName);
            if (!(parameterDefinition instanceof JsonParameterReferenceDefinition jsonParameterReferenceDefinition)) {
                return HttpResponses.errorJSON(Messages.error_parameter_not_found(paramName));
            }

            JsonSource source = jsonParameterReferenceDefinition.getSource();

            String query = jsonParameterReferenceDefinition.getQuery();
            if (query == null || query.isBlank()) {
                return HttpResponses.errorJSON(Messages.error_missing_query());
            }
            String refName = jsonParameterReferenceDefinition.getRef();
            if (refName == null || refName.isBlank()) {
                return HttpResponses.errorJSON(Messages.error_missing_refName());
            }

            refValue = (refValue != null) ? refValue : "";
            String placeholder = "${" + refName + "}";
            if (query.contains(placeholder)) {
                query = query.replace(placeholder, refValue);
            }

            JsonResult<ListBoxModel> items;
            try {
                items = source.loadOptions(query);
            } catch (Exception exception) {
                LOGGER.log(Level.WARNING, "loadOptions failed", exception);
                return HttpResponses.errorJSON(String.valueOf(exception.getMessage()));
            }

            String defaultValue = jsonParameterReferenceDefinition.getDefaultValue();
            String effectiveSelection = (defaultValue != null && !defaultValue.isBlank())
                    ? defaultValue
                    : (jsonParameterReferenceDefinition.getDefaultValue() != null
                            ? jsonParameterReferenceDefinition.getDefaultValue()
                            : "");

            if (items.isSuccess() && !effectiveSelection.isBlank()) {
                for (ListBoxModel.Option option : items.getValue()) {
                    option.selected = effectiveSelection.equals(option.value);
                }
            }

            return HttpResponses.okJSON(toJsonArray(items));
        }

        /**
         * Converts a {@link JsonResult} of a {@link ListBoxModel} into a JSON array
         * suitable for consumption by the Jelly/JavaScript frontend.
         *
         * <p>On success, each option is rendered with fields
         * {@code name}, {@code value}, and {@code selected}.
         * On error, a single placeholder option is returned.</p>
         */
        private JSONArray toJsonArray(JsonResult<ListBoxModel> items) {
            JSONArray jsonArray = new JSONArray();
            if (items.isSuccess()) {
                ListBoxModel model = items.getValue();
                for (ListBoxModel.Option option : model) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", option.name);
                    obj.put("value", option.value);
                    obj.put("selected", option.selected);
                    jsonArray.add(obj);
                }
            } else {
                JSONObject errorObj = new JSONObject();
                errorObj.put("name", Messages.placeholder_no_reference_value());
                errorObj.put("value", "");
                errorObj.put("selected", false);
                jsonArray.add(errorObj);
            }
            return jsonArray;
        }
    }
}
