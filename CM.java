import java.io.*;
import absyn.*;

class CM {
  public final static boolean SHOW_TREE = true;
  public static void main(String argv[]) {

    try {
      // not showing without -c arg
      boolean RUN_PARSER = false;
      String filename = null;

      //allows fof either arg input order
      for(int i =0; i<argv.length; i++){
        if (argv[i].equals("-a")){
          RUN_PARSER = true;
        }
        else{
          filename = argv[i];
        }
      }
      if (RUN_PARSER){
        parser p = new parser(new Lexer(new FileReader(filename)));
        Absyn result = (Absyn)(p.parse().value);
        if (SHOW_TREE && result != null) {
          System.out.println("The abstract syntax tree is:");
          AbsynVisitor visitor = new ShowTreeVisitor();
          result.accept(visitor, 0);
        }
        System.out.println("Parsing completed.");
      }
      else {
          System.out.println("Bad news: -a required to run syntax tree, no other arguments implemented");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
