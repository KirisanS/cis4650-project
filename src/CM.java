import java.io.*;
import absyn.*;

class CM {
  // public final static boolean SHOW_TREE = true;
  // public final static boolean SHOW_SEMANTIC = true;
  public static void main(String argv[]) {

    try {
      // not showing without -c arg
      boolean saveTree = false;
      boolean saveSymbolsAndTree = false;
      String filename = null;

      //allows fof either arg input order
      for(int n=0; n < argv.length; n++){
        if (argv[n].equals("-a")){
          saveTree = true;
        }
        else if(argv[n].equals("-s")){
          saveSymbolsAndTree = true;
        }
        else{
          filename = argv[n];
        }
      }

      if (filename == null) {
        System.err.println("Error: no input file specified");
        return;
      }
        //parses the file
        parser p = new parser(new Lexer(new FileReader(filename)));
        Absyn result = (Absyn)(p.parse().value);
        //displays syntax tree and semantic analysis
        if (saveSymbolsAndTree && result != null) {
          System.out.println("The abstract syntax tree is:");
          AbsynVisitor visitor = new ShowTreeVisitor();
          result.accept(visitor, 0);
          
          SemanticAnalyzer visitor2 = new SemanticAnalyzer();
          result.accept(visitor2, 0);
          visitor2.table.exitScope("global scope", 0);
          System.out.println("Parsing completed.");
      
        }
        //displays syntax tree 
        else if (saveTree && result != null) {
          System.out.println("The abstract syntax tree is:");
          AbsynVisitor visitor = new ShowTreeVisitor();
          result.accept(visitor, 0);
          System.out.println("Parsing completed.");
      
        }
        else {
            System.out.println("Bad news: -s or -a required to run syntax tree and/or semantic analysis :(");
        }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}