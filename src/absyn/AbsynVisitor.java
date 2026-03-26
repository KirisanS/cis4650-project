package absyn; 

public interface AbsynVisitor {

    public void visit(ArrayDec exp, int offset, boolean isAddr);

    public void visit(AssignExp exp, int offset, boolean isAddr);

    public void visit(BoolExp exp, int offset, boolean isAddr);

    public void visit(CallExp exp, int offset, boolean isAddr);

    public void visit(CompoundExp exp, int offset, boolean isAddr);

    public void visit(DecList exp, int offset, boolean isAddr);

    public void visit(ErrorDec exp, int offset, boolean isAddr);

    public void visit(ErrorExp exp, int offset, boolean isAddr);

    public void visit(ErrorVar exp, int offset, boolean isAddr);

    public void visit(ExpList exp, int offset, boolean isAddr);

    public void visit(FunctionDec exp, int offset, boolean isAddr);

    public void visit(IfExp exp, int offset, boolean isAddr);

    public void visit(IndexVar exp, int offset, boolean isAddr);

    public void visit(IntExp exp, int offset, boolean isAddr);

    public void visit(NameTy exp, int offset, boolean isAddr);

    public void visit(NilExp exp, int offset, boolean isAddr);

    public void visit(OpExp exp, int offset, boolean isAddr);

    public void visit(ReturnExp exp, int offset, boolean isAddr);

    public void visit(SimpleDec exp, int offset, boolean isAddr);

    public void visit(SimpleVar exp, int offset, boolean isAddr);

    public void visit(VarDecList exp, int offset, boolean isAddr);

    public void visit(VarExp exp, int offset, boolean isAddr);

    public void visit(WhileExp exp, int offset, boolean isAddr);

}