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

  Object globalGet(Token name) {
    while (foregone != null) { return foregone.globalGet(name); }

    if (values.containsKey(name.lexeme)) { return values.get(name.lexeme); }

    throw new RuntimeError(name, "Global Variable '" + name.lexeme + "' is undefined.");
  }
  
  void globalAssign(Token name, Object value) {
    if (foregone != null) { throw new RuntimeError(name, "Invalid Assignment of '" + value + "', not in the Global Scope."); }

    throw new RuntimeError(name, "Global Assignment for '" + value + "' is not implemented. Contact the developer.");
  }

  Object parentGet(Token name) {
    if (foregone.values.containsKey(name.lexeme)) { return foregone.values.get(name.lexeme); }

    throw new RuntimeError(name, "Parent Variable '" + name.lexeme + "' is undefined.");
  }

  void parentAssign(Token name, Object value) {
    throw new RuntimeError(name, "Global Assignment for '" + value + "' is not implemented. Contact the developer.");
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
