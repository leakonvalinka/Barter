package at.ac.ase.inso.group02.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorResponse {
    private final String message;
    private final Map<String, String> additionalInfo;

    public ErrorResponse(String message) {
        this.message = message;
        additionalInfo = Map.of();
    }

    public ErrorResponse(String message, Map<String, String> additionalInfo) {
        this.message = message;
        this.additionalInfo = additionalInfo;
    }
}
