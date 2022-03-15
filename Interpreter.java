package dev.alephpt.Dis;

class Interpreter implements Express.Visitor<Object> {
  void interpret(Express expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(asString(value));
    } catch (RuntimeError error) {
      DisC.runtimeError(error);
    }
  }

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
        return -(Double)right;
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
        return (Double)left > (Double)right;
      case GREAT_EQ:
        checkNumberOperands(express.operator, left, right);
        return (Double)left >= (Double)right;
      case LESSER:
        checkNumberOperands(express.operator, left, right);
        return (Double)left < (Double)right;
      case LESS_EQ:
        checkNumberOperands(express.operator, left, right);
        return (Double)left <= (Double)right;
      case MINUS:
        checkNumberOperands(express.operator, left, right);
        return (Double)left - (Double)right;
      case PLUS:
        if (left instanceof Integer && right instanceof Integer) {
              return (Integer)left + (Integer)right;
        }
        if (left instanceof Double && right instanceof Double) {
              return (Double)left + (Double)right;
        }
        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        }
        throw new RuntimeError(express.operator, "Operands must be Numbers or Strings");
      case WHACK:
        checkNumberOperands(express.operator, left, right);
        return  (Double)left / (Double)right;
      case STAR:
        checkNumberOperands(express.operator, left, right);
        return (Double)left * (Double)right;
    }
    
    // unreachable
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

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double || operand instanceof Integer) {
      return;
    }
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if ((left instanceof Integer && right instanceof Integer) || 
        (left instanceof Double && right instanceof Double)) { 
      return; 
    }
    throw new RuntimeError(operator, "Operands must be numbers.");
  }
}
