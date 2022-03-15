package dev.alephpt.Dis;

import java.util.List;

import static dev.alephpt.Dis.TokenType.*;

class Parser {
  private static class ParserError extends RuntimeException  {}
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Express parse() {
    try {
      return expression();
    } catch (ParserError error) {
      return null;
    }
  }

  private Express expression() {
    return equality();
  }

  private Express equality() {
    Express expr = comparison();

    while (match(INEQ, EQEQ)) {
      Token operator = previous();
      Express right = comparison();
      expr = new Express.Binary(expr, operator, right);
    } 
    return expr;
  }

  private Express comparison() {
    Express expr = term();

    while (match(GREATER, GREAT_EQ, LESSER, LESS_EQ)) {
      Token operator = previous();
      Express right = term();
      expr = new Express.Binary(expr, operator, right);
    }
    return expr;
  }

  private Express term() {
    Express expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Express right = factor();
      expr = new Express.Binary(expr, operator, right);
    }
    return expr;
  }

  private Express factor() {
    Express expr = unary();

    while(match(WHACK, STAR)){
      Token operator = previous();
      Express right = unary();
      expr = new Express.Binary(expr, operator, right);
    }
    return expr;
  }

  private Express unary() {
    if(match(NOT, MINUS)){
      Token operator = previous();
      Express right = unary();
      return new Express.Unary(operator, right);
    }
    return primary();
  }

  private Express primary() {
    if (match(FALSE)) { return new Express.Literal(false); }
    if (match(TRUE)) { return new Express.Literal(true); }
    if (match(NONE)) { return new Express.Literal(null); }
    if (match(NUMERAL, STRING)) { return new Express.Literal(previous().literal); }
    if (match(L_PAR)) {
      Express expr = expression();
      consume(R_PAR, "Expecting ')' after expression");
      return new Express.Grouping(expr);
    }

    throw error(peek(), "Found invalid expression. Expected a valid expression.");
  }

  private boolean match(TokenType... types) {
    for (TokenType type: types) {
      if(check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) { return advance(); }
    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) { return false; }
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) { current++; }
    return previous();
  }

  private boolean isAtEnd() { return peek().type == EOF; }
  private Token peek() { return tokens.get(current); }
  private Token previous() { return tokens.get(current - 1); }
  
  private ParserError error(Token token, String message) {
    DisC.error(token, message);
    return new ParserError();
  }

  private void synchronize() {
    advance();
    while (!isAtEnd()) {
      if(previous().type == LINE_END) return;

      switch(peek().type) {
        case CLASS: 
        case DECLARE: 
        case DEFINE: 
        case PRINT: 
        case WHEN: 
        case FOR:
        case WHILE:
        case RETURN:
          return;
      }

      advance();
    }
  }
}
