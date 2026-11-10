package com.craftinginterpreters.lox;

public class Token {
    final TokenType type; // For example, NUMBER or PLUS.
    final String lexeme; // Exact source text, such as "123.4" or "+".
    final Object literal; // Parsed value, such as 123.4; null when there is no value.
    final int line; // Source line where this token was found.

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
