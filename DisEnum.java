package dev.alephpt.Dis;

import java.util.List;
import java.util.Map;

class DisEnum implements DisCaller{
  final String name;
  private final Map<String, Express.Variable> elements;

  DisEnum(String name, Map<String, Express.Variable> elements) {
    this.name = name;
    this.elements = elements;
  }

  Express.Variable findElement(String name) {
    if (elements.containsKey(name)) {
      return elements.get(name);
    }

    return null;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    DisSample sample = new DisSample(this);
    return sample;
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
