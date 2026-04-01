import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    int mainEntry = -1;
    int globalOffset, frameOffset = 0;
    public String fileName = "";

    int tempCount = 0;
    int emitLoc = 0;
    int highEmitLoc = 0;
    int currentNestLevel = 0;

    int ofpFO = 0;
    int refFO = -1;
    int initFO = -2;

    int inputEntry = 4;
    int outputEntry = 7;

    // Special Registers 
    int pc = 7;
    int gp = 6;
    int fp = 5;
    int ac = 0;
    int ac1 = 1;

    // Prototype Fix Stuff
    private java.util.Map<String, Integer> functionAddressMap = new java.util.HashMap<>();
    private java.util.List<Object[]> deferredFunctionCalls = new java.util.ArrayList<>();


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
        emitRO("IN", ac, 0, 0, "input");
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

        for (Object[] deferred : deferredFunctionCalls) {
            int callLoc = (Integer) deferred[0];
            String funcName = (String) deferred[1];
            int funcAddr = functionAddressMap.get(funcName);
            emitBackup(callLoc);
            emitRM_Abs("LDA", pc, funcAddr, "jump to func loc");
            emitRestore();
        }

        // Generate Finale Later 

        if(mainEntry == -1) {
            System.err.println("Error: Main Not Found, Exiting Code Generation");
            return; // STOP codegen completely
        } else {
            emitComment("Finale");
            emitRM("ST", fp, globalOffset+ofpFO, fp, "push ofp");
            emitRM("LDA", fp, globalOffset, fp, "push frame");
            emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
            emitRM_Abs("LDA", pc, mainEntry, "jump to main loc");
            emitRM("LD", fp, ofpFO, fp, "pop frame");
            emitComment("End of execution.");
            emitRO("HALT", 0, 0, 0, "");
        }

    }

    private String newTemp() {
        tempCount++;
        return "t" + tempCount;
    }

    public void emitComment(String text) {
        System.out.printf("* %s\n", text);
    }

    public void emitRO(String op, int r, int s, int t, String c) {
        System.out.printf("%3d: %5s %d,%d,%d\t%s\n", emitLoc, op, r, s, t, c);
        ++emitLoc;
        if(highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitRM(String op, int r, int d, int s, String c) {
        System.out.printf("%3d: %5s %d,%d(%d)\t%s\n", emitLoc, op, r, d, s, c);
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    private void emitRM_Abs(String op, int r, int a, String c) {
        System.out.printf("%3d: %5s %d,%d(%d)\t%s\n", emitLoc, op, r, a - (emitLoc + 1), pc, c);
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

        if (exp.body instanceof NilExp) {
            return;
        }

        int savedOffset = frameOffset;
        
        emitComment("process function: " + exp.func);
        emitComment("jump around function body here");

        int savedLoc = emitSkip(1);
        exp.functionEntry = emitLoc;

        functionAddressMap.put(exp.func, emitLoc);

        if(exp.func.equals("main")) {
            mainEntry = emitLoc;
            globalOffset = frameOffset;
        }

        emitRM("ST", ac, refFO, fp, "save return address");
        frameOffset = initFO;
        currentNestLevel++;

        if (exp.params != null) {
            exp.params.accept(this, level, false); 
        }

        if(exp.body != null) {
            exp.body.accept(this, level, false);
        }

        currentNestLevel--;

        emitRM("LD", pc, refFO, fp, "return to caller");

        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", pc, savedLoc2, "jump around fn body");
        emitRestore();

        frameOffset = savedOffset;
        emitComment("<- function declaration");
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {

        emitComment("processing local var: " + exp.name);
        exp.offset = frameOffset;
        exp.nestLevel = currentNestLevel;
        frameOffset--;
    }

    public void visit(ArrayDec exp, int level, boolean isAddr) {
        if (currentNestLevel == 0) {
            emitComment("allocating global var: " + exp.name);
            emitComment("<- vardecl");
        } else {
            emitComment("processing local var: " + exp.name);
        }
        if (exp.size == 0) {
            exp.offset = frameOffset;
            exp.nestLevel = currentNestLevel;
            frameOffset -= 2;
        } else {
            exp.offset = frameOffset;
            exp.nestLevel = currentNestLevel;
            frameOffset -= exp.size;
        }
    }

    public void visit(AssignExp exp, int level, boolean isAddr) {
        emitComment("-> op");

        exp.lhs.accept(this, level, true);

        // Saving offset because RHS is nested and we need a new spot to put whatever RHS equates to
        int tempOffset = frameOffset;
        frameOffset--;

        // Pushing Left Side onto stack temporarily 
        emitRM("ST", ac, tempOffset, fp, "op: push left");

        exp.rhs.accept(this, level, false);

        // Load Left Side Back On 
        emitRM("LD", ac1, tempOffset, fp, "op: load left");
        
        emitRM("ST", ac, 0, ac1, "assign: store value");

        frameOffset++;
        emitComment("<- op");
    }

    public void visit(OpExp exp, int level, boolean isAddr) {
        emitComment("-> op");

        // handle UMINUS first
        if (exp.op == OpExp.UMINUS) {
            if (exp.right != null) {
                exp.right.accept(this, level, false);
            }

            emitRM("LDC", ac1, 0, 0, "load 0");
            emitRO("SUB", ac, ac1, ac, "op uminus");

            emitComment("<- op");
            return;
        }

        // normal logic after
        if (exp.left != null) {
            exp.left.accept(this, level, false);
        }

        // Push LHS onto stack 
        emitRM("ST", ac, frameOffset, fp, "op: push left");
        frameOffset--;

        // Get RHS 
        if (exp.right != null) {
            exp.right.accept(this, level, false);
        }

        // Load LHS back in onto the second ac1 register
        frameOffset++;
        emitRM("LD", ac1, frameOffset, fp, "op: load left");

        switch (exp.op) {
            case OpExp.PLUS:        
                emitRO("ADD", ac, ac1, ac, "op +");
                break;
            case OpExp.MINUS:       
                emitRO("SUB", ac, ac1, ac, "op -");
                break;
            case OpExp.TIMES:       
                emitRO("MUL", ac, ac1, ac, "op *");
                break;
            case OpExp.DIVIDE:      
                emitRO("DIV", ac, ac1, ac, "op /");
                break;
            case OpExp.LESSTHAN:    
                // Do X - Y, if Less Than 0 jump to final line. 
                emitRO("SUB", ac, ac1, ac, "op <");
                emitRM("JLT", ac, 2, pc, "br if true");
                // Otherwise we propogate the false case, and skip the final line
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jump");
                emitRM("LDC", ac, 1, 0, "true case");
                break;
            case OpExp.GREATERTHAN: 
                emitRO("SUB", ac, ac1, ac, "op >");
                emitRM("JGT", ac, 2, pc, "br if true");
                emitRM("LDC", ac, 0, 0,  "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jmp");
                emitRM("LDC", ac, 1, 0,  "true case");
                break;
            case OpExp.LESSEQUAL:   
                emitRO("SUB", ac, ac1, ac, "op <=");
                emitRM("JLE", ac, 2, pc, "br if true");
                emitRM("LDC", ac, 0, 0,  "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jmp");
                emitRM("LDC", ac, 1, 0,  "true case");
                break;
            case OpExp.GREATEQUAL:  
                emitRO("SUB", ac, ac1, ac, "op >=");
                emitRM("JGE", ac, 2, pc, "br if true");
                emitRM("LDC", ac, 0, 0,  "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jmp");
                emitRM("LDC", ac, 1, 0,  "true case");
                break;
            case OpExp.EQ:          
                emitRO("SUB", ac, ac1, ac, "op ==");
                emitRM("JEQ", ac, 2, pc, "br if true");
                emitRM("LDC", ac, 0, 0,  "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jmp");
                emitRM("LDC", ac, 1, 0,  "true case");
                break;
            case OpExp.NOTEQUAL:    
                emitRO("SUB", ac, ac1, ac, "op !=");
                emitRM("JNE", ac, 2, pc, "br if true");
                emitRM("LDC", ac, 0, 0,  "false case");
                emitRM("LDA", pc, 1, pc, "unconditional jmp");
                emitRM("LDC", ac, 1, 0,  "true case");
                break;
            case OpExp.AND:
                emitRO("MUL", ac, ac1, ac, "op &&");
                break;
            case OpExp.OR:
                emitRO("ADD", ac, ac1, ac, "op ||");
                emitRM("JGT", ac, 2, pc, "br if true");
                emitRM("LDC", ac, 0, 0, "false case");
                emitRM("LDA", pc, 1, pc, "skip true");
                emitRM("LDC", ac, 1, 0, "true case");
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

            boolean isParam = (dec instanceof ArrayDec && ((ArrayDec)dec).size == 0);

            if (isParam) {
                emitRM("LD", ac, offset, register, "load id value");
            } else {
                emitRM("LDA", ac, offset, register, "load id address");
            }

            int savedOffset = frameOffset;
            frameOffset--;
            emitRM("ST", ac, savedOffset, fp, "store array addr");

            var.index.accept(this, level, false);

            int indexOffset = frameOffset;
            frameOffset--;
            emitRM("ST", ac, indexOffset, fp, "store index");

            emitRM("JLT", ac, 1, pc, "skip if index < 0");
            emitRM("LDA", pc, 3, pc, "index ok, jump over error");
            emitRM("LDC", ac, -1000000, 0, "error: out of range below");
            emitRO("OUT", ac, 0, 0, "print error");
            emitRO("HALT", 0, 0, 0, "halt: index out of range below");

            if (isParam) {
                emitRM("LD", ac1, offset - 1, register, "load param array size");
            } else {
                emitRM("LDC", ac1, ((ArrayDec)dec).size, 0, "load array size");
            }

            emitRM("LD", ac, indexOffset, fp, "reload index");
            emitRO("SUB", ac, ac, ac1, "index - size");
            emitRM("JLT", ac, 3, pc, "skip if index < size");
            emitRM("LDC", ac, -2000000, 0, "error: out of range above");
            emitRO("OUT", ac, 0, 0, "print error");
            emitRO("HALT", 0, 0, 0, "halt: index out of range above");

            emitRM("LD", ac, indexOffset, fp, "reload index");
            emitRM("LD", ac1, savedOffset, fp, "load array base addr");
            emitRO("SUB", ac, ac1, ac, "base is at top of array");
            frameOffset += 2;

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
        int savedFrameOffset = frameOffset;
        if (exp.decs != null) {
            exp.decs.accept(this, level, false);
        } if (exp.exps != null) {
            exp.exps.accept(this, level, false);
        }
        emitComment("<- compound statement");
        frameOffset = savedFrameOffset;
    }

    public void visit(IfExp exp, int level, boolean isAddr) { 
        
        emitComment("-> if");

        if (exp.test != null) {
            exp.test.accept(this, level, false);
        }

        // Save the location of where we might skip to if conditional is false
        int savedLoc = emitSkip(1);

        if(exp.thenpart != null) {
            exp.thenpart.accept(this, level, false);
        }

        if(exp.elsepart != null && !(exp.elsepart instanceof NilExp)) {

            // Skipping one to go over else block later
            int savedLoc2 = emitSkip(1);

            // We know where else starts now 
            int elseLoc = emitSkip(0);
            emitBackup(savedLoc);
            emitRM_Abs("JEQ", ac, elseLoc, "jump to else block");
            emitRestore();

            exp.elsepart.accept(this, level, false);

            // Jumping to after else 
            int afterIfLoc = emitSkip(0);
            emitBackup(savedLoc2);
            emitRM_Abs("LDA", pc, afterIfLoc, "jump over else");
            emitRestore();

        } else {

            int afterIfLoc = emitSkip(0);
            emitBackup(savedLoc);
            emitRM_Abs("JEQ", ac, afterIfLoc, "jump to end of if block");
            emitRestore();

            emitRM("LDA", pc, 0, pc, "jump to end");
        }

        emitComment("<- if");
    }

    public void visit(WhileExp exp, int level, boolean isAddr) { 
        
        emitComment("-> while");
        emitComment("while: jump after body comes back here");

        int tempLoc = emitLoc;

        if (exp.test != null) {
            exp.test.accept(this, level, false);
        }

        // Save the location of where we might skip to if conditional is false
        int savedLoc = emitSkip(1);
        emitComment("while: jump to end belongs here");

        if (exp.body != null) {
            exp.body.accept(this, level, false);
        }

        // Jump back to the beginning of the while loop
        emitRM_Abs("LDA", pc, tempLoc, "while: absolute jump to test");

        // This is where while loop ends so we save this. 
        int afterLoc = emitSkip(0);

        // While Ends here, so we go back to beginning of loop  and fill in the jump location. 
        emitBackup(savedLoc);
        emitRM_Abs("JEQ", ac, afterLoc, "while: jump to end");

        // and then go back to where we were to continue code generation as usual
        emitRestore();

        emitComment("<- while");
    }
    public void visit(ReturnExp exp, int level, boolean isAddr) {

        emitComment("-> return");

        if (exp.exp != null) {
            exp.exp.accept(this, level, false);
        }

        emitRM("LD", pc, refFO, fp, "return to caller");
        emitComment("<- return");
 

    }
    public void visit(CallExp exp, int level, boolean isAddr) { 

        emitComment("-> call of function: " + exp.func);

        FunctionDec func = exp.funcDec;

        // Save the current frame location for THIS call
        int savedFrameOffset = frameOffset;

        if (exp.args != null) {
            // reverse args back to declaration order
            ExpList reversed = null;
            ExpList arguments = exp.args;
            while (arguments != null) {
                reversed = new ExpList(arguments.head, reversed);
                arguments = arguments.tail;
            }

            // Evaluate each argument and store at calculated positions
            int argOffset = savedFrameOffset + initFO;
            while (reversed != null) {
                if (reversed.head != null) {
                    boolean isArrayArgument = false;
                    boolean isParamArgument = false;

                    if(reversed.head instanceof VarExp) {
                        VarExp v = (VarExp) reversed.head;

                        if(v.variable instanceof SimpleVar && v.dtype instanceof ArrayDec) {
                            isArrayArgument = true;
                            isParamArgument = ((ArrayDec) v.dtype).size == 0;
                        }
                    }

                    // Allocate space for this argument's storage BEFORE evaluation
                    // This moves frameOffset down so nested calls don't overlap
                    int argStorageOffset = argOffset;
                    frameOffset = argOffset - 1;  // Reserve space below this arg
                    
                    reversed.head.accept(this, level, isArrayArgument && !isParamArgument);
                    emitRM("ST", ac, argStorageOffset, fp, "store arg val");
                    argOffset--;

                    if (isArrayArgument) {
                        frameOffset--;  // Reserve space for array size too
                        if (isParamArgument) {
                            VarExp v = (VarExp) reversed.head;
                            VarDec paramDec = (VarDec) v.dtype;
                            int reg = (paramDec.nestLevel == 0) ? gp : fp;
                            emitRM("LD", ac, paramDec.offset - 1, reg, "load param array size");
                        } else {
                            VarExp v = (VarExp) reversed.head;
                            ArrayDec arrDec = (ArrayDec) v.dtype;
                            emitRM("LDC", ac, arrDec.size, 0, "load real array size");
                        }
                        emitRM("ST", ac, argOffset, fp, "store array size");
                        argOffset--;
                    }
                }
                reversed = reversed.tail;
            }
        }

        // Use savedFrameOffset for the frame push
        emitRM("ST", fp, savedFrameOffset + ofpFO, fp, "push ofp");
        emitRM("LDA", fp, savedFrameOffset, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with return pointer");

        if(exp.func.equals("input")) {
            emitRM_Abs("LDA", pc, inputEntry, "jump to input loc");
        } else if (exp.func.equals("output")) {
            emitRM_Abs("LDA", pc, outputEntry, "jump to output loc");
        } else {
            int callLoc = emitSkip(1);
            deferredFunctionCalls.add(new Object[]{callLoc, exp.func});
        }

        emitRM("LD", fp, ofpFO, fp, "pop frame");
        emitComment("<- call");
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