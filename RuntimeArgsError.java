package dev.alephpt.Dis;

import java.util.List;

class RuntimeArgsError extends RuntimeException {
  final List<Object> args;

  RuntimeArgsError(List<Object> args, String message) {
    super(message);
    this.args = args;
  }
}
