package dev.alephpt.Dis;

import java.util.List;
import java.util.Map;

class DisObj implements DisCaller{
  final String name;
  private final Map<String, DisOp> methods;

  DisObj(String name, Map<String, DisOp> methods) {
    this.name = name;
    this.methods = methods;
  }

  DisOp findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    return null;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    DisInstance instance = new DisInstance(this);
    return instance;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public String toString() {
    return name;
  }
}
