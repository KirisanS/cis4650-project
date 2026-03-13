import absyn.*;

public class SemanticAnalyzer implements AbsynVisitor {


    SymbolTable table; 

    public SemanticAnalyzer() {
        table = new SymbolTable();
        table.enterScope("the global scope", 0);
    }

    // final static Dec dummyInt = new SimpleDec(0, 0,
    //     new NameTy(0, 0, NameTy.INT), "");
    // final static Dec dummyBool = new SimpleDec(0, 0,
    //     new NameTy(0, 0, NameTy.BOOL), "");


    final static int SPACES = 4;

    private void indent(int level) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    /* LISTS */ 
    public void visit(DecList exp, int level) {
        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level);
            }
            exp = exp.tail;
        }
    }

    public void visit(ExpList exp, int level) {
        while ( exp != null ) {
            if (exp.head != null) {
                exp.head.accept(this, level); 
            }
            exp = exp.tail;
        }
    }

    public void visit(VarDecList exp, int level) {
        while ( exp != null) {
            if (exp.head != null) {
                exp.head.accept( this, level ); 
            }
            exp = exp.tail;
        }
    }

    /* Declarations */

    public void visit(FunctionDec exp, int level) {

        if(!(table.insert(exp.func, exp))) {
            System.err.println("Error: Function Declaration for '" + exp.func + "' alreay exists within the current scope");
        }
        level++;
        table.enterScope("the scope for function " + exp.func, level);
        if (exp.params != null) {
            exp.params.accept(this, level + 1);
        }
        if (exp.body != null)   {
            exp.body.accept(this, level + 1);
        }
        table.exitScope("the function scope", level);
    }

    public void visit(ArrayDec exp, int level) {
        //indent(level);

        // Void Check
        if(exp.typ.type == 2) {
            System.err.println("Error: 'void' declaration encountered with '" + exp.name + "'. Changing declaration to 'int'. ");
            exp.typ.type = 0;
        }
        if(!(table.insert(exp.name, exp))) {
            System.err.println("Error: Array Declaration for '" + exp.name + "' already exists within the current scope");
        } else {
            level++;
        }
    }

    public void visit(SimpleDec exp, int level) {
        //indent(level);
        // Void Check
        if(exp.typ.type == 2) {
            System.err.println("Error: 'void' declaration encountered with '" + exp.name + "'. Changing declaration to 'int'. ");
            exp.typ.type = 0;
        }
        if(!(table.insert(exp.name, exp))) {
            System.err.println("Error: Declaration for '" + exp.name + "' already exists within the current scope");
        } else {
            level++;
        }
    }

    /* Expression */
    public void visit(VarExp exp, int level) {
        //indent(level);

        String name = null;

        if (exp.variable instanceof IndexVar) {
            name = ((IndexVar) exp.variable).name;
        } else if (exp.variable instanceof SimpleVar) {
            name = ((SimpleVar) exp.variable).name;
        } 

        Dec decType = table.lookup(name);

        if(decType == null) {
            System.err.println("Error: Variable in expression '" + name + "' has not been declared yet");
        }

        exp.dtype = decType;

    }

    /* Variables */ 
    public void visit(SimpleVar exp, int level) {

    }

    /* Expressions */ 
    public void visit(AssignExp exp, int level) {
        //indent(level);


        exp.lhs.accept(this, level);
        exp.rhs.accept(this, level);

        if (exp.lhs.dtype != null && exp.rhs.dtype != null) {

            int lhsType = getType(exp.lhs.dtype);
            int rhsType = getType(exp.rhs.dtype);

            if (!(lhsType == rhsType)) {
                System.err.println("Error: Mismatch of types between lhs and rhs of Assign Exp");
            }
        }
        exp.dtype = exp.lhs.dtype;
    }

    public void visit(OpExp exp, int level) {

        if (exp.left != null) {
            exp.left.accept(this, level);
        }
        if (exp.right != null) {
            exp.right.accept(this, level);
        }

        if (exp.left.dtype == null || exp.right.dtype == null) {
            return;
        }

        int leftType = getType(exp.left.dtype);
        int rightType = getType(exp.right.dtype);

        switch (exp.op) {
            /* All the exact same */ 
            case OpExp.PLUS: 
            case OpExp.MINUS:
            case OpExp.TIMES: 
            case OpExp.DIVIDE:
                if(leftType != NameTy.INTEGER) {
                    System.err.println("Error: Left hand side must be an integer");
                }
                if(rightType != NameTy.INTEGER) {
                    System.err.println("Error: Right hand side must be an integer");
                }
                break;
        }

    }

    public void visit(BoolExp exp, int level) {
        
    }

    public void visit(CallExp exp, int level) {
        
    }

    public void visit(CompoundExp exp, int level) {

        if (exp.decs != null) {
            exp.decs.accept(this, level);
        }
        if (exp.exps != null) {
            exp.exps.accept(this, level);
        }
        
    }

    public void visit(IfExp exp, int level) {
        
    }

    public void visit(IntExp exp, int level) {
        exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "");
    }

    public void visit(NilExp exp, int level) {
        
    }

    public void visit(NameTy exp, int level) {
        
    }

    public void visit(ReturnExp exp, int level) {
        
    }

    public void visit(WhileExp exp, int level) {
        
    }

    public void visit(IndexVar exp, int level) {
        
    }

    public void visit(ErrorExp exp, int level) {
        
    }

    public void visit(ErrorDec exp, int level) {
        
    }

    public void visit(ErrorVar exp, int level) {
        
    }



    private int getType(Dec dec) {
        if (dec instanceof SimpleDec) {
            return ((SimpleDec) dec).typ.type;
        } else if (dec instanceof ArrayDec) {
            return ((ArrayDec) dec).typ.type;
        }
        return -1;
    }
}