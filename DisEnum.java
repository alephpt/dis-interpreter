package dev.alephpt.Dis;

import java.util.List;
import java.util.Map;

class DisEnum implements DisCaller{
  final String name;
  private final Map<String, Integer> elements; // need to turn this into an array

  DisEnum(String name, Map<String, Integer> elements) {
    this.name = name;
    this.elements = elements;
  }

  Integer findElement(String name) {
    if (elements.containsKey(name)) {
      return elements.get(name);
    }
    return null;
  }

  String findElementName(Integer el) {
    for(Map.Entry<String, Integer> element : elements.entrySet()) {
      if(element.getValue() == el) {
        return element.getKey();
      }
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
