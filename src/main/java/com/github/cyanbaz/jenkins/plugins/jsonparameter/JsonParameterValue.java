/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

import hudson.model.StringParameterValue;
import java.io.Serial;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a concrete parameter value used by {@link JsonParameterDefinition}.
 * <p>
 * This class extends Jenkins' {@link StringParameterValue} to carry the selected
 * value from a JSON-based parameter input. It supports data binding from the UI
 * or CLI.
 *
 * @author Caner Yanbaz
 */
public class JsonParameterValue extends StringParameterValue {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor used by Jenkins to bind form or CLI input to this value object.
     *
     * @param name  The name of the parameter
     * @param value The actual string value selected or provided
     */
    @DataBoundConstructor
    public JsonParameterValue(String name, String value) {
        super(name, value);
    }
}
