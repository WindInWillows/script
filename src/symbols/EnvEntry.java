package symbols;

public class EnvEntry {
    public final Type  type;
    public final int   stacklevel;
    public final int   offset;

    public EnvEntry(Type t,int sl,int o){
        type = t;
        stacklevel = sl;
        offset = o;
    }

}