package dev.alephpt.Dis;

import java.util.List;

interface DisCaller {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}
