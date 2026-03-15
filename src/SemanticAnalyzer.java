import absyn.*;

public class SemanticAnalyzer implements AbsynVisitor {
    SymbolTable table; 
    NameTy currentFunctionReturnType;

    public SemanticAnalyzer() {
        table = new SymbolTable();
        table.enterScope("global scope", 0);

        // predefined function: int input(void)
        // return type int, params none
        FunctionDec inputFunc =
            new FunctionDec(0,0,new NameTy(0,0,NameTy.INTEGER),"input",null,null);

        table.insert("input", inputFunc);

        // predefined function: void output(int x)
        // return type void, params int
        SimpleDec param =
            new SimpleDec(0,0,new NameTy(0,0,NameTy.INTEGER),"x");

        VarDecList params = new VarDecList(param,null); // can have multiple params

        FunctionDec outputFunc =
            new FunctionDec(0,0,new NameTy(0,0,NameTy.VOID),"output",params,null);

        table.insert("output", outputFunc);
    }

    // final static Dec dummyInt = new SimpleDec(0, 0,
    //     new NameTy(0, 0, NameTy.INT), "");
    // final static Dec dummyBool = new SimpleDec(0, 0,
    //     new NameTy(0, 0, NameTy.BOOL), "");

    final static int SPACES = 4;

    // helpers
    private void indent(int level) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    // last declaration must be void main(void)
    private void checkMain(Dec last) {
        // int x; void main(void){} int y; -> BAD
        if (!(last instanceof FunctionDec)) {
            System.err.println("Error: last declaration must be void main(void)");
            return;
        }

        FunctionDec f = (FunctionDec) last;

        // is function name main: void start(void){} -> BAD
        if (!f.func.equals("main")) {
            System.err.println("Error: last declaration must be void main(void)");
            return;
        }

        // is return type void: int main(void){} -> BAD
        if (f.result.type != NameTy.VOID) {
            System.err.println("Error: main must return void");
        }

        // params must be void
        if (f.params != null) {
            System.err.println("Error: main must have void parameters");
        }
    }

    /* LISTS */ 
    public void visit(DecList exp, int level) {
        Dec last = null;

        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level);
                last = exp.head;
            }
            exp = exp.tail;
        }

        // after processing all declarations
        checkMain(last);
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

        // store the return type of this function
        currentFunctionReturnType = exp.result;

        if(!(table.insert(exp.func, exp))) {
            System.err.println("Error: Function Declaration for '" + exp.func + "' already exists within the current scope");
        }
        level++;
        table.enterScope("the new function scope " + exp.func, level);
        if (exp.params != null) {
            exp.params.accept(this, level + 1);
        }
        if (exp.body != null)   {
            exp.body.accept(this, level + 1);
        }
        table.exitScope("the function scope " + exp.func, level);
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

        // var can be SimpleVar or IndexVar so cast accordingly
        if (exp.variable instanceof IndexVar) {
            name = ((IndexVar) exp.variable).name;
        } else if (exp.variable instanceof SimpleVar) {
            name = ((SimpleVar) exp.variable).name;
        } 

        Dec decType = table.lookup(name);

        if(decType == null) {
            System.err.println("Error: Variable in expression '" + name + "' has not been declared yet");
            return; // stop further checks if variable is not declared
        }

        // int f(int a){return a;}
        // int x; x = f; -> BAD
        if (decType instanceof FunctionDec) {
            System.err.println("Error: function '" + name + "' used as variable");
        }

        // int a[10];
        // int x;
        // x = a; -> BAD
        if (decType instanceof ArrayDec && exp.variable instanceof SimpleVar) {
            System.err.println("Error: array '" + name + "' used without index");
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

        // lhs must be a variable
        if (!(exp.lhs instanceof VarExp)) {
            System.err.println("Error: left side of assignment must be variable");
        }

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
        // result of arithmetic operation is int
        exp.dtype = new SimpleDec(0,0,new NameTy(0,0,NameTy.INTEGER),"");
    }

    public void visit(BoolExp exp, int level) {
        
    }

    public void visit(CallExp exp, int level) {
        // are CALLING the function
        // x = add(3, 4);
        // exp.func = add
        // exp.args = 3, 4

        // what exactly is function declared as
        Dec dec = table.lookup(exp.func);

        // triggers when calling a function that isn't declared
        if (dec == null) {
            System.err.println("Error: function '" + exp.func + "' not declared");
            return;
        }

        if (!(dec instanceof FunctionDec)) {
            System.err.println("Error: '" + exp.func + "' is not a function");
            return;
        }

        // compiler sees dec as Dec, yes FunctionDec extends Dec but
        // we won't get access to .params or .result
        FunctionDec func = (FunctionDec) dec;

        // check number of arguments and types of arguments
        VarDecList params = func.params;
        ExpList args = exp.args;

        // case where parameters and arguments both exist
        // args != nulll -> needed for situations such as add(3,4, x + y)
        while (params != null && args != null) {

            args.head.accept(this, level);

            // x = add(y, 5); where y is undeclared
            if (params.head != null && args.head.dtype != null) {

                int paramType = getType(params.head);
                int argType = getType(args.head.dtype);

                if (paramType != argType) {
                    System.err.println("Error: argument type mismatch in call to '" + exp.func + "'");
                }
            }

            params = params.tail;
            args = args.tail;
        }

        // case where there are too many arguments or too few arguments
        while (args != null) {
            args.head.accept(this, level);
            args = args.tail;
        }

        if (params != null || args != null) {
            System.err.println("Error: wrong number of arguments in call to '" + exp.func + "'");
        }

        // add(3,4) returns int, so the call expression has type int
        exp.dtype = new SimpleDec(0, 0, func.result, "");
    }

    public void visit(CompoundExp exp, int level) {
        table.enterScope("the new block", level);
        if (exp.decs != null) {
            exp.decs.accept(this, level);
        }
        if (exp.exps != null) {
            exp.exps.accept(this, level);
        }
        table.exitScope("the block", level);
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
        // return 5;
        // exp.exp =

        // return can be expression: return x + 5; -> needs to be checked
        if (exp.exp != null) {
            exp.exp.accept(this, level);
        }

        // or just a plain return;
        if (exp.exp == null) {
            if (currentFunctionReturnType.type != NameTy.VOID) {
                System.err.println("Error: return without value in non-void function");
            }
            return;
        }

        if (exp.exp != null && exp.exp.dtype != null) {
            // what type is returned from the return call
            int returnType = getType(exp.exp.dtype);

            // compare with what function header claims
            if (returnType != currentFunctionReturnType.type) {
                System.err.println("Error: return type mismatch");
            }
        }
    }

    public void visit(WhileExp exp, int level) {
        
    }

    public void visit(IndexVar exp, int level) {
        // x = a[2];
        // exp.name = "a"
        // exp.index = 2

        // what is the array declared as - arraydec, simpledec, functiondec
        Dec dec = table.lookup(exp.name);

        if (dec == null) {
            System.err.println("Error: array '" + exp.name + "' not declared");
            return;
        }

        if (!(dec instanceof ArrayDec)) {
            System.err.println("Error: '" + exp.name + "' is not an array");
        }

        // index itself can be an expression: a[y + 2], so run semantic analysis
        if (exp.index != null) {
            exp.index.accept(this, level);
        }

        if (exp.index != null && exp.index.dtype != null) {
            int indexType = getType(exp.index.dtype);

            // is the index an integer: bool b; a[b]; -> BAD
            if (indexType != NameTy.INTEGER) {
                System.err.println("Error: array index must be integer");
            }
        }

        // the type of a[i] should be equal the same type result
        // exp.dtype = dec;
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
