
struct Expr {
    @int
    def virtual int getValue();
}

struct Arith:Expr{
    Expr e1;
    Expr e2;
    def void init(Expr e1,Expr e2){
        this.e1 = e1;
        this.e2 = e2;
    }
}

struct Add:Arith{
    def override int getValue(){
        return this.e1.getValue()+this.e2.getValue();
    }
}

struct Sub:Arith{
    def override int getValue(){
        return this.e1.getValue()-this.e2.getValue();
    } 
}

struct Mult:Arith{
    def override int getValue(){
        return this.e1.getValue()*this.e2.getValue();
    } 
}

struct Div:Arith{
    def override int getValue(){
        return this.e1.getValue()/this.e2.getValue();
    } 
}

struct Const:Expr{
    int value;
    def void setValue(int v){
        this.value = v;
    }

    def override int getValue(){
        return this.value;
    }
}

struct Token{
    int tag;

    @string
    def virtual string toString(){
        return (string)this.tag;
    }
    
    def void setTag(int d){
        this.tag = d;
    }
}

struct Num:Token{
    int value;

    def override string toString(){
        return (string)this.value;
    }

    def void init(int value){
        this.value = value;
        this.setTag(256);
    }
}

def bool isDigital(char c){
    return c <= '9' && c >= '0';
}

struct lexer{
    int no;
    char peek;
    string poly;
    def void init(string poly){
        this.poly = poly;
        this.peek = ' ';
        this.no = 0;
    }

    def void readch(){

        if(this.no >= strlen(this.poly)){
            this.peek = 0;
        } else { 
            this.peek = this.poly[this.no];
            this.no++;
        }
    }

    def Token scan(){
        Token t;

        while(this.peek == ' '){
            this.readch();
        }
        char c = this.peek;
        if(isDigital(c)){
            int i = 0;
            do{
                i *= 10;
                i += (c-'0');
                this.readch();
                c = this.peek;
            }while(isDigital(c));
            Num n ;
            n.init(i);
            return n;
        }
        t.setTag(c);
        this.peek = ' ';
        return t;
   
    }
}

struct parser{
    lexer lex;
    Token look;

    def void init(lexer l){
        this.lex = l;
    }

    def void next(){
        this.look = this.lex.scan();
    }


    def Expr term();
    def Expr mult();
    def Expr add();
    def Expr expr(){
        this.next();
        return this.add();
    }
}

def Expr parser.term(){
    switch(this.look.tag){
    case '(':
        this.next();
        Expr e = this.add();
        if(this.look.tag != ')'){
            println("`(' mismatched");
        } else {
            this.next();
        }
        
        return e;
    case 256:
        Const e;
        e.setValue(((Num)this.look).value);
        this.next();
        return e;
    default:
        println("Unknown token `" + this.look + "' not found");
    }
}

def Expr parser.mult(){
    Expr e = this.term();
    while(this.look.tag == '*' || this.look.tag == '/'){
        if(this.look.tag == '*'){
            this.next();
            Mult m;
            m.init(e,this.term());
            e = m;
        } else {
            this.next();
            Div d;
            d.init(e,this.term());
            e = d;
        }
    }
    return e;
}

def Expr parser.add(){
    Expr e = this.mult();
    while(this.look.tag == '+' || this.look.tag == '-'){
        if(this.look.tag == '+'){
            this.next();
            Add a;
            a.init(e,this.mult());
            e = a;
        } else {
            this.next();
            Sub s;
            s.init(e,this.mult());
            e = s;
        }
    }
    return e;
}
