package com.craftinginterpreters.lox;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        // Handle a binary node and return the visitor's result.
        R visitBinaryExpr(Binary expr);

        // Handle a grouping node and return the visitor's result.
        R visitGroupingExpr(Grouping expr);

        // Handle a literal node and return the visitor's result.
        R visitLiteralExpr(Literal expr);

        // Handle a unary node and return the visitor's result.
        R visitUnaryExpr(Unary expr);
    }

    // Example: 1 + 2
    static class Binary extends Expr {

        // Store the two operands and operator that make up this binary node.
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expr left;
        final Token operator;
        final Expr right;

        // Dispatch this node to the visitor's binary-expression handler.
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    // Example: (1 + 2)
    static class Grouping extends Expr {

        // Store the expression contained inside the parentheses.
        Grouping(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        // Dispatch this node to the visitor's grouping-expression handler.
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    // Examples: 123, "hello", true, nil
    static class Literal extends Expr {

        // Store the value represented by this literal node.
        Literal(Object value) {
            this.value = value;
        }

        final Object value;

        // Dispatch this node to the visitor's literal-expression handler.
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    // Examples: -123, !true
    static class Unary extends Expr {

        // Store the unary operator and the expression it applies to.
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        final Token operator;
        final Expr right;

        // Dispatch this node to the visitor's unary-expression handler.
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    // Require each expression subtype to dispatch itself to the matching visitor
    // method.
    abstract <R> R accept(Visitor<R> visitor);
}
