// generated with ast extension for cup
// version 0.8
// 18/8/2021 16:44:4


package rs.ac.bg.etf.pp1.ast;

public class MethodTypeDeclDerived4 extends MethodTypeDecl {

    private MethTypeName MethTypeName;
    private FormParams FormParams;

    public MethodTypeDeclDerived4 (MethTypeName MethTypeName, FormParams FormParams) {
        this.MethTypeName=MethTypeName;
        if(MethTypeName!=null) MethTypeName.setParent(this);
        this.FormParams=FormParams;
        if(FormParams!=null) FormParams.setParent(this);
    }

    public MethTypeName getMethTypeName() {
        return MethTypeName;
    }

    public void setMethTypeName(MethTypeName MethTypeName) {
        this.MethTypeName=MethTypeName;
    }

    public FormParams getFormParams() {
        return FormParams;
    }

    public void setFormParams(FormParams FormParams) {
        this.FormParams=FormParams;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethTypeName!=null) MethTypeName.accept(visitor);
        if(FormParams!=null) FormParams.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethTypeName!=null) MethTypeName.traverseTopDown(visitor);
        if(FormParams!=null) FormParams.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethTypeName!=null) MethTypeName.traverseBottomUp(visitor);
        if(FormParams!=null) FormParams.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodTypeDeclDerived4(\n");

        if(MethTypeName!=null)
            buffer.append(MethTypeName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(FormParams!=null)
            buffer.append(FormParams.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodTypeDeclDerived4]");
        return buffer.toString();
    }
}
