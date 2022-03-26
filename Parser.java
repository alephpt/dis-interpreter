package dev.alephpt.Dis;

import java.util.ArrayList;
import java.util.Arrays;
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
      if (match(OP)) { return operation("operation"); }
      if (match(DEFINE)) { return varDefinition(); }
      return statement();
    } catch (ParserError error) {
      synchronize();
      return null;
    }
  }

  private Statement statement() {
    if (match(AS)) { return asStatement(); }
    if (match(WHEN)) { return whenStatement(); }
    if (match(LOG)) { return printStatement(); }
    if (match(WHILE)) { return whileStatement(); }
    if (match(BODY_START)) { return new Statement.Body(block()); }

    return expressionStatement();
  }
  

  ////////
  // AS //
  ////////

  private Statement asStatement() {
    consume(COMMA, "',' expected after 'as'.");
    
    // initializer conditions
    Statement initializer;
    if(match(LINE_END)) { 
      initializer = null; 
    }
    else if(match(DEFINE)) { 
      initializer = varDefinition(); 
    }
    else { 
      initializer = expressionStatement(); 
    }
  
    // increment conditions
    Express increment = null;
    consume(L_PAR, "Opening '(' expected for increment body.");
    if(!check(R_PAR)) { increment = expression(); }
    consume(R_PAR, "Closing ')' expected after increment expression.");
    
    // conditional conditions
    Express condition = null;
    if (!check(COLON)) { condition = expression(); }
    consume(COLON, "':' expected after as clauses.");

    Statement body = statement();

    if (increment != null) {
      body = new Statement.Body(
          Arrays.asList(
            body,
            new Statement.Expression(increment)));
    }

    if (condition == null) { condition = new Express.Literal(true); }
    body = new Statement.While(condition, body);

    if (initializer != null) {
      body = new Statement.Body(Arrays.asList(initializer, body));
    }
    
    return body;
  }


  //////////
  // WHEN //
  //////////

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


  ////////
  // OR //
  ////////

  private Statement.Or orStatement() {
    consume(COMMA, "',' expected after 'or'.");
    Express condition = expression();
    consume(COLON, "closing ':' expected after or conditional.");

    Statement orBranch = statement();

    return new Statement.Or(condition, orBranch);
  }


  /////////
  // LOG //
  /////////
  
  private Statement printStatement() {
    consume(R_ASSIGN, "Directional '->' Token expected after LOG declaration.");
    Express value = expression();
    consume(LINE_END, "Expected endline value '.' after statement.");

    return new Statement.Print(value);
  }

  ////////
  // OP //
  ////////
  
  private Statement.Operation operation(String kind) {
    Token name = consume(IDENTIFIER, "expected " + kind + " name.");
    consume(COMMA, "',' expected after " + kind + " declaration.");
    List<Token> params = new ArrayList<>();

    if(!check(COLON)) {
      do {
        if(params.size() >= 255) { error(peek(), "parameters exceed maximum 255 Inputs."); }

        params.add(consume(IDENTIFIER, "parameter name expected."));
      } while (match(COMMA));
    }
    consume(COLON, "closing ':' expected after " + kind + " parameters.");
    consume(BODY_START, "opening '|' expected before " + kind + " body.");
    List<Statement> body = block();
    return new Statement.Operation(name, params, body);
  }



  /////////
  // DEF //
  /////////
  
  private Statement varDefinition() {
    Token name = consume(IDENTIFIER, "Variable name expected.");

    Express initial = null;
    if (match(L_ASSIGN)) { initial = expression(); }

    consume(LINE_END, "Expected endline value '.' after declaration.");
    return new Statement.Variable(name, initial);
  }


  ///////////
  // WHILE //
  ///////////
  
  private Statement whileStatement() {
    consume(COMMA, "',' expected after 'while'.");
    Express condition = expression();
    consume(COLON, "':' expected after while condition.");
    Statement body = statement();

    return new Statement.While(condition, body);
  }

  // expression Statement //
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
    return calling();
  }

  private Express finishCalling(Express called) {
    List<Express> args = new ArrayList<>();
    
    if (!check(COLON)) {
      do {
        if (args.size() >= 255) { error(peek(), "Maximum of 255 Arguments Exceeded."); }

        args.add(expression());
      } while (match(COMMA));
    }

    Token par = consume(COLON, "Closing ':' expected after function parameters");

    return new Express.Calling(called, par, args);
  }

  private Express calling() {
    Express expr = primary();

    while (true) {
      if(match(COMMA)){
        expr = finishCalling(expr);
      } else {
        break;
      }
    }

    return expr;
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
        case OP:
        case SET:
        case DEFINE: 
        case LOG: 
        case WHEN:
        case OR:
        case ELSE:
        case AS:
        case WHILE:
        case RETURN:
        case LINE_END:
          return;
      }

      advance();
    }
  }
}
