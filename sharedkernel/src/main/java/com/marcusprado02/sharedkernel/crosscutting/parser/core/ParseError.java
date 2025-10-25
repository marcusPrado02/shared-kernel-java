package com.marcusprado02.sharedkernel.crosscutting.parser.core;

// core/ParseError.java
public final class ParseError {
    private final String message;
    private final int position;        // -1 se não aplicável
    private final String snippet;      // trecho (redacted se sensível)
    private final Throwable cause;     // opcional
    private final String hint;         // dica de correção

    private ParseError(String message, int position, String snippet, Throwable cause, String hint) {
        this.message = message; this.position = position; this.snippet = snippet; this.cause = cause; this.hint = hint;
    }
    public static ParseError of(String msg, int pos, String snippet, String hint, Throwable cause) {
        return new ParseError(msg, pos, snippet, cause, hint);
    }
    public static ParseError simple(String msg) { return new ParseError(msg, -1, null, null, null); }

    // getters …
    public String message() { return message; }
    public int position() { return position; }
    public String snippet() { return snippet; }
    public Throwable cause() { return cause; }
    public String hint() { return hint; }
}
