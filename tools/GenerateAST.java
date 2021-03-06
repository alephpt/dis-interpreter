package dev.alephpt.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];
     
    defineAST(outputDir, "Statement", Arrays.asList(
          "Expression   : Express expression",
          "Operation    : Token name, List<Token> params, List<Statement> body",
          "Or           : Express condition, Statement orBranch",
          "When         : Express condition, Statement thenBranch, List<Statement.Or> orBranches, Statement elseBranch",
          "While        : Express condition, Statement body",
          "Print        : Express expression",
          "Return       : Token keyword, Express value",
          "Body         : List<Statement> statements",
          "Obj          : Token name, List<Statement> body",// List<Statement.Variable> members",
          "Enum         : Token name, List<Express.Variable> elements",
          "Form         : Token name, List<Statement.Variable> members",
          "Variable     : Token name, Express initial"
          )
    );
    
    defineAST(outputDir, "Express", Arrays.asList(
          "Assign         : Token name, Express value",
          "Count          : Token operator, Express identifier, Token name",
          "Unary          : Token operator, Express right",
          "Binary         : Express left, Token operator, Express right",
          "Calling        : Express called, List<Express> args",
          "Self           : Token keyword",
          "GetProps       : Express object, Token name",
          "SetProps       : Express object, Token locale, Token name, Express value",
          "Grouping       : Express expression",
          "Literal        : Object value",
          "Logical        : Express left, Token operator, Express right",
          "Variable       : Token name",
          "ParentVariable : Token name",
          "GlobalVariable : Token name"
          )
    );
  }

  private static void defineAST(String outputDir, String baseName, List<String> types) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package dev.alephpt.Dis;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");

    writer.println();
    
    defineVisitor(writer, baseName, types);

    // the AST Classes
    for (String type : types) {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, baseName, className, fields);
    }

    // base accept() method
    writer.println();
    writer.println();

    writer.println("  abstract <R> R accept(Visitor<R> visitor);");

    writer.println("}");
    writer.close();
  }

  private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
    writer.println();
    writer.println();
    writer.println("  // " + className + " " + baseName + " Definition //");
    // declaration
    writer.println("  static class " + className + " extends " + baseName + " {");

    // constructor
    writer.println("    " + className + "(" + fieldList + ") {");

    // parameters in fields
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";"); 
    }

    writer.println("    }");

    writer.println();
    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("      return visitor.visit" + className + baseName + "(this);");
    writer.println("    }");

    // fields
    writer.println();
    for (String field : fields) {
      writer.println("    final " + field + ";");
    }

    writer.println("  }");
  }
  
  private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
    writer.println("  interface Visitor<R> {");

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println("    R visit" + typeName + baseName + "(" 
                            + typeName + " " + baseName.toLowerCase() + ");");
    }
    writer.println("  }");
  }
}

