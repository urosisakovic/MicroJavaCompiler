// generated with ast extension for cup
// version 0.8
// 18/8/2021 16:44:4


package rs.ac.bg.etf.pp1.ast;

public class ParamError extends FormParam {

    public ParamError () {
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
        buffer.append("ParamError(\n");

        buffer.append(tab);
        buffer.append(") [ParamError]");
        return buffer.toString();
    }
}
