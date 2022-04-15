package dev.alephpt.Dis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class Interpreter implements Express.Visitor<Object>, Statement.Visitor<Void> {
  final Field globals = new Field();
  private Field fields = globals;
  private final Map<Express, Integer> relatives = new HashMap<>();

  Interpreter() {
    globals.define("clock", new DisCaller() {
      @Override
      public int arity() { return 0; }

      @Override
      public Object call(Interpreter interpret,
                         List<Object> args) {
        return (double)System.currentTimeMillis();
      }

      @Override
      public String toString() { return "<Native Operation>"; }
    });
  }

  void interpret(List<Statement> statements) {
    try {
      for (Statement statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      DisC.runtimeError(error);
    } catch (RuntimeArgsError error) {
      DisC.runtimeArgsError(error);
    }
  }

  @Override
  public Object visitLiteralExpress(Express.Literal express) {
    return express.value;
  }

  @Override
  public Object visitLogicalExpress(Express.Logical express) {
    Object left = evaluate(express.left);

    if (express.operator.type == TokenType.OR_OP) {
      if (isTruthful(left)) {
        return left;
      }
    } else {
      if (!isTruthful(left)) {
        return left;
      }
    }

    return evaluate(express.right);
  }

  @Override
  public Object visitGroupingExpress(Express.Grouping express) {
    return evaluate(express.expression);
  }

  @Override
  public Object visitCallingExpress(Express.Calling express) {
    Object called = evaluate(express.called);
    List<Object> args = new ArrayList<>();
    for (Express argument : express.args) {
      args.add(evaluate(argument));
    }

    if (!(called instanceof DisCaller)) {
      throw new RuntimeArgsError(args, "Only Classes and Functions are Callable.");
    }

    DisCaller operation = (DisCaller)called;
  
    if(args.size() != operation.arity()) {
      throw new RuntimeArgsError(args, "Expected " + operation.arity() + " arguments.");
    }

    return operation.call(this, args);
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
  public Object visitCountExpress(Express.Count express) {
    Object identity = evaluate(express.identifier);
    Number value = 0;

    switch(express.operator.type){
      case PLUSPLUS:
        if(identity instanceof Integer) {
          value = (Integer)identity + 1;
          break;
        } else 
        if (identity instanceof Double) {
          value = (Double)identity + 1.0;
          break;
        } else {
          throw new RuntimeError(express.operator, "must only be used with Numbers.");
        }
      case MINUSMINUS:
        if(identity instanceof Integer) {
          value = (Integer)identity - 1;
          break;
        } else 
        if (identity instanceof Double) {
          value = (Double)identity - 1.0;
          break;       
        } else {
          throw new RuntimeError(express.operator, "must only be used with Numbers.");
        }
    }
    fields.assign(express.name, value);
    return value;
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
        if (left instanceof String || right instanceof String) {
          return asString(left) + asString(right);
        }
        if (left instanceof Double || right instanceof Double ||
            left instanceof Integer || right instanceof Integer) {
              return ((Number)left).doubleValue() + ((Number)right).doubleValue();
        }
        throw new RuntimeError(express.operator, "Operands must be of Numbers or Strings.");
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
  public Object visitVariableExpress(Express.Variable express) {
    return findVar(express.name, express);
  }

  @Override
  public Object visitParentVariableExpress(Express.ParentVariable express) {
    return fields.parentGet(express.name);
  }

  @Override
  public Object visitGlobalVariableExpress(Express.GlobalVariable express) {
    return fields.globalGet(express.name);
    // globals.get(express.name);
  }

  @Override
  public Object visitAssignExpress(Express.Assign express) {
    Object value = evaluate(express.value);
    Integer distance = relatives.get(express);

    if (distance != null) {
      fields.assignAt(distance, express.name, value);
    } else {
      globals.assign(express.name, value);
    }

    return value;
  }

  @Override
  public Object visitGetPropsExpress(Express.GetProps props) {
    Object object = evaluate(props.object);
    if (object instanceof DisInstance) {
      return ((DisInstance)object).get(props.name);
    }
    if (object instanceof DisSample) {
      return ((DisSample)object).get(props.name);
    }
    if (object instanceof DisTaste) {
      return ((DisTaste)object).get(props.name);
    }

    throw new RuntimeError(props.name, "Property '" + props.name + "' unavailable. Make sure you are using a proper instance.");
  }

  @Override
  public Object visitSetPropsExpress(Express.SetProps props) {
    Object object = evaluate(props.object);
    
    if (!(object instanceof DisInstance) &&
        !(object instanceof DisSample) &&
        !(object instanceof DisTaste)) {
      throw new RuntimeError(props.name, "Only instances of Object, Enumeration, or Forms contain indexable fields");
    }

    Object value = evaluate(props.value);

    if (object instanceof DisInstance) {
      ((DisInstance)object).set(props.name, value);
    }
    if (object instanceof DisSample) {
      ((DisSample)object).set(props.name, value);
    }
    if (object instanceof DisTaste) {
      ((DisTaste)object).set(props.name, value);
    }

    return value;
  }

  @Override
  public Object visitSelfExpress(Express.Self self) {
    return findVar(self.keyword, self);
  }

  @Override
  public Void visitExpressionStatement(Statement.Expression statement) {
    evaluate(statement.expression);
    return null;
  }

  @Override 
  public Void visitOperationStatement(Statement.Operation statement) {
    DisOp operation = new DisOp(statement, fields, false);
    fields.define(statement.name.lexeme, operation);

    return null;
  }

  @Override
  public Void visitWhenStatement(Statement.When statement) {
    boolean met = false;
    if (isTruthful(evaluate(statement.condition))) {
      met = true;
      execute(statement.thenBranch);
    } else {
      for (Statement.Or orStatement : statement.orBranches) {
        if (isTruthful(evaluate(orStatement.condition)) && !met) {
          met = true;
          execute(orStatement.orBranch);
        }
      }
    }
    if (statement.elseBranch != null && !met) {
      execute(statement.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitOrStatement(Statement.Or statement) {
    if (isTruthful(evaluate(statement.condition))) {
      execute(statement.orBranch);
    }
    return null;
  }

  @Override
  public Void visitWhileStatement(Statement.While statement) {
    while (isTruthful(evaluate(statement.condition))) { execute(statement.body); }
    return null;
  }

  @Override
  public Void visitPrintStatement(Statement.Print statement) {
    Object value = evaluate(statement.expression);
    System.out.println(asString(value));
    return null;
  }
  
  @Override
  public Void visitReturnStatement(Statement.Return statement) {
    Object value = null;
    if(statement.value != null) { value = evaluate(statement.value); } 
    throw new Return(value);
  }

  @Override
  public Void visitObjStatement(Statement.Obj object) {
    fields.define(object.name.lexeme, null);

    Map<String, DisOp> methods = new HashMap<>();
    for (Statement.Operation method : object.methods) {
      DisOp op = new DisOp(method, fields, method.name.lexeme.equals("init"));
      methods.put(method.name.lexeme, op);
    }

    DisObj obj = new DisObj(object.name.lexeme, methods);
    fields.assign(object.name, obj);
    return null;
  }

  @Override
  public Void visitEnumStatement(Statement.Enum enumstmnt) {
    fields.define(enumstmnt.name.lexeme, null);
    Integer i = 0;

    Map<String, Integer> elements = new HashMap<>();
    for (Express.Variable element : enumstmnt.elements) {
      elements.put(element.name.lexeme, i);
      i++;
    }

    DisEnum enums = new DisEnum(enumstmnt.name.lexeme, elements);
    fields.assign(enumstmnt.name, enums);
    return null;
  }

  @Override
  public Void visitFormStatement(Statement.Form form) {
    fields.define(form.name.lexeme, null);

    Map<String, Object> members = new HashMap<>();
    for (Statement.Variable member : form.members) {
      Object value = evaluate(member.initial);
      members.put(member.name.lexeme, value);
    }

    DisForm formt = new DisForm(form.name.lexeme, members);
    fields.assign(form.name, formt);
    return null;
  }

  @Override
  public Void visitBodyStatement(Statement.Body statement) {
    executeBlock(statement.statements, new Field(fields));
    return null;
  }

  @Override
  public Void visitVariableStatement(Statement.Variable statement) {
    Object value = null;
    if (statement.initial != null ) { value = evaluate(statement.initial); }

    fields.define(statement.name.lexeme, value);
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

  void resolve(Express express, int depth) {
    relatives.put(express, depth);
  }

  private Object evaluate(Express express) {
    return express.accept(this);
  }

  private void execute(Statement statement) {
    statement.accept(this);
  }

  private Object findVar(Token name, Express express) {
    Integer distance = relatives.get(express);

    if(distance != null){
      return fields.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }

  void executeBlock(List<Statement> statements, Field field) {
    Field previous = this.fields;

    try {
      this.fields = field;
      for (Statement statement : statements) { execute(statement); }
    } finally { this.fields = previous; } 
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
