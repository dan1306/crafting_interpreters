package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Map reserved words to their token types so they are not treated as
    // identifiers.
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    // Save the source text that this scanner will process.
    Scanner(String source) {
        this.source = source;
    }

    // Scan each lexeme, append the end-of-file token, and return the collected
    // tokens.
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // Consume a number's digits and optional fractional part, then store its
    // numeric value.
    private void number() {
        while (isDigit(peek()))
            advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance();

            while (isDigit(peek()))
                advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // Consume one character and decide whether to add a token, skip it, or scan a
    // larger lexeme.
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            // These operators may include a following '=' to form a two-character token.
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(SLASH);
                }
                break;
            // Ignore horizontal whitespace; newlines are counted separately.
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            // Numbers and identifiers continue across multiple characters; delegate to
            // helpers.
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    // Consume an identifier, then replace its type if its text is a reserved
    // keyword.
    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }

    // Read through the closing quote, count embedded newlines, and store the
    // unquoted value.
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();
        // Trim surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // Consume the next character only when it matches the expected character.
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    // Return the current character without consuming it, or a sentinel at
    // end-of-input.
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt((current));
    }

    // Return the character after the current one without moving the scanner cursor.
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    // Check whether a character can start or continue an identifier.
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // Check whether a character can continue an identifier after its first
    // character.
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // Check whether a character is an ASCII decimal digit.
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Check whether the cursor has reached the end of the source.
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Return the current character and move the cursor forward by one.
    private char advance() {
        return source.charAt(current++);
    }

    // Add a token that does not carry a separate literal value.
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Capture the current source slice and append it with its type and optional
    // value.
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
