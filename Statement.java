package dev.alephpt.Dis;

import java.util.List;

abstract class Statement {

  interface Visitor<R> {
    R visitExpressionStatement(Expression statement);
    R visitOperationStatement(Operation statement);
    R visitOrStatement(Or statement);
    R visitWhenStatement(When statement);
    R visitWhileStatement(While statement);
    R visitPrintStatement(Print statement);
    R visitReturnStatement(Return statement);
    R visitBodyStatement(Body statement);
    R visitObjStatement(Obj statement);
    R visitEnumStatement(Enum statement);
    R visitFormStatement(Form statement);
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


  // Operation Statement Definition //
  static class Operation extends Statement {
    Operation(Token name, List<Token> params, List<Statement> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitOperationStatement(this);
    }

    final Token name;
    final List<Token> params;
    final List<Statement> body;
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


  // While Statement Definition //
  static class While extends Statement {
    While(Express condition, Statement body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStatement(this);
    }

    final Express condition;
    final Statement body;
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


  // Return Statement Definition //
  static class Return extends Statement {
    Return(Token keyword, Express value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStatement(this);
    }

    final Token keyword;
    final Express value;
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


  // Obj Statement Definition //
  static class Obj extends Statement {
    Obj(Token name, List<Statement> body) {
      this.name = name;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitObjStatement(this);
    }

    final Token name;
    final List<Statement> body;
  }


  // Enum Statement Definition //
  static class Enum extends Statement {
    Enum(Token name, List<Express.Variable> elements) {
      this.name = name;
      this.elements = elements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitEnumStatement(this);
    }

    final Token name;
    final List<Express.Variable> elements;
  }


  // Form Statement Definition //
  static class Form extends Statement {
    Form(Token name, List<Statement.Variable> members) {
      this.name = name;
      this.members = members;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFormStatement(this);
    }

    final Token name;
    final List<Statement.Variable> members;
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
