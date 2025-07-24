/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter.model;

import com.github.cyanbaz.jenkins.plugins.jsonparameter.enumeration.SourceValue;
import java.io.Serial;
import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents the data source definition for a JSON parameter.
 * <p>
 * A source can be either:
 * <ul>
 *     <li>{@code CONFIG} — pointing to a Jenkins config file (global or folder-scoped)</li>
 *     <li>{@code REMOTE} — pointing to an external URL</li>
 * </ul>
 * Depending on the selected {@link SourceValue}, only one of {@code config} or {@code remote} will be used.
 *
 * @author Caner Yanbaz
 */
public class Source implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SourceValue value;
    private final Config config;
    private final Remote remote;

    /**
     * Constructs a new source definition bound via Jenkins' UI.
     *
     * @param value  The type of the source (CONFIG or REMOTE)
     * @param config The folder/global config file source (optional)
     * @param remote The remote URL source (optional)
     */
    @DataBoundConstructor
    public Source(SourceValue value, final Config config, final Remote remote) {
        this.value = value;
        this.config = config;
        this.remote = remote;
    }

    public SourceValue getValue() {
        return value;
    }

    public Config getConfig() {
        return config;
    }

    public Remote getRemote() {
        return remote;
    }
}
