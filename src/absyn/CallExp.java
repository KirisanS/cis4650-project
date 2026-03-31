package absyn;

public class CallExp extends Exp {

    public String func;
    public ExpList args;

    // checkpoint 3
    public FunctionDec funcDec;

    public CallExp(int row, int col, String func, ExpList args) {
        this.row = row;
        this.col = col;
        this.func = func;
        this.args = args;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}