import java.io.*;

class Main {
  public static void main(String argv[]) {
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      p.parse();
      System.out.println("Parsing completed.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}