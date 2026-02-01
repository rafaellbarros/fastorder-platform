package br.com.rafaellbarros.order.domain.exception;

public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String msg) {
        super(msg);
    }
}
