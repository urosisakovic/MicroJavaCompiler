// generated with ast extension for cup
// version 0.8
// 11/8/2021 19:22:8


package rs.ac.bg.etf.pp1.ast;

public class ReturnNoExpr extends Matched {

    public ReturnNoExpr () {
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
        buffer.append("ReturnNoExpr(\n");

        buffer.append(tab);
        buffer.append(") [ReturnNoExpr]");
        return buffer.toString();
    }
}
