package inter;

import runtime.*;

public class Break extends Stmt {
    static public  final Throwable BreakCause = new Throwable();
    Stmt stmt;
    public final int sizeOfStack;
    public Break(int s){
        if( Stmt.Enclosing == Stmt.Null )
            error("unenclosed break");
        stmt = Stmt.Enclosing;
        sizeOfStack = s;
    }

    public void run(){
        /*
         * I *KNOW* it is wrong use of exception
         * But it works well.
         * Maybe I will change the virtual machine.
         */
        for(int i = 0 ; i < sizeOfStack;i++)
            VarTable.popTop();

        throw new RuntimeException(BreakCause);
    }
}