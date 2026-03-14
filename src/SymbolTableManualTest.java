import absyn.*;

public class SymbolTableManualTest {
    public static void main(String[] args) {

        SymbolTable table = new SymbolTable();

        // Enter global scope
        table.enterScope("the global scope", 0);

        // int i;
        SimpleDec i = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "i");
        table.insert("i", i);

        // int j;
        SimpleDec j = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "j");
        table.insert("j", j);

        // int f(int size)
        VarDecList params = new VarDecList(
            new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "size"), null);

        FunctionDec f = new FunctionDec(
            0, 0, new NameTy(0, 0, NameTy.INTEGER), "f", params, null);

        table.insert("f", f);

        // Enter function scope
        table.enterScope("the scope for function f", 1);

        SimpleDec i2 = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "i");
        table.insert("i", i2);

        SimpleDec temp = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "temp");
        table.insert("temp", temp);

        SimpleDec size = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "size");
        table.insert("size", size);

        table.enterScope("a new block", 2);

        SimpleDec j2 = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INTEGER), "j");
        table.insert("j", j2);

        table.exitScope("the block", 2);

        table.enterScope("a new block", 2);

        SimpleDec j3 = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.BOOLEAN), "j");
        table.insert("j", j3);

        table.exitScope("the block", 2);

        table.exitScope("the function scope", 1);
        table.exitScope("the global scope", 0);
    }
}