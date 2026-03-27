import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    int mainEntry, globalOffset;

    int tempCount = 0;
    // stub

    public void visit(Absyn trees) {

    }

    private String newTemp() {
        tempCount++;
        return "t" + tempCount;
    }

    public void emitCode(String text) {
        System.out.println(text);
    }

    /* LISTS */
    public void visit(DecList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false);
            }
            exp = exp.tail;
        }
        
    }

    public void visit(ExpList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false);
            }
            exp = exp.tail;
        }
    }

    public void visit(VarDecList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false);
            }
            exp = exp.tail;
        }
    }

    /* Declarations */ 
    public void visit(FunctionDec exp, int level, boolean isAddr) {
        if(exp.body != null) {
            exp.body.accept(this, level, false);
        }
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {

    }

    public void visit(ArrayDec exp, int level, boolean isAddr) {

    }

    public void visit(AssignExp exp, int level, boolean isAddr) {

        exp.rhs.accept(this, level, false);
        exp.lhs.accept(this, level, false);

        exp.temp = exp.lhs.temp;

        emitCode(exp.lhs.temp + "=" + exp.rhs.temp);
    }

    public void visit(OpExp exp, int level, boolean isAddr) {

        if (exp.left != null) {
            exp.left.accept(this, level, false);
        }

        if (exp.right != null) {
            exp.right.accept(this, level, false);
        }

        exp.temp = newTemp();

        if (exp.op == OpExp.UMINUS) {
            emitCode(exp.temp + " = -" + exp.right.temp);
        }

        if (exp.op == OpExp.NOT) {
            emitCode(exp.temp + " = !" + exp.right.temp);
        }

        String op = ""; 
        switch (exp.op) {
            case OpExp.PLUS:        
                op = " + ";  
                break;
            case OpExp.MINUS:       
                op = " - ";  
                break;
            case OpExp.TIMES:       
                op = " * ";  
                break;
            case OpExp.DIVIDE:      
                op = " / ";  
                break;
            case OpExp.LESSTHAN:    
                op = " < ";  
                break;
            case OpExp.GREATERTHAN: 
                op = " > ";  
                break;
            case OpExp.LESSEQUAL:   
                op = " <= "; 
                break;
            case OpExp.GREATEQUAL:  
                op = " >= ";
                break;
            case OpExp.EQ:          
                op = " == "; 
                break;
            case OpExp.NOTEQUAL:    
                op = " != "; 
                break;
            case OpExp.AND:         
                op = " && "; 
                break;
            case OpExp.OR:          
                op = " || "; 
                break;
        }

        emitCode(exp.temp + " = " + exp.left.temp + op + exp.right.temp);
    }

    public void visit(VarExp exp, int level, boolean isAddr) {

        
        if (exp.variable instanceof IndexVar) {
            IndexVar var = (IndexVar) exp.variable;
            var.index.accept(this, level, false);
            exp.temp = var.name + "[" + var.index.temp + "]";
        } else if (exp.variable instanceof SimpleVar) {
            exp.temp = ((SimpleVar) exp.variable).name;
        } 
    }
        
    public void visit(IntExp exp, int level, boolean isAddr) {
        exp.temp = String.valueOf(exp.value);
    }

    public void visit(BoolExp exp, int level, boolean isAddr) {
        exp.temp = exp.value ? "1" : "0";
    }

    public void visit(CompoundExp exp, int level, boolean isAddr) {
        if (exp.decs != null) {
            exp.decs.accept(this, level, false);
        } if (exp.exps != null) {
            exp.exps.accept(this, level, false);
        }
    }

    public void visit(IfExp exp, int level, boolean isAddr) { 

    }
    public void visit(WhileExp exp, int level, boolean isAddr) { 

    }
    public void visit(ReturnExp exp, int level, boolean isAddr) { 

    }
    public void visit(CallExp exp, int level, boolean isAddr) { 

    }
    public void visit(NilExp exp, int level, boolean isAddr) { 

    }
    public void visit(NameTy exp, int level, boolean isAddr) { 

    }
    public void visit(SimpleVar exp, int level, boolean isAddr) { 

    }
    public void visit(IndexVar exp, int level, boolean isAddr) { 

    }
    public void visit(ErrorExp exp, int level, boolean isAddr) { 

    }
    public void visit(ErrorDec exp, int level, boolean isAddr) { 

    }
    public void visit(ErrorVar exp, int level, boolean isAddr) { 
        
    }
}