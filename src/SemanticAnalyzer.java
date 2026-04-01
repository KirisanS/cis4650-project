import absyn.*;

public class SemanticAnalyzer implements AbsynVisitor {
    SymbolTable table; 
    NameTy currentFunctionReturnType;
    final static int SPACES = 4;
    boolean print;
    public boolean valid = true;

    public SemanticAnalyzer(boolean print) {
        table = new SymbolTable(print);
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

    // helpers
    private void indent(int level) {
        for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
    }

    // last declaration must be void main(void)
    private void checkMain(Dec last) {
        // int x; void main(void){} int y; -> BAD
        if (!(last instanceof FunctionDec)) {
            semanticError("last declaration must be void main(void)");
            return;
        }

        FunctionDec f = (FunctionDec) last;

        // is function name main: void start(void){} -> BAD
        if (!f.func.equals("main")) {
            semanticError("last declaration must be void main(void)");
            return;
        }

        // is return type void: int main(void){} -> BAD
        if (f.result.type != NameTy.VOID) {
            semanticError("main must return void");
        }

        // params must be void
        if (f.params != null) {
            semanticError("main must have void parameters");
        }
    }

    private void semanticError(String message) {
        valid = false;
        System.err.println("Error: " + message);    
    }

    private void semanticError(int row, int col, String message) {
        valid = false;
        System.err.println(
            "Error at line " + (row + 1) +
            ", col " + (col + 1) +
            ": " + message
        );
    }

    private ExpList reverseArgs(ExpList list) {
        ExpList result = null;

        while (list != null) {
            result = new ExpList(list.head, result);
            list = list.tail;
        }

        return result;
    }

    /* LISTS */ 
    public void visit(DecList exp, int level, boolean isAddr) {
        Dec last = null;

        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false);
                last = exp.head;
            }
            exp = exp.tail;
        }

        // after processing all declarations
        checkMain(last);
    }

    public void visit(ExpList exp, int level, boolean isAddr) {
        while ( exp != null ) {
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

    /* Declarations */

    public void visit(FunctionDec exp, int level, boolean isAddr) {

        // store the return type of this function
        currentFunctionReturnType = exp.result;

        if(exp.body instanceof NilExp) {
            if(!(table.insert(exp.func, exp))) {
                semanticError(exp.row, exp.col,"Function Prototype for '" + exp.func + "' already exists within the current scope");
            }
            return;
        }

        Dec existingPrototype = table.lookup(exp.func);

        if (existingPrototype != null) {
            if (existingPrototype instanceof FunctionDec && ((FunctionDec) existingPrototype).body instanceof NilExp) {
                FunctionDec prototype = (FunctionDec) existingPrototype;
                if (prototype.result.type != exp.result.type) {
                    semanticError(exp.row, exp.col,"Function '" + exp.func + "' return type doesn't match prototype");
                }
            } else {
                semanticError(exp.row, exp.col,"Function Declaration for '" + exp.func + "' already exists within the current scope");
            }
        } else {
            table.insert(exp.func, exp);
        }
        level++;
        table.enterScope("the new function scope " + exp.func, level);
        if (exp.params != null) {
            exp.params.accept(this, level + 1, false);
        }
        if (exp.body != null)   {
            exp.body.accept(this, level + 1, false);
        }
        table.exitScope("the function scope " + exp.func, level);
    }

    public void visit(ArrayDec exp, int level, boolean isAddr) {
        //indent(level);

        // Void Check
        if(exp.typ.type == 2) {
            semanticError(exp.row, exp.col,"'void' declaration encountered with '" + exp.name + "'. Changing declaration to 'int'. ");
            exp.typ.type = 0;
        }
        if(!(table.insert(exp.name, exp))) {
            semanticError(exp.row, exp.col,"Array Declaration for '" + exp.name + "' already exists within the current scope");
        } else {
            level++;
        }
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {
        //indent(level);
        // Void Check
        if(exp.typ.type == 2) {
            semanticError(exp.row, exp.col,"'void' declaration encountered with '" + exp.name + "'. Changing declaration to 'int'. ");
            exp.typ.type = 0;
        }
        if(!(table.insert(exp.name, exp))) {
            semanticError(exp.row, exp.col,"Declaration for '" + exp.name + "' already exists within the current scope");
        } else {
            level++;
        }
    }

    /* Expression */
    public void visit(VarExp exp, int level, boolean isAddr) {

        String name = null;
        if (exp.variable instanceof IndexVar) {
            IndexVar iv = (IndexVar) exp.variable;
            name = iv.name;
            if (iv.index != null) {
                iv.index.accept(this, level, false);
            }
        } else if (exp.variable instanceof SimpleVar) {
            name = ((SimpleVar) exp.variable).name;
        } 

        Dec decType = table.lookup(name);

        if(decType == null) {
            semanticError(exp.row, exp.col,"Variable in expression '" + name + "' has not been declared yet");
            return; // stop further checks if variable is not declared
        }

        // int f(int a){return a;}
        // int x; x = f; -> BAD
        if (decType instanceof FunctionDec) {
            semanticError(exp.row, exp.col,"function '" + name + "' used as variable");
            return;
        }

        // int a[10];
        // int x;
        // x = a; -> BAD
        // if (decType instanceof ArrayDec && exp.variable instanceof SimpleVar) {
        //     semanticError(exp.row, exp.col,"array '" + name + "' used without index");
        //     return;
        // }

        exp.dtype = decType;

    }

    /* Variables */ 
    public void visit(SimpleVar exp, int level, boolean isAddr) {

    }

    /* Expressions */ 
    public void visit(AssignExp exp, int level, boolean isAddr) {
        //indent(level);


        exp.lhs.accept(this, level, false);
        exp.rhs.accept(this, level, false);

        // lhs must be a variable
        if (!(exp.lhs instanceof VarExp)) {
            semanticError(exp.row, exp.col,"left side of assignment must be variable");
        }

        if (exp.rhs instanceof VarExp) {
            VarExp rhs = (VarExp) exp.rhs;
            if (rhs.dtype instanceof ArrayDec && rhs.variable instanceof SimpleVar) {
                SimpleVar tempVar = (SimpleVar) rhs.variable;
                semanticError(exp.row, exp.col,"array '" + tempVar.name + "' used without index");
                return;
            }
        }

        if (exp.lhs.dtype != null && exp.rhs.dtype != null) {

            int lhsType = getType(exp.lhs.dtype);
            int rhsType = getType(exp.rhs.dtype);

            if (!(lhsType == rhsType)) {
                semanticError(exp.row, exp.col,"Mismatch of types between lhs and rhs of Assign Exp");
            }
        }
        exp.dtype = exp.lhs.dtype;
    }

    public void visit(OpExp exp, int level, boolean isAddr) {

        if (exp.op == OpExp.UMINUS) {
            if (exp.right != null) {
                exp.right.accept(this, level, false);
                if (exp.right.dtype != null && getType(exp.right.dtype) != NameTy.INTEGER) {
                    semanticError(exp.row, exp.col,"unary minus operand must be an integer");
                }
            }
            exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "");
            return;
        }

        if (exp.op == OpExp.NOT) {
            if (exp.right != null) {
                exp.right.accept(this, level, false);
                if (exp.right.dtype != null) {
                    int t = getType(exp.right.dtype);
                    if (t != NameTy.INTEGER && t != NameTy.BOOLEAN) {
                        semanticError(exp.row, exp.col,"NOT operand must be an integer or boolean");
                    }
                }
            }
            exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "");
            return;
        }
        if (exp.left != null) {
            exp.left.accept(this, level, false);
        }
        if (exp.right != null) {
            exp.right.accept(this, level, false);
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
                    semanticError(exp.row, exp.col,"Left hand side must be an integer");
                }
                if(rightType != NameTy.INTEGER) {
                    semanticError(exp.row, exp.col,"Right hand side must be an integer");
                }
                // result of arithmetic operation is int
                exp.dtype = new SimpleDec(0,0,new NameTy(0,0,NameTy.INTEGER),"");
                break;

            case OpExp.LESSTHAN:
            case OpExp.GREATERTHAN:
            case OpExp.LESSEQUAL:
            case OpExp.GREATEQUAL:
                if (leftType != NameTy.INTEGER) {
                    semanticError(exp.row, exp.col,"left hand side of comparison must be integer");
                }
                if (rightType != NameTy.INTEGER) {
                    semanticError(exp.row, exp.col,"right hand side of comparison must be integer");
                }
                exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "");
                break;

            case OpExp.EQ:
            case OpExp.NOTEQUAL:
                if (leftType != rightType)
                    semanticError(exp.row, exp.col,"both sides of equality must be the same type");
                exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "");
                break;

            case OpExp.AND:
            case OpExp.OR:
                if (leftType != NameTy.BOOLEAN)
                    semanticError(exp.row, exp.col,"left hand side of logical op must be boolean");
                if (rightType != NameTy.BOOLEAN)
                    semanticError(exp.row, exp.col,"right hand side of logical op must be boolean");
                exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "");
                break;
        }
    }

    public void visit(BoolExp exp, int level, boolean isAddr) {
        exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "");
    }

    public void visit(CallExp exp, int level, boolean isAddr) {
        // are CALLING the function
        // x = add(3, 4);
        // exp.func = add
        // exp.args = 3, 4

        // what exactly is function declared as
        Dec dec = table.lookup(exp.func);

        // triggers when calling a function that isn't declared
        if (dec == null) {
            semanticError(exp.row, exp.col,"function '" + exp.func + "' not declared");
            return;
        }

        if (!(dec instanceof FunctionDec)) {
            semanticError(exp.row, exp.col,"'" + exp.func + "' is not a function");
            return;
        }

        // compiler sees dec as Dec, yes FunctionDec extends Dec but
        // we won't get access to .params or .result
        FunctionDec func = (FunctionDec) dec;

        // check number of arguments and types of arguments
        VarDecList params = func.params;
        ExpList args = reverseArgs(exp.args);  // args were reversed in parser

        // case where parameters and arguments both exist
        // args != nulll -> needed for situations such as add(3,4, x + y)
        while (params != null && args != null) {

            args.head.accept(this, level, false);

            // x = add(y, 5); where y is undeclared
            if (params.head != null && args.head.dtype != null) {

                // int paramType = getType(params.head);
                // int argType = getType(args.head.dtype);

                // if (paramType != argType) {
                //     semanticError(exp.row, exp.col,"argument type mismatch in call to '" + exp.func + "'");
                // }
                Dec paramDec = params.head;
                Dec argDec = args.head.dtype;

                boolean argIsArray = false;

                // detect if argument is an array VARIABLE (not a[i])
                if (args.head instanceof VarExp) {
                    VarExp v = (VarExp) args.head;

                    // only SimpleVar represents passing entire array (a)
                    if (v.variable instanceof SimpleVar) {
                        Dec actualDec = table.lookup(((SimpleVar)v.variable).name);
                        argIsArray = actualDec instanceof ArrayDec;
                    }
                }

                // parameter is array if declared as int a[]
                boolean paramIsArray = paramDec instanceof ArrayDec;

                // case 1: array passed to non-array parameter
                if (argIsArray && !paramIsArray) {
                    semanticError(exp.row, exp.col,
                        "array argument passed to non-array parameter in call to '" + exp.func + "'");
                }
                // case 2: non-array passed to array parameter
                else if (!argIsArray && paramIsArray) {
                    semanticError(exp.row, exp.col,
                        "non-array argument passed to array parameter in call to '" + exp.func + "'");
                }
                // case 3: both are same structure, check type
                else {
                    int paramType = getType(paramDec);
                    int argType = getType(argDec);

                    if (paramType != argType) {
                        semanticError(exp.row, exp.col,
                            "argument type mismatch in call to '" + exp.func + "'");
                    }
                }
            }

            params = params.tail;
            args = args.tail;
        }

        // case where there are too many arguments or too few arguments
        while (args != null) {
            args.head.accept(this, level, false);
            args = args.tail;
        }

        if (params != null || args != null) {
            semanticError(exp.row, exp.col,"wrong number of arguments in call to '" + exp.func + "'");
        }

        // add(3,4) returns int, so the call expression has type int
        exp.dtype = new SimpleDec(0, 0, func.result, "");

        // after fighting many demons this had to be added for checkpoint 3
        exp.funcDec = func;
    }

    public void visit(CompoundExp exp, int level, boolean isAddr) {
        table.enterScope("the new (compound) block", level);
        if (exp.decs != null) {
            exp.decs.accept(this, level, false);
        }
        if (exp.exps != null) {
            exp.exps.accept(this, level, false);
        }
        table.exitScope("the (compound) block", level);
    }

    public void visit(IfExp exp, int level, boolean isAddr) {
        if (exp.test != null) {
            exp.test.accept(this, level, false);
            if (exp.test.dtype != null) {
                int testType = getType(exp.test.dtype);
                if (testType != NameTy.INTEGER && testType != NameTy.BOOLEAN) {
                    semanticError(exp.row, exp.col,"if condition has to evaluate to an integer or a boolean");
                }
            }
        }

        if (exp.thenpart != null) {
            exp.thenpart.accept(this, level, false);
        }

        if (exp.elsepart != null) {
            exp.elsepart.accept(this, level, false);
        }
        if (exp.thenpart != null) {
            exp.dtype = exp.thenpart.dtype;
        }
        
    }

    public void visit(IntExp exp, int level, boolean isAddr) {
        exp.dtype = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "");
    }

    public void visit(NilExp exp, int level, boolean isAddr) {
        // exp.dtype = new SimpleDec(0,0,new NameTy(0,0,NameTy.VOID),"");
    }

    public void visit(NameTy exp, int level, boolean isAddr) {}

    public void visit(ReturnExp exp, int level, boolean isAddr) {
        // return 5;
        // exp.exp =

        // return can be expression: return x + 5; -> needs to be checked
        if (exp.exp != null) {
            exp.exp.accept(this, level, false);
        }

        // or just a plain return;
        if (exp.exp == null || exp.exp instanceof NilExp) {
            if (currentFunctionReturnType.type != NameTy.VOID) {
                semanticError(exp.row, exp.col,"return without value in non-void function");
            }
            return;
        }

        if (exp.exp != null && exp.exp.dtype != null) {
            // what type is returned from the return call
            int returnType = getType(exp.exp.dtype);

            // compare with what function header claims
            if (returnType != currentFunctionReturnType.type) {
                semanticError(exp.row, exp.col,"return type mismatch");
            }
        }
    }

    public void visit(WhileExp exp, int level, boolean isAddr) {
        if (exp.test != null) {
            exp.test.accept(this, level, false);
            if (exp.test.dtype != null) {
                int testType = getType(exp.test.dtype);
                if (testType != NameTy.INTEGER && testType != NameTy.BOOLEAN) {
                    semanticError(exp.row, exp.col,"while condition has to evaluate to an integer or boolean");
                }
            }
        }
        
        if (exp.body != null) {
            exp.body.accept(this, level, false);
        }
    }

    public void visit(IndexVar exp, int level, boolean isAddr) {
        // x = a[2];
        // exp.name = "a"
        // exp.index = 2

        // what is the array declared as - arraydec, simpledec, functiondec
        Dec dec = table.lookup(exp.name);

        if (dec == null) {
            semanticError(exp.row, exp.col,"array '" + exp.name + "' not declared");
            return;
        }

        if (!(dec instanceof ArrayDec)) {
            semanticError(exp.row, exp.col,"'" + exp.name + "' is not an array");
        }

        // index itself can be an expression: a[y + 2], so run semantic analysis
        if (exp.index != null) {
            exp.index.accept(this, level, false);
        }

        if (exp.index != null && exp.index.dtype != null) {
            int indexType = getType(exp.index.dtype);

            // is the index an integer: bool b; a[b]; -> BAD
            if (indexType != NameTy.INTEGER) {
                semanticError(exp.row, exp.col,"array index must be integer");
            }
        }

        // IndexVar does not store dtype
        // type of a[i] is assigned in VarExp, which wraps this node
        // thus no dtype assignment is needed here

        // the type of a[i] should be equal the same type result
        // exp.dtype = dec;
    }

    public void visit(ErrorExp exp, int level, boolean isAddr) {}

    public void visit(ErrorDec exp, int level, boolean isAddr) {}

    public void visit(ErrorVar exp, int level, boolean isAddr) {}

    private int getType(Dec dec) {
        if (dec instanceof SimpleDec) {
            return ((SimpleDec) dec).typ.type;
        } else if (dec instanceof ArrayDec) {
            return ((ArrayDec) dec).typ.type;
        }
        return -1;
    }
}
