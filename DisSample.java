package dev.alephpt.Dis;

import java.util.HashMap;
import java.util.Map;

class DisSample {
  private DisEnum enumd;
  private final Map<String, Object> fields = new HashMap<>();

  DisSample(DisEnum enumd) {
    this.enumd = enumd;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    if (name.literal instanceof Integer) {
      String elementName = enumd.findElementName((Integer)name.literal);
      if (elementName != null) {
        return elementName;
      }
    }

    Integer element = enumd.findElement(name.lexeme);
    if (element != null) {
      return element;
    }

    throw new RuntimeError(name, "Undefined Enum element '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    if (enumd.name != null) { return "<" + enumd.name + " instance>"; } 
    DisC.error(null, "instance name is null");
    return null;
  }

}
