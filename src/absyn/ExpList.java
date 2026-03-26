package absyn;

public class ExpList {

    public Exp head;
    public ExpList tail;

    public ExpList(Exp head, ExpList tail) {
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int offset, boolean isAddr) {
        visitor.visit(this, offset, isAddr);
    }
}