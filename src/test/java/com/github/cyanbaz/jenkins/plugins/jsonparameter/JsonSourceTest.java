/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import static org.junit.jupiter.api.Assertions.*;

import hudson.util.ListBoxModel;
import org.junit.jupiter.api.Test;

class JsonSourceTest {

    @Test
    void given_valid_json_when_loadOptions_return_success() {
        // given
        String json =
                """
                [
                  {"name": "Alice", "age": 30},
                  {"name": "Bob", "age": 25}
                ]
                """;
        JsonSource source = new JsonSource() {
            @Override
            public String loadJson() {
                return json;
            }
        };
        String query = "$[*].name";

        // when
        JsonResult<ListBoxModel> result = source.loadOptions(query);

        // then
        assertTrue(result.isSuccess());
        assertNotNull(result.getValue());
        assertEquals("Alice", result.getValue().get(0).value);
        assertEquals("Bob", result.getValue().get(1).value);
    }

    @Test
    void given_invalid_json_when_loadOptions_return_success() {
        // given
        JsonSource source = new JsonSource() {
            @Override
            public String loadJson() {
                throw new IllegalArgumentException("Invalid json");
            }
        };
        String query = "$[*].name";

        // when
        JsonResult<ListBoxModel> result = source.loadOptions(query);

        // then
        assertEquals("Invalid json", result.getErrorMessage());
    }
}
