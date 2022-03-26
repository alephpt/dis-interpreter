package dev.alephpt.Dis;

import java.util.List;

class DisOp implements DisCaller {
  private final Statement.Operation declaration;

  DisOp(Statement.Operation declaration) {
    this.declaration = declaration;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    Field fields = new Field(interpreter.globals);
    for(int i = 0; i < declaration.params.size(); i++){
      fields.define(declaration.params.get(i).lexeme, args.get(i));
    }
    interpreter.executeBlock(declaration.body, fields);
    return null;
  }

  @Override
  public int arity() { return declaration.params.size(); }

//  @Override
//  public int toString() { return "<op " + (String)declaration.name.lexeme + ">"; }
}
