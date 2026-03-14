package absyn;

public class OpExp extends Exp {

    public static final int PLUS        = 0;
    public static final int MINUS       = 1;
    public static final int UMINUS      = 2;
    public static final int TIMES       = 3;
    public static final int DIVIDE      = 4;
    public static final int EQ          = 5;
    public static final int NOTEQUAL    = 6;
    public static final int LESSTHAN    = 7;
    public static final int GREATERTHAN = 8;
    public static final int LESSEQUAL   = 9;
    public static final int GREATEQUAL  = 10;
    public static final int NOT         = 11; 
    public static final int AND         = 12;
    public static final int OR          = 13;


    public Exp left;
    public int op;
    public Exp right;

    public OpExp(int row, int col, Exp left, int op, Exp right) {
        this.row = row;
        this.col = col;
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}