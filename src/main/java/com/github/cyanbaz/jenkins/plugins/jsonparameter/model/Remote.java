/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter.model;

import java.io.Serial;
import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a remote JSON source, defined by a URL.
 * <p>
 * This model is used when the {@code SourceValue} is set to {@code REMOTE},
 * and allows Jenkins to fetch dynamic data from an external endpoint.
 * Typically used in combination with {@link com.github.cyanbaz.jenkins.plugins.jsonparameter.resolver.JsonResolver}.
 *
 * @author Caner Yanbaz
 */
public class Remote implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String url;

    /**
     * Constructor bound by Jenkins when configuring the parameter in the UI.
     *
     * @param url The remote URL to fetch the JSON from
     */
    @DataBoundConstructor
    public Remote(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
