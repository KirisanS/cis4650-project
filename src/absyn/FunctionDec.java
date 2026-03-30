package absyn;

public class FunctionDec extends Dec {

    public NameTy result;
    public String func;
    public VarDecList params;
    public Exp body;

    // checkpoint 3
    public int funaddr;
    public int functionEntry;

    public FunctionDec(int row, int col, NameTy result, String func, VarDecList params, Exp body) {
        this.row = row;
        this.col = col;
        this.result = result;
        this.func = func;
        this.params = params;
        this.body = body;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}