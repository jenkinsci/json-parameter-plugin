/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import hudson.model.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.htmlunit.html.*;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JsonParameterDefinitionTest {

    final String name = "JSON_PARAM";

    @Test
    void given_url_when_configuration_param_return_success() {
        // given
        String defaultValue = "";
        String query = "$[*].name";
        String url = "https://localhost:8080/users.json";
        String credentialsId = "credentialsId";
        JsonSource source = new RemoteSource(url, credentialsId);

        // when
        JsonParameterDefinition parameter = new JsonParameterDefinition(name, defaultValue, source, query);

        // then
        assertEquals(name, parameter.getName());
        assertEquals(defaultValue, parameter.getDefaultValue());
        assertEquals(query, parameter.getQuery());
    }

    @Test
    void given_global_config_id_when_configuration_param_return_success() {
        // given
        String name = "JSON_PARAM";
        String defaultValue = "";
        String query = "$[*].name";
        String id = "12345";
        JsonSource source = new ConfigFileSource(false, null, id);

        // when
        JsonParameterDefinition parameter = new JsonParameterDefinition(name, defaultValue, source, query);

        // then
        assertEquals(name, parameter.getName());
        assertEquals(defaultValue, parameter.getDefaultValue());
        assertEquals(query, parameter.getQuery());
    }

    @Test
    void given_folder_config_id_when_configuration_param_return_success() {
        // given
        String name = "JSON_PARAM";
        String defaultValue = "";
        String query = "$[*].name";
        String path = "path";
        String id = "12345";
        JsonSource source = new ConfigFileSource(true, path, id);

        // when
        JsonParameterDefinition parameter = new JsonParameterDefinition(name, defaultValue, source, query);

        // then
        assertEquals(name, parameter.getName());
        assertEquals(defaultValue, parameter.getDefaultValue());
        assertEquals(query, parameter.getQuery());
    }

    @Test
    void given_valid_url_when_configuration_param_return_success(JenkinsRule jenkins) throws Exception {
        // given
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        String json;
        try (InputStream in = getClass().getResourceAsStream("/users.json")) {
            if (in == null) {
                throw new FileNotFoundException("users.json not found in test resources");
            }
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        server.createContext("/users", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        });
        server.setExecutor(null);
        server.start();

        try {
            int port = server.getAddress().getPort();
            String mockUrl = "http://localhost:" + port + "/users";

            String defaultValue = "";
            String query = "$[*].name";
            String value = "Ervin Howell";
            JsonSource source = new RemoteSource(mockUrl, null);
            JsonParameterDefinition parameter = new JsonParameterDefinition(name, defaultValue, source, query);

            FreeStyleProject project = jenkins.createFreeStyleProject();
            project.addProperty(new ParametersDefinitionProperty(parameter));

            // when
            try (JenkinsRule.WebClient webClient = jenkins.createWebClient()) {
                webClient.setThrowExceptionOnFailingStatusCode(false);
                HtmlPage page = webClient.goTo("job/" + project.getName() + "/build");

                HtmlSelect select = page.getElementByName("value");
                HtmlOption option = select.getOptions().stream()
                        .filter(o -> o.getText().contains(value))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Option '" + value + "' not found"));
                select.setSelectedAttribute(option, true);

                HtmlButton buildButton = page.querySelector(".jenkins-button--primary");
                buildButton.click();

                jenkins.waitUntilNoActivity();

                FreeStyleBuild build = project.getLastBuild();
                jenkins.assertBuildStatusSuccess(build);

                // then
                assertNotNull(build);
                ParametersAction params = build.getAction(ParametersAction.class);
                ParameterValue val = params.getParameter(name);
                assertNotNull(val);
                assertEquals(value, ((StringParameterValue) val).value);
            }
        } finally {
            server.stop(0);
        }
    }
}
