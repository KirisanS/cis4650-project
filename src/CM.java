import java.io.*;
import absyn.*;

class CM { 
  // public final static boolean SHOW_TREE = true;
  // public final static boolean SHOW_SEMANTIC = true;
  public static void main(String argv[]) {

    try {
      //used to determine what output we want to display
      boolean saveTree = false;
      boolean saveSymbols= false;
      String inputFile = null;
      String baseFile = null;

      //allows fof either arg input order
      for(int n=0; n < argv.length; n++){
        if (argv[n].equals("-a")){
          saveTree = true;
        }
        else if(argv[n].equals("-s")){
          saveSymbols = true;
        }
        else {
          inputFile = argv[n];
        }
      }

      if (inputFile == null) {
        System.err.println("Error: no input file given");
        return;
      }

      if (!saveTree && !saveSymbols) {
        System.err.println("Error: -a and/or -s required to run syntax tree and/or semantic analysis :(");
        return;
      }

      if (inputFile.endsWith(".cm")) {
        baseFile = inputFile.substring(0, inputFile.length() - 3);
      } else {
        baseFile = inputFile;
      }

      //parses the file
      parser p = new parser(new Lexer(new FileReader(inputFile)));
      Absyn result = (Absyn)(p.parse().value);

      if (result == null) {
        System.err.println("Syntax errors found, program terminated.");
        return;
      }

      PrintStream console = System.out;
        
      //displays syntax tree 
      if (saveTree && result != null) {
        PrintStream outputFile = new PrintStream(new FileOutputStream(baseFile + ".abs"));
        System.setOut(outputFile);
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0, false);
        outputFile.close();
        System.setOut(console);
        System.out.println("Syntax tree saved to " + baseFile + ".abs");
      
      }
      //saves symbol table to file if there are no syntax errors
      if (saveSymbols) {

        //System.out.println("The abstract syntax tree is:");
        // ShowTreeVisitor visitor = new ShowTreeVisitor();
        // result.accept(visitor, 0);

        if (p.valid) {
          PrintStream outputFile = new PrintStream(new FileOutputStream(baseFile + ".sym"));
          System.setOut(outputFile);
          SemanticAnalyzer visitor2 = new SemanticAnalyzer();
          result.accept(visitor2, 0, false);
          visitor2.table.exitScope("global scope", 0);
          outputFile.close();
          System.setOut(console);
          System.out.println("Symbol table saved to " + baseFile + ".sym");
        } else {
          System.err.println("Error occurred during Parsing. Exiting Semantic Analysis");
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}