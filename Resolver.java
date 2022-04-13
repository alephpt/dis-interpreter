package dev.alephpt.Dis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Express.Visitor<Void>, Statement.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private OperationType currentOperation = OperationType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  private enum OperationType {
    NONE,
    OPERATION,
    METHOD
  }

  @Override
  public Void visitObjStatement(Statement.Obj object) {
    declare(object.name);

    for (Statement.Operation method : object.methods) {
      resolveOperation(method, OperationType.METHOD);
    }

    return null;
  }

  @Override
  public Void visitEnumStatement(Statement.Enum enumstmnt) {
    declare(enumstmnt.name);
    define(enumstmnt.name);
    return null;
  }

  @Override
  public Void visitFormStatement(Statement.Form form) {
    declare(form.name);

    for (Statement.Variable variable : form.members) {
      declare(variable.name);
      if (variable.initial != null) { resolve(variable.initial); }
      define(variable.name);
    }
    
    return null;
  }

  @Override
  public Void visitGetPropsExpress(Express.GetProps props) {
    resolve(props.object);
    return null;
  }

  @Override
  public Void visitSetPropsExpress(Express.SetProps props) {
    resolve(props.value);
    resolve(props.object);
    return null;
  }

  @Override
  public Void visitBodyStatement(Statement.Body body) {
    beginScope();
    resolve(body.statements);
    endScope();
    return null;
  }

  @Override
  public Void visitOperationStatement(Statement.Operation operation) {
    declare(operation.name);
    define(operation.name);
    resolveOperation(operation, OperationType.OPERATION);
    return null;
  }

  @Override
  public Void visitReturnStatement(Statement.Return returnstmnt) {
    if (currentOperation == OperationType.NONE){
      DisC.error(returnstmnt.keyword, "Cannot return from top-level execution.");
    }

    if (returnstmnt.value != null) { 
      resolve(returnstmnt.value);
    }

    return null;
  }

  @Override
  public Void visitPrintStatement(Statement.Print print) {
    resolve(print.expression);
    return null;
  }

  @Override
  public Void visitWhenStatement(Statement.When when) {
    resolve(when.condition);
    resolve(when.thenBranch);
    for(Statement.Or or : when.orBranches) {
      resolve(or.condition);
      resolve(or.orBranch);
    }
    if (when.elseBranch != null) resolve(when.elseBranch);
    return null;
  }

  @Override
  public Void visitOrStatement(Statement.Or or) {
    resolve(or.condition);
    resolve(or.orBranch);
    return null;
  }

  @Override
  public Void visitWhileStatement(Statement.While whilestmnt) {
    resolve(whilestmnt.condition);
    resolve(whilestmnt.body);
    return null;
  }

  @Override
  public Void visitVariableStatement(Statement.Variable variable) {
    declare(variable.name);
    if (variable.initial != null) { resolve(variable.initial); }
    define(variable.name);
    return null;
  }

  @Override
  public Void visitExpressionStatement(Statement.Expression expressstmnt) {
    resolve(expressstmnt.expression);
    return null;
  }

  @Override
  public Void visitVariableExpress(Express.Variable variable) {
    if(!scopes.isEmpty() && scopes.peek().get(variable.name.lexeme) == Boolean.FALSE) {
      DisC.error(variable.name, "Needs to return 'none'"); 
    }

    resolveLocal(variable, variable.name);
    return null;
  }

  @Override
  public Void visitGlobalVariableExpress(Express.GlobalVariable globally) {
    resolveGlobal(globally, globally.name);
    return null;
  }

  @Override
  public Void visitParentVariableExpress(Express.ParentVariable parental) {
    resolveParent(parental, parental.name);
    return null;
  }

  @Override
  public Void visitAssignExpress(Express.Assign assignment) {
    resolve(assignment.value);
    resolveLocal(assignment, assignment.name);
    return null;
  }

  @Override
  public Void visitBinaryExpress(Express.Binary binary) {
    resolve(binary.left);
    resolve(binary.right);
    return null;
  }

  @Override
  public Void visitGroupingExpress(Express.Grouping parens) {
    resolve(parens.expression);
    return null;
  }

  @Override
  public Void visitCountExpress(Express.Count count) {
    resolve(count.identifier);
    resolveLocal(count, count.name);
    return null;
  }

  @Override
  public Void visitCallingExpress(Express.Calling call) {
    resolve(call.called);
    for (Express argument : call.args) {
      resolve(argument);
    }
    return null;
  }

  @Override
  public Void visitLiteralExpress(Express.Literal literally) {
    return null;
  }

  @Override
  public Void visitLogicalExpress(Express.Logical logical) {
    resolve(logical.left);
    resolve(logical.right);
    return null;
  }

  @Override
  public Void visitUnaryExpress(Express.Unary unary) {
    resolve(unary.right);
    return null;
  }


  /// HELPER FUNCTIONS //

  private void beginScope() { scopes.push(new HashMap<String, Boolean>()); }
  private void endScope() { scopes.pop(); }

  private void resolve(Statement statement) { statement.accept(this); }
  private void resolve(Express express) { express.accept(this); }
  
  void resolve(List<Statement> statements) {
    for (Statement statement : statements) { 
      resolve(statement); 
    }
  }

  private void resolveLocal(Express express, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(express, scopes.size() - 1 - i);
        return;
      }
    }
  }

  private void resolveGlobal(Express express, Token name) {
    if (scopes.get(0).containsKey(name.lexeme)) {
      interpreter.resolve(express, scopes.size() - 1); // -1?
      return;
    }
  }

  private void resolveParent(Express express, Token name) {
    int scopeSize = scopes.size() - 1;
    if (scopes.get(scopeSize).containsKey(name.lexeme)) {
      interpreter.resolve(express, scopeSize); 
      return;
    }
  }

  private void resolveOperation(Statement.Operation operation, OperationType type) {
    OperationType enclosingOperation = currentOperation;
    currentOperation = type;

    beginScope(); // is this what we need?
    for (Token parameter : operation.params) {
      declare(parameter);
      define(parameter);
    } 
    resolve(operation.body);
    endScope();

    currentOperation = enclosingOperation;
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
      DisC.error(name, "This variable already exists within this scope.");
    }

    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

}
