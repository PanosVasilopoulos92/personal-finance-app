package org.viators.personal_finance_app.exceptions;

public class NotAnAdminException extends RuntimeException {
    public NotAnAdminException(String message) {
        super(message);
    }
}
