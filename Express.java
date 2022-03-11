package dev.alephpt.Dis;

import java.util.List;

abstract class Express {

  static class Binary extends Express {
    Binary(Express left, Token Operator, Express right) {
      this.left = left;
      this.Operator = Operator;
      this.right = right;
    }

    final Express left;
    final Token Operator;
    final Express right;
  }

  static class Grouping extends Express {
    Grouping(Express expression) {
      this.expression = expression;
    }

    final Express expression;
  }

  static class Literal extends Express {
    Literal(Object value) {
      this.value = value;
    }

    final Object value;
  }

  static class Unary extends Express {
    Unary(Token operator, Express right) {
      this.operator = operator;
      this.right = right;
    }

    final Token operator;
    final Express right;
  }
}
