package absyn;

public class ErrorExp extends Exp {

    public ErrorExp(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}