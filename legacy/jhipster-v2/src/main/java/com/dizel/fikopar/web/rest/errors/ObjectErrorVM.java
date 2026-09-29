package com.dizel.fikopar.web.rest.errors;

public class ObjectErrorVM {

    private static final long serialVersionUID = 1L;
    private final String objectName;

    private final String message;

    public ObjectErrorVM(String dto, String message) {
        this.objectName = dto;
        this.message = message;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getMessage() {
        return message;
    }
}
