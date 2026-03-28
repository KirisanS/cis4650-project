import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    int mainEntry, globalOffset, frameOffset = 0;
    public String fileName = "";

    int tempCount = 0;
    int emitLoc = 0;
    int highEmitLoc = 0;

    // Special Registers 
    int pc = 7;
    int gp = 6;
    int fp = 5;
    int ac = 0;
    int ac1 = 1;

    // stub

    public CodeGenerator(String fileName) {
        this.fileName = fileName;
    }

    public void visit(Absyn trees) {
        // Header
        emitComment("C-Minus Compilation to TM Code");
        emitComment("File: %s".formatted(fileName));
        
        // Prelude Generation
        emitComment("Standard prelude: ");

        emitRM("LD", gp, 0, ac, "load gp with maxaddress");
        emitRM("LDA", fp, 0, gp, "copy gp to fp");
        emitRM("ST", ac, 0, ac, "clear location 0");

        int savedLoc = emitSkip(1);

        emitComment("Jump around i/o routines here");
        emitComment("code for input routine");

        emitRM("ST", ac, -1, fp, "store return");
        emitRM("IN", ac, 0, 0, "input");
        emitRM("LD", pc, -1, fp, "return to caller");

        emitComment("code for output routine");

        emitRM("ST", ac, -1, fp, "store return");
        emitRM("LD", ac, -2, fp, "load output value");
        emitRO("OUT", ac, 0, 0, "output");
        emitRM("LD", pc, -1, fp, "return to caller");

        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", pc, savedLoc2, "jump around i/o code");
        emitRestore();

        emitComment("End of standard prelude");

        // Enter Code Generation
        ((DecList) trees).accept(this, 0, false);

        // Generate Finale Later 
    }

    private String newTemp() {
        tempCount++;
        return "t" + tempCount;
    }

    public void emitComment(String text) {
        System.out.printf("* %s\n", text);
    }

    public void emitRO(String op, int r, int s, int t, String c) {
        System.out.printf("%3d: %5s %d %d %d\t%s\n", emitLoc, op, r, s, t, c);
        ++emitLoc;
        if(highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitRM(String op, int r, int d, int s, String c) {
        System.out.printf("%3d: %5s %d %d(%d)\t%s\n", emitLoc, op, r, d, s, c);
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    private void emitRM_Abs(String op, int r, int a, String c) {
        System.out.printf("%3d: %5s %d %d(%d)\t%s\n", emitLoc, op, r, a - (emitLoc + 1), pc, c);
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    private int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
        return i;
    }

    private void emitBackup(int loc) {
        if (loc > highEmitLoc) {
            emitComment("BUG in emitBackup");
        }
        emitLoc = loc;
    }

    private void emitRestore() {
        emitLoc = highEmitLoc;
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
        
        emitComment("process function: " + exp.func);
        emitComment("jump around function body here");

        int savedLoc = emitSkip(1);

        if(exp.func.equals("main")) {
            mainEntry = emitLoc;
            globalOffset = frameOffset;
        }

        emitRM("ST", ac, -1, fp, "save return address");
        frameOffset = -2;

        if (exp.params != null) {
            exp.params.accept(this, level, false); 
        }

        if(exp.body != null) {
            exp.body.accept(this, level, false);
        }

        emitRM("LD", pc, -1, fp, "return to caller");

        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", pc, savedLoc2, "jump around fn body");
        emitRestore();

        emitComment("<- function declaration");
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {
        emitComment("processing local var: " + exp.name);
        exp.offset = frameOffset;
        frameOffset--;
    }

    public void visit(ArrayDec exp, int level, boolean isAddr) {
        emitComment("processing local var: " + exp.name);
        exp.offset = frameOffset;
        frameOffset -= exp.size;
    }

    public void visit(AssignExp exp, int level, boolean isAddr) {
        emitComment("-> op");

        exp.lhs.accept(this, level, true);

        // Pushing Left Side onto stack temporarily 
        emitRM("ST", ac, frameOffset, fp, "op: push left");

        exp.rhs.accept(this, level, false);

        // Load Left Side Back On 
        emitRM("LD", ac1, frameOffset, fp, "op: load left");
        
        emitRM("ST", ac, 0, ac1, "assign: store value");

        emitComment("<- op");
    }

    public void visit(OpExp exp, int level, boolean isAddr) {
        emitComment("-> op");

        if (exp.left != null) {
            exp.left.accept(this, level, false);
        }

        // Push LHS to stack
        emitRM("ST", ac, frameOffset, fp, "op: push left");

        if (exp.right != null) {
            exp.right.accept(this, level, false);
        }

        // Load LHS back in
        emitRM("LD", ac1, frameOffset, fp, "op: load left");

        switch (exp.op) {
            case OpExp.PLUS:        
                emitRO("ADD", ac, ac1, ac, "op +");
                break;
            case OpExp.MINUS:       

                break;
            case OpExp.TIMES:       

                break;
            case OpExp.DIVIDE:      

                break;
            case OpExp.LESSTHAN:    

                break;
            case OpExp.GREATERTHAN: 

                break;
            case OpExp.LESSEQUAL:   

                break;
            case OpExp.GREATEQUAL:  

                break;
            case OpExp.EQ:          

                break;
            case OpExp.NOTEQUAL:    

                break;
            case OpExp.AND:         

                break;
            case OpExp.OR:          

                break;
        }

        emitComment("<- op");
    }

    public void visit(VarExp exp, int level, boolean isAddr) {

        if (exp.variable instanceof SimpleVar) {
            SimpleVar var = (SimpleVar) exp.variable;
            emitComment("-> id");
            emitComment("looking up id: " + var.name);

            VarDec dec = (VarDec) exp.dtype;
            int offset  = dec.offset;
            int register = (dec.nestLevel == 0) ? gp : fp;


            if (isAddr) {
                emitRM("LDA", ac, offset, register, "load id address");
            } else {
                emitRM("LD", ac, offset, register, "load id value");
            }
            emitComment("<- id");
        } else if (exp.variable instanceof IndexVar) {
            IndexVar var = (IndexVar) exp.variable;
            emitComment("-> id");
            emitComment("looking up id: " + var.name);

            VarDec dec = (VarDec) exp.dtype;
            int offset  = dec.offset;
            int register = (dec.nestLevel == 0) ? gp : fp;

            var.index.accept(this, level, false);

            emitRM("LDA", ac1, offset, register, "load array address");
            emitRO("ADD", ac, ac1, ac, "compute element address");

            if (!isAddr) {
                emitRM("LD", ac, 0, ac, "load element value");
            }
            emitComment("<- id");
        }
    }
        
    public void visit(IntExp exp, int level, boolean isAddr) {
        emitComment("-> constant");
        emitRM("LDC", ac, exp.value, 0, "load const");
        emitComment("<- constant");
    }

    public void visit(BoolExp exp, int level, boolean isAddr) {
        emitComment("-> constant");
        emitRM("LDC", ac, exp.value ? 1 : 0, 0, "load bool const");
        emitComment("<- constant");
    }

    public void visit(CompoundExp exp, int level, boolean isAddr) {
        emitComment("-> compound statement");
        if (exp.decs != null) {
            exp.decs.accept(this, level, false);
        } if (exp.exps != null) {
            exp.exps.accept(this, level, false);
        }
        emitComment("<- compound statement");
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