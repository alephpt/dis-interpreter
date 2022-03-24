package dev.alephpt.Dis;

import java.util.List;

abstract class Statement {

  interface Visitor<R> {
    R visitPrintStatement(Print statement);
    R visitExpressionStatement(Expression statement);
    R visitBodyStatement(Body statement);
    R visitVariableStatement(Variable statement);
  }


  // Print Statement Definition //
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


  // Expression Statement Definition //
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


  // Body Statement Definition //
  static class Body extends Statement {
    Body(List<Statement> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBodyStatement(this);
    }

    final List<Statement> statements;
  }


  // Variable Statement Definition //
  static class Variable extends Statement {
    Variable(Token name, Express initial) {
      this.name = name;
      this.initial = initial;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableStatement(this);
    }

    final Token name;
    final Express initial;
  }


  abstract <R> R accept(Visitor<R> visitor);
}
