package dev.alephpt.Dis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.alephpt.Dis.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;
  private static final Map<String, TokenType> literals;

  static { 
    literals = new HashMap<>();
    literals.put("and",     AND);
    literals.put("when",    WHEN);
    literals.put("then",    THEN);
    literals.put("or",      OR);
    literals.put("else",    ELSE);
    literals.put("for",     FOR);
    literals.put("while",   WHILE);
    literals.put("none",    NONE);
    literals.put("obj",     CLASS);
    literals.put("log",     PRINT);
    literals.put("def",     DEFINE);
    literals.put("include", INCLUDE);
    literals.put("true",    TRUE);
    literals.put("false",   FALSE);
    literals.put("this",    THIS);
    literals.put("super",   SUPER);
  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while(!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(L_PAR); break;
      case ')': addToken(R_PAR); break;
      case '[': addToken(L_BRACK); break;
      case ']': addToken(R_BRACK); break;
      case '{': addToken(L_BRACE); break;
      case '}': addToken(R_BRACE); break;
      case '<': addToken(match('-') ? L_ASSIGN :
                         match('=') ? LESS_EQ :
                         match('>') ? INEQ :
                                      LESSER);
                break;
      case '>': addToken(match('=') ? GREAT_EQ : 
                         match('<') ? INEQ :
                                      GREATER); 
                break;
      case '!': addToken(match('=') ? INEQ : NOT); 
                break;
      case ',': addToken(COMMA); break;
      case '.': addToken(LINE_END); break;
      case '-': addToken(match('>') ? R_ASSIGN :
                         match('-') ? MINUSMINUS :
                         match('=') ? MINUS_EQ :
                                      MINUS);
                break;
      case '+': addToken(match('+') ? PLUSPLUS :
                         match('=') ? PLUS_EQ :
                                      PLUS); 
                break;
      case ':': addToken(NESTING); break;
      case ';': addToken(SEMIC); break;
      case '/': if (match('*')) {
                  blockComment();
                } else
                if (match('/')) {
                  while(peek() != '\n' && !isAtEnd()) advance(); 
                } else {
                  addToken(WHACK);
                }
                break;
      case '\\': addToken(BWHACK); break;
      case '*': addToken(match('*') ? POWER : STAR); break;
      case '?': addToken(QUEST); break;
      case '$': addToken(DOLLAR); break;
      case '#': addToken(HASH); break;
      case '^': addToken(RETURN); break;
      case '~': addToken(BODY_END); break;
      case '&': addToken(match('&') ? AND_OP : REF); break;
      case '|': addToken(match('|') ? OR_OP : BODY_START); break;
      case '=': addToken(match('=') ? EQEQ :
                         match('+') ? EQ_PLUS :
                         match('-') ? EQ_MINUS :
                         match('>') ? R_OUT :
                         match('<') ? L_OUT :
                                      EQ); 
                break;

      

      case ' ':
      case '\r':
      case '\t':
                break;

      case '\n': line++; break;

      case '"': string(); break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) { 
          identifier();
        } else {
          DisC.error(line, "Unexpected character.");
        }
        break;
      
    }
  }

  private void blockComment() {
    while(peek() != '*' && peekNext() != '/' && !isAtEnd()) {
      advance();
    }

    if(peek() == '*' && peekNext() == '/') { advance(); advance(); }

    if (isAtEnd()) {
      DisC.error(line, "Unterminated comment.");
      return;
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) { advance(); }
    
    String text = source.substring(start, current);
    TokenType type = literals.get(text);
    
    if (type == null) { type = IDENTIFIER; }

    addToken(type);
  }

  private void number() {
    boolean isDecimal = false;
    while (isDigit(peek())) advance();

    if (peek() == '.' && isDigit(peekNext())) {
      isDecimal = true;
      advance();

      // come back and improve edge cases
      while (isDigit(peek())) { advance(); }
    }

    if (isDecimal) { 
      addToken(NUMERAL, Double.parseDouble(source.substring(start, current)));
    } else {
      addToken(NUMERAL, Integer.parseInt(source.substring(start, current)));
    }
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      DisC.error(line, "Unterminated string.");
      return;
    }

    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
