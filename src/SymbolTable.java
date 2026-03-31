import java.util.*;
import absyn.*;

public class SymbolTable {

    private HashMap<String, ArrayList<NodeType>> table; 
    private int currentLevel;
    final static int SPACES = 4;
    boolean print;

    public SymbolTable(boolean print) {
        table = new HashMap<>();
        currentLevel = -1;
        this.print = print;
    }

    private void indent(int level) {
        for( int i = 0; i < level * SPACES; i++ ) if (print) System.out.print( " " );
    }


    public void enterScope(String scopeName, int level) {
        indent(level);
        currentLevel++;
        if (print) System.out.println("Entering " + scopeName);
    }

    public void exitScope(String scopeName, int level) {
        //print scope
        printCurrentScope(level);
        indent(level);
        if (print) System.out.println("Leaving " + scopeName);
        deleteScope(currentLevel);
        currentLevel--;
    }

    public boolean insert(String name, Dec dec) {
        if (inCurrentScope(name)) {
            return false;
        }

        // Either Return existing bucket list or default to new bucket list
        ArrayList<NodeType> list = table.getOrDefault(name, new ArrayList<NodeType>());
        list.add(0, new NodeType(name, dec, currentLevel));
        table.put(name, list);

        return true;
    }

    public Dec lookup(String name) {
        ArrayList<NodeType> list = table.get(name);
        if (list == null) {
            return null;
        }
        
        return list.get(0).dec;
    }

    public boolean inCurrentScope(String name) {
        ArrayList<NodeType> list = table.get(name);
        if (list == null) {
            return false;
        }

        if (list.get(0).level == currentLevel) {
            return true;
        }
        return false;
    }

    public void deleteScope(int level) {
        Iterator<Map.Entry<String, ArrayList<NodeType>>> iter = table.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<NodeType>> entry = iter.next();
            ArrayList<NodeType> list = entry.getValue();

            list.removeIf(node -> (node.level == level));

            // Delete entire entry if it is empty 
            if (list.isEmpty()) {
                iter.remove();
            }
        }
    }

    private void printCurrentScope(int level) {
        level++;

        for (Map.Entry<String, ArrayList<NodeType>> entry : table.entrySet()) {
            ArrayList<NodeType> list = entry.getValue();

            for (NodeType node : list) {
                if (node.level == currentLevel) {
                    indent(level);
                    System.out.print(node.name + ": ");
                    printDecType(node.dec);
                }
            }
        }
    }

    private void printDecType(Dec dec) {
        if (dec instanceof FunctionDec) {
            FunctionDec funDec = (FunctionDec) dec;
            System.out.print("(");
            VarDecList params = funDec.params;

            while (params != null) {
                if (params.head instanceof SimpleDec) {
                    printType(((SimpleDec) params.head).typ);
                } else if (params.head instanceof ArrayDec) {
                    ArrayDec arr = (ArrayDec) params.head;
                    printType(arr.typ);
                    System.out.print("[" + arr.size + "]");
                } 
                if (params.tail != null) {
                    System.out.print(", ");
                }
                params = params.tail;
            }
            System.out.print(") -> ");
            printType(((FunctionDec) dec).result);
        } else if (dec instanceof ArrayDec) {
            // printType(((ArrayDec) dec).typ);
            ArrayDec arr = (ArrayDec) dec;
            printType(arr.typ);
            System.out.print("[" + arr.size + "]");
        } else if (dec instanceof SimpleDec) {
            printType(((SimpleDec) dec).typ);
        } else if (dec instanceof FunctionDec) {
            printType(((FunctionDec) dec).result);
        }

        if (print) System.out.println("");
    }

    private void printType(NameTy type) {

        switch (type.type) {
            case NameTy.INTEGER: 
                System.out.print("int");
                break;
            case NameTy.BOOLEAN: 
                System.out.print("bool");
                break;
            case NameTy.VOID: 
                System.out.print("void");
                break;
            case NameTy.ERROR:
                System.out.print("error");
                break;
        }
    }



}