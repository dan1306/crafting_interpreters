package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {

    // Used to stop parsing after reporting a syntax error.
    private static class ParseError extends RuntimeException {
    };

    // The tokens being parsed and the index of the next token to inspect.
    private final List<Token> tokens;
    private int current = 0;

    // Keep the scanner's token list so the parsing methods can read it.
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Start parsing an expression. A syntax error is reported and returns null.
    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    // Expressions start at the lowest-precedence rule: equality.
    private Expr expression() {
        return equality();
    }

    // Parse == and !=, whose operands are comparison expressions.
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse <, <=, >, and >=, whose operands are terms.
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse + and -, whose operands are factors.
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse * and /, whose operands are unary expressions.
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Parse prefix ! and -; calling unary again allows chains like !!true.
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // Parse literals and parenthesized expressions, the basic expressions.
    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // If the next token is one of these types, consume it and report success.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // Require a specific next token; report an error if it is missing.
    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    // Check the next token's type without consuming it.
    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    // Return the current token and move forward, unless it is EOF.
    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    // Tell whether the next token marks the end of the token list.
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // Look at the next token without moving the current position.
    private Token peek() {
        return tokens.get(current);
    }

    // Return the token most recently consumed by advance().
    private Token previous() {
        return tokens.get(current - 1);
    }

    // Report a syntax error and create the signal that stops the current parse.
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // Skip tokens after an error until a likely statement boundary is reached.
    // This recovery helper is for statement parsing, which is not implemented yet.
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
