// generated with ast extension for cup
// version 0.8
// 16/8/2021 22:50:51


package rs.ac.bg.etf.pp1.ast;

public class StmtContinue extends Statement {

    public StmtContinue () {
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
        buffer.append("StmtContinue(\n");

        buffer.append(tab);
        buffer.append(") [StmtContinue]");
        return buffer.toString();
    }
}
