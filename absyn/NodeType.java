package absyn;

public class NodeType {
    public Dec dec;
    public int level;
    public String name;

    public NodeType(String name, Dec dec, int level) {
         this.name = name;
        this.dec = dec;
        this.level = level;
    }
}