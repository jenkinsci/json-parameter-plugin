package com.github.cyanbaz.jenkins.plugins.jsonparameter;

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
