/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.ListBoxModel;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RemoteSourceTest {

    @Test
    void given_valid_url_when_instantiate_RemoteSource_return_success() {
        // given
        String url = "https://localhost:8080/users.json";
        String credentialsId = "credentialsId";

        // when
        RemoteSource source = new RemoteSource(url, credentialsId);

        // then
        assertEquals(url, source.getUrl());
    }

    @Test
    void given_valid_credentialsId_when_instantiate_RemoteSource_return_success() {
        // given
        String url = "https://localhost:8080/users.json";
        String credentialsId = "credentialsId";

        // when
        RemoteSource source = new RemoteSource(url, credentialsId);

        // then
        assertEquals(credentialsId, source.getCredentialsId());
    }

    @Test
    void
            given_valid_credentials_and_user_with_configure_permission_when_doFillCredentialsIdItems_return_list_of_credentialsId(
                    JenkinsRule jenkinsRule) throws IOException, Descriptor.FormException {
        // given
        String credentialsId = "credentials-id";

        SystemCredentialsProvider.getInstance()
                .getCredentials()
                .add(new UsernamePasswordCredentialsImpl(
                        CredentialsScope.GLOBAL, credentialsId, "Description", "user", "password"));

        FreeStyleProject project = jenkinsRule.createFreeStyleProject("job");

        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();

        strategy.grant(Item.CONFIGURE).everywhere().to("configurer");

        jenkinsRule.jenkins.setAuthorizationStrategy(strategy);

        User user = User.getById("configurer", true);
        assertNotNull(user);

        try (ACLContext ignored = ACL.as2(user.impersonate2())) {
            // when
            RemoteSource.DescriptorImpl descriptor =
                    jenkinsRule.jenkins.getDescriptorByType(RemoteSource.DescriptorImpl.class);
            ListBoxModel items = descriptor.doFillCredentialsIdItems(project);

            // then
            assertFalse(items.isEmpty());
            assertEquals(2, items.size());
            assertEquals(credentialsId, items.get(1).value);
            assertEquals("user/****** (Description)", items.get(1).name);
        }
    }

    @Test
    void given_valid_credentials_and_user_with_read_permission_when_doFillCredentialsIdItems_return_empty_list(
            JenkinsRule jenkinsRule) throws IOException {
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
            RemoteSource.DescriptorImpl descriptor =
                    jenkinsRule.jenkins.getDescriptorByType(RemoteSource.DescriptorImpl.class);
            ListBoxModel items = descriptor.doFillCredentialsIdItems(project);

            // then
            assertTrue(items.isEmpty());
        }
    }
}
