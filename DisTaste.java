package dev.alephpt.Dis;

import java.util.HashMap;
import java.util.Map;

class DisTaste {
  private DisForm form;
  private final Map<String, Object> fields = new HashMap<>();

  DisTaste(DisForm form) {
    this.form = form;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    Statement.Variable member = form.findMember(name.lexeme);
    if (member != null) {
      return member;
    }

    throw new RuntimeError(name, "Undefined form member '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    if (form.name != null) { return "<" + form.name + " instance>"; } 
    DisC.error(null, "enum name is null");
    return null;
  }

}
