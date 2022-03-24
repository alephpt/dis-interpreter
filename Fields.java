package dev.alephpt.Dis;

import java.util.HashMap;
import java.util.Map;

class Field {
  final Field foregone;
  private final Map<String, Object> values = new HashMap<>();

  Field() {
    foregone = null;
  }

  Field(Field foregone) {
    this.foregone = foregone;
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) { return values.get(name.lexeme); }

    if (foregone != null) { return foregone.get(name); }

    return values.get(null);
    // to throw an error or return a 'none' type ?
    // throw new RuntimeError(name, "Variable '" + name.lexeme + "' is undefined.");
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (foregone != null) { 
      foregone.assign(name, value); 
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) {
    values.put(name, value);
  }
}
