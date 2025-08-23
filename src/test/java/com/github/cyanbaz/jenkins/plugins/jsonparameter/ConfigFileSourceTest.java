/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import static org.junit.jupiter.api.Assertions.*;

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AccessDeniedException3;
import hudson.util.ListBoxModel;
import java.util.Collection;
import org.jenkinsci.lib.configprovider.model.Config;
import org.jenkinsci.plugins.configfiles.GlobalConfigFiles;
import org.jenkinsci.plugins.configfiles.json.JsonConfig;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ConfigFileSourceTest {

    @Test
    void given_config_id_when_instantiate_ConfigFileSource_return_success() {
        // given
        String configId = "json-config";

        // when
        ConfigFileSource configFileSource = new ConfigFileSource(configId);

        // then
        assertEquals(configId, configFileSource.getConfigId());
    }

    @Test
    void given_config_file_and_user_with_configure_permission_when_doFillConfigIdItems_return_list_of_config_files(
            JenkinsRule jenkinsRule) throws Exception {
        // given
        String configId = "json-config-id";
        String configName = "json-config-name";

        GlobalConfigFiles store = jenkinsRule
                .getInstance()
                .getExtensionList(GlobalConfigFiles.class)
                .get(GlobalConfigFiles.class);
        assertNotNull(store);
        assertTrue(store.getConfigs().isEmpty());

        JsonConfig config = new JsonConfig(configId, configName, "comment", "content");
        store.save(config);

        Collection<Config> configs = store.getConfigs();
        assertEquals(1, configs.size());

        FreeStyleProject project = jenkinsRule.createFreeStyleProject("job");

        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();

        strategy.grant(Item.CONFIGURE).everywhere().to("configurer");

        jenkinsRule.jenkins.setAuthorizationStrategy(strategy);

        User user = User.getById("configurer", true);
        assertNotNull(user);

        try (ACLContext ignored = ACL.as2(user.impersonate2())) {
            // when
            ConfigFileSource.DescriptorImpl descriptor =
                    jenkinsRule.jenkins.getDescriptorByType(ConfigFileSource.DescriptorImpl.class);

            ListBoxModel items = descriptor.doFillConfigIdItems(project);

            // then
            assertFalse(items.isEmpty());
            assertEquals(2, items.size());
            assertEquals(configId, items.get(1).value);
            assertEquals(configName + " (ID: " + configId + ")", items.get(1).name);
        }
    }

    @Test
    void given_config_file_and_user_with_read_permission_when_doFillConfigIdItems_throws_exception(
            JenkinsRule jenkinsRule) throws Exception {
        // given
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("job");

        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();

        strategy.grant(Item.READ).everywhere().to("reader");

        jenkinsRule.jenkins.setAuthorizationStrategy(strategy);

        User user = User.getById("reader", true);
        assertNotNull(user);

        try (ACLContext ignored = ACL.as2(user.impersonate2())) {
            // when
            ConfigFileSource.DescriptorImpl descriptor =
                    jenkinsRule.jenkins.getDescriptorByType(ConfigFileSource.DescriptorImpl.class);

            // then
            assertThrows(AccessDeniedException3.class, () -> descriptor.doFillConfigIdItems(project));
        }
    }

    @Test
    void given_no_config_files_when_doFillConfigIdItems_return_no_config_files(JenkinsRule jenkinsRule) {
        // given
        ConfigFileSource.DescriptorImpl descriptor =
                jenkinsRule.jenkins.getDescriptorByType(ConfigFileSource.DescriptorImpl.class);

        // when
        ListBoxModel items = descriptor.doFillConfigIdItems(null);

        // then
        assertEquals(1, items.size());
    }
}
