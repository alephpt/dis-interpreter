package dev.alephpt.Dis;

import java.util.List;

abstract class Statement {

  interface Visitor<R> {
    R visitExpressionStatement(Expression statement);
    R visitOrStatement(Or statement);
    R visitWhenStatement(When statement);
    R visitPrintStatement(Print statement);
    R visitBodyStatement(Body statement);
    R visitVariableStatement(Variable statement);
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


  // Or Statement Definition //
  static class Or extends Statement {
    Or(Express condition, Statement orBranch) {
      this.condition = condition;
      this.orBranch = orBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitOrStatement(this);
    }

    final Express condition;
    final Statement orBranch;
  }


  // When Statement Definition //
  static class When extends Statement {
    When(Express condition, Statement thenBranch, List<Statement.Or> orBranches, Statement elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.orBranches = orBranches;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhenStatement(this);
    }

    final Express condition;
    final Statement thenBranch;
    final List<Statement.Or> orBranches;
    final Statement elseBranch;
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
