package dev.alephpt.Dis;

import java.util.List;

abstract class Statement {
  interface Visitor<R> {
    R visitExpressionStatement(Expression statement);
    R visitPrintStatement(Print statement);
  }

  static class Expression extends Statement {
    Expression(Express expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStatement(this);
    }

    final Express expression;
  }

  static class Print extends Statement {
    Print(Express expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStatement(this);
    }

    final Express expression;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
