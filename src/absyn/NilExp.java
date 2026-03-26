package absyn;

public class NilExp extends Exp {

    public NilExp(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}