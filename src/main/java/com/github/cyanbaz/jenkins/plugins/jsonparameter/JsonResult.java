/**
 * Copyright (c) 2025 Caner Yanbaz
 * Licensed under the MIT License (see LICENSE file).
 */
package com.github.cyanbaz.jenkins.plugins.jsonparameter;

/**
 * A generic result container used to wrap the outcome of loading JSON data.
 * Includes a success flag, the result value (if successful), and an optional error message.
 *
 * @param <T> The type of the value.
 * @author Caner Yanbaz
 */
public class JsonResult<T> {

    private final boolean success;
    private final T value;
    private final String errorMessage;

    public JsonResult(boolean success, T value, String errorMessage) {
        this.success = success;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static <T> JsonResult<T> success(T value) {
        return new JsonResult<>(true, value, null);
    }

    public static <T> JsonResult<T> failure(String errorMessage) {
        return new JsonResult<>(false, null, errorMessage);
    }
}
