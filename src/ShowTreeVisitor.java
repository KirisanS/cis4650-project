import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

    final static int SPACES = 4;

    public Boolean errorOccured = false;

    private void indent( int level ) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    /* LISTS */ 
    public void visit(DecList exp, int level, boolean isAddr) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false); 
            }
            exp = exp.tail;
        }
    }

    public void visit(ExpList exp, int level, boolean isAddr) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false); 
            }
            exp = exp.tail;
        }
    }

    public void visit(VarDecList exp, int level, boolean isAddr) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false); 
            }
            exp = exp.tail;
        }
    }

    /* Expression */ 
    public void visit(AssignExp exp, int level, boolean isAddr) {
        indent(level); 
        System.out.println("AssignExp: ");
        level++; 
        exp.lhs.accept(this, level, false);
        exp.rhs.accept(this, level, false);
    }

    public void visit(BoolExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("BoolExp: " + exp.value);
    }

    public void visit(CallExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("CallExp: " + exp.func);
        level++;
        if (exp.args != null) {
            exp.args.accept(this, level, false);
        }
    }

    public void visit(CompoundExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("CompoundExp: ");
        level++;
        if (exp.decs != null) {
            exp.decs.accept(this, level, false);
        }
        if (exp.exps != null) {
            exp.exps.accept(this, level, false);
        }
    }

    public void visit(IfExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("IfExp: ");
        level++;
        if (exp.test != null) {
            exp.test.accept(this, level, false);
        }
        exp.thenpart.accept(this, level, false);
        if (exp.elsepart != null) {
            exp.elsepart.accept(this, level, false);
        }
    }

    public void visit(IntExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("IntExp: " +  exp.value);
    }

    public void visit(NilExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("NilExp");
    }

    public void visit(NameTy exp, int level, boolean isAddr) {
        indent(level);
        System.out.print("NameTy: ");
        switch (exp.type) {
            case NameTy.INTEGER: 
                System.out.println("Integer");
                break;
            case NameTy.BOOLEAN: 
                System.out.println("Boolean");
                break;
            case NameTy.VOID: 
                System.out.println("Void");
                break;
            case NameTy.ERROR: 
                System.out.println("Error");
        }
    }

    public void visit(OpExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.print("OpExp:");
        switch (exp.op) {
            case OpExp.PLUS:
                System.out.println(" + ");
                break;
            case OpExp.MINUS: 
                System.out.println(" - ");
                break;
            // DOUBLE CHECK THIS LATER 
            case OpExp.UMINUS: 
                System.out.println("-");
                break;
            case OpExp.TIMES: 
                System.out.println(" * ");
                break;
            case OpExp.DIVIDE: 
                System.out.println(" / ");
                break;
            case OpExp.EQ: 
                System.out.println(" == ");
                break;
            case OpExp.NOTEQUAL:
                System.out.println(" != ");
                break;
            case OpExp.LESSTHAN: 
                System.out.println(" < ");
                break;
            case OpExp.GREATERTHAN: 
                System.out.println(" > ");
                break;
            case OpExp.LESSEQUAL: 
                System.out.println(" <= ");
                break;
            case OpExp.GREATEQUAL: 
                System.out.println(" >= "); 
                break;
            case OpExp.NOT: 
                System.out.println(" ~ ");
                break;
            case OpExp.AND: 
                System.out.println(" && ");
                break;
            case OpExp.OR: 
                System.out.println(" || ");
                break;
        }
        level++;
        if (exp.left != null) {
            exp.left.accept(this, level, false);
        }
        if (exp.right != null) {
            exp.right.accept(this, level, false);
        }
    }

    public void visit(ReturnExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("ReturnExp: ");
        level++;
        exp.exp.accept(this, level, false);
    }

    public void visit(VarExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("VarExp: ");
        level++;
        exp.variable.accept(this, level, false);
    }

    public void visit(WhileExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("WhileExp: ");
        level++;
        exp.test.accept(this, level, false);
        exp.body.accept(this, level, false);
    }

    /* Declaration */ 
    public void visit(ArrayDec exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("ArrayDec: " + exp.name + ", Size: " + exp.size);
        level++;
        exp.typ.accept(this, level, false);
    }

    public void visit(FunctionDec exp, int level, boolean isAddr) {
        indent(level); 
        System.out.println("FunctionDec: " +  exp.func);
        level++;
        indent(level); 
        System.out.println("Return Type: ");
        exp.result.accept(this, level + 1, false);
        if (exp.params != null) {
            exp.params.accept(this, level, false); 
        }
        indent(level);
        System.out.println("Body: ");
        level++;
        if (exp.body != null) {
            exp.body.accept(this, level, false);
        }
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("SimpleDec: " + exp.name);
        level++;
        exp.typ.accept(this, level, false);

    }

    /* Var */ 
    public void visit(IndexVar exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("IndexVar: " + exp.name);
        level++;
        if(exp.index != null) {
            exp.index.accept(this, level, false);
        }
    }

    public void visit(SimpleVar exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("SimpleVar: " + exp.name);
    }

    public void visit(ErrorExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("[Error Expression Occured]");
        errorOccured = true;
    }

    public void visit(ErrorDec exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("[Error Declaration Occured]");
        errorOccured = true;
    }

    public void visit(ErrorVar exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("[Error Variable Occured]");
        errorOccured = true;
    }













    



    // :(
}