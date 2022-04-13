package dev.alephpt.Dis;

import java.util.HashMap;
import java.util.Map;

class DisInstance {
  private DisObj obj;
  private final Map<String, Object> fields = new HashMap<>();

  DisInstance(DisObj obj) {
    this.obj = obj;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    DisOp method = obj.findMethod(name.lexeme);
    if (method != null) {
      return method;
    }

    throw new RuntimeError(name, "Undefined object property '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    if (obj.name != null) { return "<" + obj.name + " instance>"; } 
    DisC.error(null, "instance name is null");
    return null;
  }

}
