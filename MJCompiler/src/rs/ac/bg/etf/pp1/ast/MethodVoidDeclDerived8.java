// generated with ast extension for cup
// version 0.8
// 18/8/2021 19:7:56


package rs.ac.bg.etf.pp1.ast;

public class MethodVoidDeclDerived8 extends MethodVoidDecl {

    private MethVoidName MethVoidName;

    public MethodVoidDeclDerived8 (MethVoidName MethVoidName) {
        this.MethVoidName=MethVoidName;
        if(MethVoidName!=null) MethVoidName.setParent(this);
    }

    public MethVoidName getMethVoidName() {
        return MethVoidName;
    }

    public void setMethVoidName(MethVoidName MethVoidName) {
        this.MethVoidName=MethVoidName;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethVoidName!=null) MethVoidName.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethVoidName!=null) MethVoidName.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethVoidName!=null) MethVoidName.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodVoidDeclDerived8(\n");

        if(MethVoidName!=null)
            buffer.append(MethVoidName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodVoidDeclDerived8]");
        return buffer.toString();
    }
}
