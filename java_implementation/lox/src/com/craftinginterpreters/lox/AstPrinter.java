package com.craftinginterpreters.lox;

class AstPrinter implements Expr.Visitor<String> {
    // This is the entry point for printing an expression tree.
    // The expression tells us which visitor method to call.
    String print(Expr expr) {
        return expr.accept(this);
    }

    // Binary expressions look like: (left operator right)
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(
                expr.operator.lexeme, expr.left, expr.right);
    }

    // Grouping expressions mean the expression was wrapped in parentheses.
    // Example: (5 + 2) becomes (group (+ 5 2))
    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    // A literal value is printed directly. Null values are shown as nil.
    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    // Unary expressions look like: (operator right)
    // Example: -123 becomes (- 123)
    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    // Builds strings like: (+ 1 2), (- 3), or (group 5)
    // This is the helper that wraps the operator and any child expressions.
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    // Small test program to verify the printer works on a nested expression.
    // The expression built here is roughly: (-123) * (45.67)
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(
                                TokenType.MINUS,
                                "-",
                                null,
                                1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(45.67)));
        System.out.println(new AstPrinter().print(expression));
    }
}
