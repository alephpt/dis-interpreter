package dev.alephpt.Dis;

import java.util.List;

class DisOp implements DisCaller {
  private final Statement.Operation declaration;
  private final Field closure;

  DisOp(Statement.Operation declaration, Field closure) {
    this.closure = closure;
    this.declaration = declaration;
  }

  DisOp bind(DisInstance objIns) {
    Field field = new Field(closure);
    field.define("this", objIns);
    return new DisOp(declaration, field);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    Field fields = new Field(closure);
    for(int i = 0; i < declaration.params.size(); i++){
      fields.define(declaration.params.get(i).lexeme, args.get(i));
    }
    
    try {
      interpreter.executeBlock(declaration.body, fields);
    } catch (Return returnValue) {
      return returnValue.value;
    }
    return null;
  }

  @Override
  public int arity() { return declaration.params.size(); }

  @Override
  public String toString() { return "<operation " + declaration.name.lexeme + ">"; }
}
