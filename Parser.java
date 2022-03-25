package dev.alephpt.Dis;

import java.util.ArrayList;
import java.util.List;

import static dev.alephpt.Dis.TokenType.*;

class Parser {
  private static class ParserError extends RuntimeException  {}
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) { 
    this.tokens = tokens;
  }

  List<Statement> parse() {
    List<Statement> statements = new ArrayList<>();
    while (!isAtEnd()) { statements.add(definition()); }

    return statements;
  }

/*  Express parse() {
    try {
      return expression();
    } catch (ParserError error) {
      return null;
    }
  }
  */

  private Express expression() { return assignment(); }

  private Statement definition() {
    try {
      if (match(DEFINE)) { return varDefinition(); }
      return statement();
    } catch (ParserError error) {
      synchronize();
      return null;
    }
  }

  private Statement statement() {
    if (match(WHEN)) { return whenStatement(); }
    if (match(PRINT)) { return printStatement(); }
    if (match(BODY_START)) { return new Statement.Body(block()); }

    return expressionStatement();
  }

  private Statement whenStatement() {
    consume(COMMA, "',' expected after 'when'.");
    Express condition = expression();
    consume(COLON, "closing ':' expected after when conditional.");

    Statement thenBranch = statement();

    List<Statement.Or> orBranches = new ArrayList<>();
    Statement elseBranch = null;
    
    while (match(OR) && !isAtEnd()) {
      orBranches.add(orStatement());
    }

    if (match(ELSE)) {
      consume(COLON, "':' expected after 'else'.");
      elseBranch = statement();
    }

    return new Statement.When(condition, thenBranch, orBranches, elseBranch);
  }

  private Statement.Or orStatement() {
    consume(COMMA, "',' expected after 'or'.");
    Express condition = expression();
    consume(COLON, "closing ':' expected after or conditional.");

    Statement orBranch = statement();

    return new Statement.Or(condition, orBranch);
  }


  private Statement printStatement() {
    consume(R_ASSIGN, "Directional '->' Token expected after LOG declaration.");
    Express value = expression();
    consume(LINE_END, "Expected endline value '.' after statement.");

    return new Statement.Print(value);
  }

  private Statement varDefinition() {
    Token name = consume(IDENTIFIER, "Variable name expected.");

    Express initial = null;
    if (match(L_ASSIGN)) { initial = expression(); }

    consume(LINE_END, "Expected endline value '.' after declaration.");
    return new Statement.Variable(name, initial);
  }

  private Statement expressionStatement() {
    Express express = expression(); 
    consume(LINE_END, "Expected endline value '.' after expression.");

    return new Statement.Expression(express);
  }

  private List<Statement> block() {
    List<Statement> statements = new ArrayList<>();

    while (!check(BODY_END) && !isAtEnd()) {
      statements.add(definition());
    }

    consume(BODY_END, "Expecting '~' after body.");
    return statements;
  }

  private Express assignment() {
    Express expr = equality();

    if (match(L_ASSIGN)) {
      Token equals = previous();
      Express value = assignment();

      if (expr instanceof Express.Variable) {
        Token name = ((Express.Variable)expr).name;
        return new Express.Assign(name, value);
      }

    error(equals, "Invalid assignment target.");
    }
    return expr;
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
    if (match(IDENTIFIER)) { return new Express.Variable(previous()); }
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
