package org.xyjh.xyjhstartweb.duduplan.error;

import org.springframework.http.HttpStatus;

public class DuduPlanApiException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public DuduPlanApiException(HttpStatus status, String error) {
        super(error);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
