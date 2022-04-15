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

  private Express expression() { return assignment(); }

  private Statement definition() {
    try {
      if (match(OBJ)) { return objDefinition(); }
      if (match(FORM)) { return formDefinition(); }
      if (match(ENUM)) { return enumDefinition(); }
      if (match(OP)) { return operation("operation"); }
      if (match(DEFINE)) { return varDefinition(); }
      return statement();
    } catch (ParserError error) {
      synchronize();
      return null;
    }
  }

  private Statement objDefinition() {
    Token name = consume(IDENTIFIER, "Your object needs a name.");
    consume(L_BRACE, "Object body requires starting with '{' opening brace.");
    List<Statement> body = new ArrayList<>();

    while(!check(R_BRACE) && !isAtEnd()) {
      if(match(OP) || check(PILOT)) {
        body.add(operation("method"));
      } else if (match(DEFINE)) {
        body.add(varDefinition());
      }
      else {
        throw error(previous(), "The Body of an Object can only contain Operations, Definitions and an Initializer.");
      }
    }

    consume(R_BRACE, "Object body requires ending with '}' closing brace.");

    return new Statement.Obj(name, body);
  }

  private Statement formDefinition() {
    Token name = consume(IDENTIFIER, "Your form needs a name.");
    consume(L_BRACE, "Form body requires starting with '{' opening brace.");
    List<Statement.Variable> members = new ArrayList<>();

    while(!check(R_BRACE) && !isAtEnd()) {
      consume(DEFINE, "Member name must be defined. Try using 'def " + peek() + "'?");
      Token varname = consume(IDENTIFIER, "Member name expected in form '" + name.lexeme + "'.");

      Express initial = null;
      if (match(L_ASSIGN)) { initial = expression(); }

      consume(LINE_END, "Expected endline value '.' after declaration.");
      members.add(new Statement.Variable(varname, initial));
    }

    consume(R_BRACE, "Form body requires ending with '}' closing brace.");

    return new Statement.Form(name, members);
  }

  private Statement enumDefinition() {
    Token name = consume(IDENTIFIER, "Your form needs a name.");
    consume(L_BRACE, "Form body requires starting with '{' opening brace.");
    List<Express.Variable> elements = new ArrayList<>();

    while(!check(R_BRACE) && !isAtEnd()) {
      Token initName = consume(IDENTIFIER, "Element name expexted in enum element definition.");
      consume(LINE_END, "Expected endline value '.' after enum element declaration.");

      elements.add(new Express.Variable(initName));
    }

    consume(R_BRACE, "Form body requires ending with '}' closing brace.");

    return new Statement.Enum(name, elements);
  }

  private Statement statement() {
    if (match(AS)) { return asStatement(); }
    if (match(WHEN)) { return whenStatement(); }
    if (match(LOG)) { return printStatement(); }
    if (match(RETURN)) { return returnStatement(); }
    if (match(WHILE)) { return whileStatement(); }
    if (match(BODY_START)) { return new Statement.Body(block()); }

    return expressionStatement();
  }
  

  ////////
  // AS //
  ////////

  private Statement asStatement() {
    consume(COMMA, "',' expected after 'as'.");
 
    boolean nullinit = false;
    boolean initdef = false;
    Statement.Expression initializerExpr = null;
    Statement.Variable initializerVar = null;
    Token initName = null;
    Express initVal = null;
    Express increment = null;
    Express condition = null;
    Token operator = null;
    Express right = null;
    Token counter = null;
    

    // initializer conditions
    if(match(LINE_END)) { 
      nullinit = true;
    }
    else if(match(DEFINE)) {
      initName = consume(IDENTIFIER, "Variable name expected in definition.");
      consume(L_ASSIGN, "Assignment expected for scoped 'as' declaration.");
      initVal = expression(); 
      consume(LINE_END, "Expected endline value '.' inside scoped 'as' declaration.");
      initializerVar = new Statement.Variable(initName, initVal);
      initdef = true;
    }
    else {
      if(match(IDENTIFIER)) {
        initName = previous();
        initVal = new Express.Variable(initName); 
        initializerExpr = new Statement.Expression(initVal);
      }
      consume(LINE_END, "Expected endline value '.' inside scoped 'as' declaration.");
    }
    
      // increment conditions
    consume(L_PAR, "Opening '(' expected for increment body.");
    if(!check(R_PAR) && !nullinit) { 
      if (match(PLUSPLUS)) {
        counter = previous();
      }
      else if (match(MINUSMINUS)) {
        counter = previous();
      } 
      else {
        increment = assignment();
      }
    }
    consume(R_PAR, "Closing ')' expected after increment expression.");
    
    // conditional conditions
    if (!check(COLON) && !nullinit) { 
      if(match(INEQ, EQEQ)) {
        operator = previous();
        right = comparison();
      }
      if(match(GREATER, GREAT_EQ, LESSER, LESS_EQ)){
        operator = previous();
        right = term();
      }

      if(initdef){
        condition = new Express.Binary(new Express.Variable(initName), operator, right);
      } else {
        condition = new Express.Binary(initVal, operator, right);
      }
    }

    consume(COLON, "':' expected after as clauses.");

    // body of the loop
    Statement body = statement();

    // check for runtime error IF counter/increment THEN IF !nullinit

    // if the incrementer is not null, we want to increment at the end
    if ((increment != null || counter != null) && !nullinit) {
      if(counter != null){
        body = new Statement.Body(
          Arrays.asList(
            body,
            new Statement.Expression(new Express.Count(counter, new Express.Variable(initName), initName))));
      } else {
        body = new Statement.Body(
          Arrays.asList(
            body,
            new Statement.Expression(new Express.Assign(initName, increment))));
      }
    }


    if (condition == null) { condition = new Express.Literal(true); }
    body = new Statement.While(condition, body);

    if (initializerVar != null || initializerExpr != null) {
      if(initdef){
        body = new Statement.Body(Arrays.asList(initializerVar, body));
      } else {
        body = new Statement.Body(Arrays.asList(initializerExpr, body));
      }     
    }
    else if (!nullinit) {
      body = new Statement.Body(Arrays.asList(new Statement.Expression(initVal), body));
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
    consume(R_ASSIGN, "Directional Executive Token '->' expected after Log Statement.");
    Express value = expression();
    consume(LINE_END, "Expected endline value '.' after log statement.");

    return new Statement.Print(value);
  }



  ////////////
  // RETURN //
  ////////////

  private Statement returnStatement() {
    Token keyword = previous();
    Express value = null;

    if(!check(LINE_END)){
      value = expression();
    }

    consume(LINE_END, "endline '.' expected after return statement.");
    return new Statement.Return(keyword, value);
  }

  ////////
  // OP //
  ////////
  
  private Statement.Operation operation(String kind) {
    Token name = consume(IDENTIFIER, "expected " + kind + " name.");
    consume(L_ASSIGN, "Imperative Left Assignment operator '<-' expected after " + kind + " declaration.");
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

    consume(LINE_END, "Expected endline value '.' after variable declaration.");
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
    Express expr = or();

    if (match(L_ASSIGN)) {
      Token equals = previous();
      Express value = assignment();

      if (expr instanceof Express.Variable) {
        Token name = ((Express.Variable)expr).name;
        return new Express.Assign(name, value);
      } else if (expr instanceof Express.GetProps) {
        Express.GetProps get = (Express.GetProps)expr;
        return new Express.SetProps(get.object, new Token(PUBLIC, "public", null, 0), get.name, value);
      }

      error(equals, "Invalid assignment target.");
    }
    return expr;
  }

  private Express or() {
    Express expr = and();

    while (match(OR_OP)) {
      Token operator = previous();
      Express right = and();
      expr = new Express.Logical(expr, operator, right);
    }

    return expr;
  }

  private Express and() {
    Express expr = count();

    while (match(AND_OP)) {
      Token operator = previous();
      Express right = count();
      expr = new Express.Logical(expr, operator, right);
    }

    return expr;
  }

  private Express count() {
    Express expr = equality();

    if (match(PLUSPLUS)) {
      Token operator = previous();
      Token name = ((Express.Variable)expr).name;
      return new Express.Count(operator, expr, name);
    }

    if (match(MINUSMINUS)) {
      Token operator = previous();
      Token name = ((Express.Variable)expr).name;
      return new Express.Count(operator, expr, name);
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
    return scoping();
  }

  private Express scoping() {
    if(match(GLOBAL)) {
      consume(LINE_END, "'global' keyword requires '.' decimal indexing. (e.g. 'global.foo')");
      if (match(IDENTIFIER)) { return new Express.GlobalVariable(previous()); }
    } else if (match(PARENT)) {
      consume(LINE_END, "'parent' keyword requires '.' decimal indexing. (e.g. 'parent.foo')");
      if (match(IDENTIFIER)) { return new Express.ParentVariable(previous()); }
    }
    return calling();
  }

  private Express calling() {
    Express expr = primary();

    while (true) {
      if(match(R_ASSIGN)){
        expr = finishCalling(expr);
      } else
      if (match(INDEX)) {
        Token name = consume(IDENTIFIER, "Expected property name after index");
        expr = new Express.GetProps(expr, name);
      } else
      if (match(L_BRACK)) {
        Token name = consume(IDENTIFIER, "Expected property name after opening bracket");
        expr = new Express.GetProps(expr, name);
        consume(R_BRACK, "Expected closing bracket after property name.");
      } else {
        break;
      }
    }
    
    return expr;
  }

  private Express finishCalling(Express called) {
    List<Express> args = new ArrayList<>();
    
    if (!check(LINE_END) && !check(L_BRACK) && !check(INDEX)) {
      do {
        if (args.size() >= 255) { error(peek(), "Maximum of 255 Arguments Exceeded."); }
        args.add(expression());
      } while (match(COMMA));
    }  
    
    // consume(LINE_END, "Calling '" + called + "' requires End Line '.' value.");

    return new Express.Calling(called, args);
  }

  private Express primary() {
    if (match(FALSE)) { return new Express.Literal(false); }
    if (match(TRUE)) { return new Express.Literal(true); }
    if (match(NONE)) { return new Express.Literal(null); }
    if (match(NUMERAL, STRING)) { return new Express.Literal(previous().literal); }
    if (match(IDENTIFIER)) { return new Express.Variable(previous()); }
    if (match(SELF)) { return new Express.Self(previous()); }
    if (match(L_PAR)) {
      Express expr = expression();
      consume(R_PAR, "Expecting ')' after expression");
      return new Express.Grouping(expr);
    }

    throw error(previous(), peek() + "Invalid Expression Found. Something is missing.\n\t Instructions Died at the End of Execution . . RIP.");
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
        case OBJ:
        case DEFINE: 
        case LOG: 
        case WHEN:
        case OR:
        case ELSE:
        case AS:
        case WHILE:
        case RETURN:
          return;
      }

      advance();
    }
  }
}
