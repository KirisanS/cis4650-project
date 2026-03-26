package absyn;

public class ArrayDec extends VarDec {

    public NameTy typ;
    public String name;
    public int size;

    // checkpoint 3
    public int nestLevel;
    public int offset;

    public ArrayDec(int row, int col, NameTy typ, String name, int size) {
        this.row = row;
        this.col = col;
        this.typ = typ;
        this.name = name;
        this.size = size;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}