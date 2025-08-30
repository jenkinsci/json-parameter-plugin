/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import com.jayway.jsonpath.JsonPath;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.util.HttpResponses;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.ConfigFiles;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.verb.POST;

import java.io.Serial;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a custom Jenkins parameter definition that extracts values from a JSON source
 * (either from a local config file or a remote URL) using a JSONPath expression.
 * <p>
 * This class integrates with Jenkins' build parameters system and provides dynamic dropdown
 * population via the UI or CLI.
 *
 * @author Caner Yanbaz
 */
public class JsonParameterReferenceDefinition extends JsonParameterDefinition {

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
    public JsonParameterReferenceDefinition(String name, String defaultValue, JsonSource source, String query, String ref) {
        super(name, defaultValue, source, query);
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    /**
     * Descriptor for this parameter type, defines how it is represented in Jenkins UI.
     */
    @Extension
    @Symbol({"jsonParamRef"})
    public static class DescriptorImpl extends ParameterDescriptor {

        Logger logger = Logger.getLogger(ParameterDescriptor.class.getName());

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

        @POST
        public HttpResponse doLoadOptions(
                @AncestorInPath Item item,
                @QueryParameter String query,
                @QueryParameter String refName,
                @QueryParameter String refValue,
                @QueryParameter String configId
        ) {
            logger.info("doLoadOptions called with query: " + query + ", refName: " + refName + ", refValue: " + refValue + ", configId: " + configId);
            Jenkins.get().checkPermission(Jenkins.READ);
            JsonResult<ListBoxModel> items = loadOptions(item, query, refName, refValue, configId);
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
                errorObj.put("error", items.getErrorMessage());
                jsonArray.add(errorObj);
            }
            return HttpResponses.okJSON(jsonArray);
        }

        public JsonResult<ListBoxModel> loadOptions(Item item, String query, String refName, String refValue, String configId) {
            ListBoxModel model = new ListBoxModel();
            query = query.replace("${" + refName + "}", refValue);
            return getListBoxModelJsonResult(item, query, model, configId);
        }

        private JsonResult<ListBoxModel> getListBoxModelJsonResult(Item item, String query, ListBoxModel model, String configId) {
            try {
                String json = loadJson(item, configId);
                List<String> values = JsonPath.read(json, query);
                if (values.isEmpty()) {
                    return JsonResult.failure(Messages.error_no_data());
                }
                for (String value : values) {
                    model.add(value, value);
                }
            } catch (Exception e) {
                return JsonResult.failure(e.getMessage());
            }
            return JsonResult.success(model);
        }

        public String loadJson(Item item, String configId) {
            if (item != null) {
                JsonParameterReferenceDefinition definition =
                        Stapler.getCurrentRequest2().findAncestorObject(JsonParameterReferenceDefinition.class);
                Config cfg = ConfigFiles.getByIdOrNull(item, configId);
                if (cfg == null) {
                    throw new IllegalArgumentException(Messages.error_config_id_not_found(configId));
                }
                return cfg.content;
            }
            throw new IllegalStateException(Messages.error_jenkins_item_not_found());
        }
    }
}
