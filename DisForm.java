package dev.alephpt.Dis;

import java.util.List;
import java.util.Map;

class DisForm implements DisCaller{
  final String name;
  private final Map<String, Statement.Variable> members;

  DisForm(String name, Map<String, Statement.Variable> members) {
    this.name = name;
    this.members = members;
  }

  Statement.Variable findMember(String name) {
    if (members.containsKey(name)) {
      return members.get(name);
    }

    return null;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    DisTaste taste = new DisTaste(this);
    return taste;
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
