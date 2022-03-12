package dev.alephpt.Dis;

class PrintAST implements Express.Visitor<String> {
  String print(Express express) {
    return express.accept(this);
  }

  @Override
  public String visitBinaryExpress(Express.Binary express) {
    return parenthesize(express.operator.lexeme, express.left, express.right);
  }

  @Override
  public String visitGroupingExpress(Express.Grouping express) {
    return parenthesize("group", express.expression);
  }

  @Override
  public String visitLiteralExpress(Express.Literal express) {
    if (express.value == null) { return "none"; }
    return express.value.toString();
  }

  @Override
  public String visitUnaryExpress(Express.Unary express) {
    return parenthesize(express.operator.lexeme, express.right);
  }

  private String parenthesize(String name, Express... expressions) {
    StringBuilder builder = new StringBuilder();

    builder.append("( ").append(name);
    for (Express express : expressions) {
      builder.append(" ");
      builder.append(express.accept(this));
    }
    builder.append(" )");

    return builder.toString();
  }

  public static void main(String[] args) {
    Express expression = new Express.Binary(
      new Express.Unary(new Token(TokenType.MINUS, "-", null, 1), new Express.Literal(98321)),
      new Token(TokenType.STAR, "*", null, 1),
      new Express.Grouping(new Express.Literal(932.128)));

    System.out.println(new PrintAST().print(expression));
  }
}
