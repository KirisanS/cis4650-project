import java.util.*;
import absyn.*;

public class SemanticAnalyzer implements AbsynVisitor{
    HashMap<String, ArrayList<NodeType>> table;
    int level = 0;

    public SemanticAnalyzer() {
        table = new HashMap<>();
    }

    public void visit(DecList exp, int level) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept( this, level ); 
            }
            exp = exp.tail;
        }
    }

    public void visit(ExpList exp, int level) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept( this, level ); 
            }
            exp = exp.tail;
        }
    }

    public void visit(VarDecList exp, int level) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept( this, level ); 
            }
            exp = exp.tail;
        }
    }
}
