package inter.expr;

import lexer.Token;
import symbols.Struct;

/*
 * new<type>;
 */
public class New extends Op {
    public New(Token tok,Struct type){
        super(tok,type);
    }

    @Override
    public String toString(){
        return "new " + type.toString();
    }
    @Override
    boolean isChangeable(){
        return true;
    }

    @Override
    public Constant getValue(){
        return new StructConst((Struct)type);
    }
}


