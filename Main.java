import java.io.*;
import absyn.*;

class Main {
  public final static boolean SHOW_TREE = true;
  public static void main(String argv[]) {
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);
      if (SHOW_TREE && result != null) {
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0);
      }
      System.out.println("Parsing completed.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}