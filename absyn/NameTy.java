package absyn; 
public class NameTy extends Absyn {

    public static final int INTEGER = 0; 
    public static final int BOOLEAN = 1;
    public static final int VOID = 2; 
    public int type; 

    public NameTy(int row, int col, int type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }


}