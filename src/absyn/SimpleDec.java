package absyn;

public class SimpleDec extends VarDec {

    public NameTy typ;
    public String name;

    // checkpoint 3
    public int nestLevel;
    public int offset;

    public SimpleDec(int row, int col, NameTy typ, String name) {
        this.row = row;
        this.col = col;
        this.typ = typ;
        this.name = name;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}