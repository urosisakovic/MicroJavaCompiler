// generated with ast extension for cup
// version 0.8
// 11/8/2021 21:47:25


package rs.ac.bg.etf.pp1.ast;

public class RelopGT extends Relop {

    public RelopGT () {
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("RelopGT(\n");

        buffer.append(tab);
        buffer.append(") [RelopGT]");
        return buffer.toString();
    }
}
