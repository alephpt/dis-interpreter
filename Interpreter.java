package dev.alephpt.Dis;

import java.util.List;

class Interpreter implements Express.Visitor<Object>, Statement.Visitor<Void> {
  void interpret(List<Statement> statements) {
    try {
      for (Statement statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      DisC.runtimeError(error);
    }
  }

/*  void interpret(Express expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(asString(value));
    } catch (RuntimeError error) {
      DisC.runtimeError(error);
    }
  }*/

  @Override
  public Object visitLiteralExpress(Express.Literal express) {
    return express.value;
  }

  @Override
  public Object visitGroupingExpress(Express.Grouping express) {
    return evaluate(express.expression);
  }

  @Override
  public Object visitUnaryExpress(Express.Unary express) {
    Object right = evaluate(express.right);

    switch(express.operator.type) {
      case NOT:
        return !isTruthful(right);
      case MINUS:
        checkNumberOperand(express.operator, right);
        if(right instanceof Double) { return -((Number)right).doubleValue(); }
        if(right instanceof Integer) { return -(Integer)right; }
    }

    // unreachable
    return null;
  }
  
  @Override
  public Object visitBinaryExpress(Express.Binary express) {
    Object left = evaluate(express.left);
    Object right = evaluate(express.right);

    switch(express.operator.type){
      case INEQ:
        return !isEqual(left, right);
      case EQEQ:
        return isEqual(left, right);
      case GREATER:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left > (Integer)right; 
        }        
        return ((Number)left).doubleValue() > ((Number)right).doubleValue(); 
      case GREAT_EQ:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left >= (Integer)right; 
        }
        return ((Number)left).doubleValue() >= ((Number)right).doubleValue(); 
      case LESSER:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left < (Integer)right; 
        }        
        return ((Number)left).doubleValue() < ((Number)right).doubleValue(); 
      case LESS_EQ:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left <= (Integer)right; 
        } 
        return ((Number)left).doubleValue() <= ((Number)right).doubleValue(); 
      case MINUS:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left - (Integer)right; 
        } 
        return ((Number)left).doubleValue() - ((Number)right).doubleValue(); 
      case PLUS:
        if (left instanceof Integer && right instanceof Integer) {
              return (Integer)left + (Integer)right;
        }
        if (left instanceof Double || right instanceof Double ||
            left instanceof Integer || right instanceof Integer) {
              return ((Number)left).doubleValue() + ((Number)right).doubleValue();
        }
        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        }
        throw new RuntimeError(express.operator, "Operands must be of Numbers or Strings and be matching types.");
      case WHACK:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left / (Integer)right; 
        }
        return ((Number)left).doubleValue() / ((Number)right).doubleValue(); 
      case STAR:
        checkNumberOperands(express.operator, left, right);
        if(left instanceof Integer && right instanceof Integer) {
          return (Integer)left * (Integer)right; 
        }
        return ((Number)left).doubleValue() * ((Number)right).doubleValue(); 
    }
    
    // unreachable
    return null;
  }

  @Override
  public Void visitExpressionStatement(Statement.Expression statement) {
    evaluate(statement.expression);
    return null;
  }

  @Override
  public Void visitPrintStatement(Statement.Print statement) {
    Object value = evaluate(statement.expression);
    System.out.println(asString(value));
    return null;
  }

  private boolean isTruthful(Object object) {
    if (object == null /*|| object.length < 1*/) { return false; }
    if (object instanceof Boolean) { return (boolean)object; }
    return true;
  }

  private boolean isEqual(Object a, Object b){
    if (a == null && b == null) { return true; }
    if (a == null) { return false; }
    return a.equals(b);
  }

  private String asString(Object object) {
    if (object == null) { return "none"; }

    if (object instanceof Double || object instanceof Integer) {
      String text = object.toString();
      if (text.endsWith(".0")) { 
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  private Object evaluate(Express express) {
    return express.accept(this);
  }

  private void execute(Statement statement) {
    statement.accept(this);
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double || operand instanceof Integer) {
      return;
    }
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if ((left instanceof Integer || left instanceof Double) && 
        (right instanceof Integer || right instanceof Double)) { 
      return; 
    }
    throw new RuntimeError(operator, "Operands must be numbers.");
  }
}
