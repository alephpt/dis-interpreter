package dev.alephpt.Dis;

import java.util.List;

abstract class Express {
  interface Visitor<R> {
    R visitBinaryExpress(Binary express);
    R visitGroupingExpress(Grouping express);
    R visitLiteralExpress(Literal express);
    R visitUnaryExpress(Unary express);
  }

  static class Binary extends Express {
    Binary(Express left, Token operator, Express right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpress(this);
    }

    final Express left;
    final Token operator;
    final Express right;
  }

  static class Grouping extends Express {
    Grouping(Express expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpress(this);
    }

    final Express expression;
  }

  static class Literal extends Express {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpress(this);
    }

    final Object value;
  }

  static class Unary extends Express {
    Unary(Token operator, Express right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpress(this);
    }

    final Token operator;
    final Express right;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
