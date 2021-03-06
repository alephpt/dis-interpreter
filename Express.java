package dev.alephpt.Dis;

import java.util.List;

abstract class Express {

  interface Visitor<R> {
    R visitAssignExpress(Assign express);
    R visitCountExpress(Count express);
    R visitUnaryExpress(Unary express);
    R visitBinaryExpress(Binary express);
    R visitCallingExpress(Calling express);
    R visitSelfExpress(Self express);
    R visitGetPropsExpress(GetProps express);
    R visitSetPropsExpress(SetProps express);
    R visitGroupingExpress(Grouping express);
    R visitLiteralExpress(Literal express);
    R visitLogicalExpress(Logical express);
    R visitVariableExpress(Variable express);
    R visitParentVariableExpress(ParentVariable express);
    R visitGlobalVariableExpress(GlobalVariable express);
  }


  // Assign Express Definition //
  static class Assign extends Express {
    Assign(Token name, Express value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpress(this);
    }

    final Token name;
    final Express value;
  }


  // Count Express Definition //
  static class Count extends Express {
    Count(Token operator, Express identifier, Token name) {
      this.operator = operator;
      this.identifier = identifier;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCountExpress(this);
    }

    final Token operator;
    final Express identifier;
    final Token name;
  }


  // Unary Express Definition //
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


  // Binary Express Definition //
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


  // Calling Express Definition //
  static class Calling extends Express {
    Calling(Express called, List<Express> args) {
      this.called = called;
      this.args = args;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallingExpress(this);
    }

    final Express called;
    final List<Express> args;
  }


  // Self Express Definition //
  static class Self extends Express {
    Self(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSelfExpress(this);
    }

    final Token keyword;
  }


  // GetProps Express Definition //
  static class GetProps extends Express {
    GetProps(Express object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetPropsExpress(this);
    }

    final Express object;
    final Token name;
  }


  // SetProps Express Definition //
  static class SetProps extends Express {
    SetProps(Express object, Token locale, Token name, Express value) {
      this.object = object;
      this.locale = locale;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetPropsExpress(this);
    }

    final Express object;
    final Token locale;
    final Token name;
    final Express value;
  }


  // Grouping Express Definition //
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


  // Literal Express Definition //
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


  // Logical Express Definition //
  static class Logical extends Express {
    Logical(Express left, Token operator, Express right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpress(this);
    }

    final Express left;
    final Token operator;
    final Express right;
  }


  // Variable Express Definition //
  static class Variable extends Express {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpress(this);
    }

    final Token name;
  }


  // ParentVariable Express Definition //
  static class ParentVariable extends Express {
    ParentVariable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitParentVariableExpress(this);
    }

    final Token name;
  }


  // GlobalVariable Express Definition //
  static class GlobalVariable extends Express {
    GlobalVariable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGlobalVariableExpress(this);
    }

    final Token name;
  }


  abstract <R> R accept(Visitor<R> visitor);
}
