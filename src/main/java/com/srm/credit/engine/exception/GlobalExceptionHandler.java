package com.srm.credit.engine.exception;

import com.srm.credit.engine.cedente.exception.CedenteNotFoundException;
import com.srm.credit.engine.exchange.exception.ExchangeRateNotFoundException;
import com.srm.credit.engine.receivable.exception.ReceivableNotFoundException;
import com.srm.credit.engine.settlement.exception.SettlementNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReceivableNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReceivableNotFound(
            ReceivableNotFoundException exception
    ) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException exception
    ) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(CedenteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCedenteNotFound(
            CedenteNotFoundException exception
    ) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(ExchangeRateNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleExchangeRateNotFound(
            ExchangeRateNotFoundException exception
    ) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(SettlementNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSettlementNotFound(
            SettlementNotFoundException exception
    ) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", exception.getMessage(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "Erro de validação",
                "errors", errors,
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}