package inter.stmt;

import runtime.*;
import inter.util.Node;

public class Stmt extends Node{
    public Stmt()
    {}

    public void run(){
        
    }

    public static final Stmt Null = new Stmt();
    public static final Stmt RecoverStack = new Stmt(){
        public void run(){
            VarTable.popTop();
        }
        public String toString(){
            return "RecoverStack\n";
        }
        /*
            void emitBinaryCode(BinaryCode x){
                x.emit(POP_STACK);
            }
        */
    };
    public static final Stmt PushStack = new Stmt(){
        public void run(){
            VarTable.pushTop();
        }
        public String toString(){
            return "PushStack\n";
        }
        /*
            void emitBinaryCode(BinaryCode x){
                x.emit(PUSH_STACK);
            }
        */
    };

    public String toString(){
        return this.getClass().getName() + "\n";
    }

    public Stmt optimize(){
        return this;
    }

    public static Stmt Enclosing        = Null;
    public static Stmt BreakEnclosing   = Null; 
}