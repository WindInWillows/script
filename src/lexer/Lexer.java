package lexer;


import main.Main;
import runtime.Dictionary;
import symbols.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

class Info {
    InputStreamReader in;

    int         peek;
    String      filename;
    int         line;
    String      path;

    Info(InputStreamReader i,int p,String f,int l,String pt){
        in = i;
        peek = p;
        filename = f;
        line = l;
        path = pt;
    }
}

public class Lexer implements Dictionary {
    public static int line = 1;
    public static String filename = "";
    private int peek = ' ';
    private Stack<Info> list = new Stack<>();
    private InputStreamReader in = null;
    private Map<String,Token> words = new HashMap<>();
	/*
	 * record the files imported to avoid re-import
	 */
    private Set<File> importedFiles = new HashSet<>();
	

    private void reserve(Word w) {
        words.put(w.lexeme,w);
    }

    @Override
    public Token getOrReserve(String name){
        Token s = words.get(name);
        if(s == null){
            Word tmp = new Word(name,Tag.ID);
            reserve(tmp);
            s = tmp;
        }
        return s;
    }
    
    public void error(String s){
        throw new RuntimeException("Line " + line + " in file `" +  filename + "':\n\t" + s);
    }

    public Lexer() {
        reserve( new Word("if",Tag.IF) );
        reserve( new Word("else",Tag.ELSE) );
        reserve( new Word("while",Tag.WHILE) );
        reserve( new Word("for",Tag.FOR) );
        reserve( new Word("do",Tag.DO) );
        reserve( new Word("break",Tag.BREAK) );
        reserve( new Word("continue",Tag.CONTINUE));
        reserve( new Word("def",Tag.DEF) );
        reserve( new Word("new",Tag.NEW));
        reserve( new Word("return",Tag.RETURN) );
        reserve( new Word("sizeof",Tag.SIZEOF));
        reserve( new Word("native",Tag.NATIVE));
        reserve( new Word("import",Tag.IMPORT));
        reserve( new Word("struct",Tag.STRUCT));
        reserve( new Word("switch",Tag.SWITCH));
        reserve( new Word("case",Tag.CASE));
        reserve( new Word("default",Tag.DEFAULT));
        reserve( new Word("virtual",Tag.VIRTUAL));
        reserve( new Word("override",Tag.OVERRIDE));
        reserve( new Word("instanceof",Tag.INSTOF));
        
        reserve( Word.True );
        reserve( Word.False );
        reserve( Word.This );
        reserve( Word.Super );
        reserve( Word.Null );
        reserve( Word.Auto );

        reserve( Type.Int );
        reserve( Type.Char );
        reserve( Type.Bool );
        reserve( Type.Real );
        reserve( Type.Str );
        reserve( Type.BigInt );
        reserve( Type.BigReal );
        reserve( Type.Void );

        reserve( Word.args);
    }

    private void save(String path){
        list.push(new Info(in,peek,filename,line,path));
    }

    /*
     * Save the file name
     */
    public void open( String file ) throws IOException {
        /*for the first open*/
        File f = new File(file);
        f = f.getCanonicalFile();
		
		/*
		 * we've seen this file before don't import it again
		 * NOTE: so we can't import a file recurrently!
		 */
		if(importedFiles.contains(f)){
			return;
		}
		
        if(in != null)
            save(System.getProperty("user.dir"));
        try{
            System.setProperty("user.dir",f.getCanonicalFile().getParent());
        } catch(Exception e){
            error(e.toString());
        }
        if(!f.isFile()){
            error("File `" + file + "' doesn't exist");
        }

        if(!f.canRead()) {
            error("File `" + file + "' can't be read");
        }

		importedFiles.add(f);

        in = new InputStreamReader(new FileInputStream(f));
        line = 1;
        peek = ' ';
        filename = file;
    }

    /*
     * Recover the lexer's info,if the stack is 
     * empty,do nothing
     * Return false if the stack is empty,or true
     */
    private boolean recover(){
        if(list.empty()){
            return false;
        } else {
            Info i = list.pop();
            in = i.in;
            filename = i.filename;
            line = i.line;
            peek = i.peek;
            try{
                System.setProperty("user.dir",i.path);
            } catch(Exception e){
                error(e.toString());
            }
            return true;
        }
    }
    
    private void readch() throws IOException {
        
        int p = in.read();
        while(p < 0){
            in.close();
            if(!recover()){
                /*end of all files*/
                in.close();
                in = null;
                break;
            }
            p = in.read();
        }
        peek = p > 0?(char) p : p;
    }

   private boolean readch(char c) throws IOException {
        readch();
        if(peek != c)
            return false;
        peek = ' ';
        return true;
    }


    public Token scan() throws IOException {
        for(;;readch()){
            if(peek == '\n') {
                line++;
            } else if(Character.isWhitespace(peek)){
                continue;
            } else if(peek == '/'){
                if(readch('*')){
                    readch();
                    do{
                        while(peek != '*'){
                            if(peek == '\n')
                                line ++;
                            readch();
                        }
                        readch();
                    }while( peek != '/' );
                } else if( peek == '/' ){
                    while(!readch('\n')){}
                    line++;
                } else if( peek == '=' ){
                    peek = ' ';
                    return Word.divass;
                } else {
                    return Word.div;
                }
            } else {
                break;
            }
        }

        switch(peek){
            case '&':
                if(readch('&')) 
                    return Word.and;
                else 
                    return new Token('&');
            case '|':
                if(readch('|')) 
                    return Word.or;
                else 
                    return new Token('|');
            case '=':
                if(readch('='))
                    return Word.eq;
                else
                    return Word.ass;
            case '!':
                if(readch('='))
                    return Word.ne;
                else
                    return Word.not;
            case '<':
                if(readch('='))
                    return Word.le;
                else
                    return Word.ls;
            case '>':
                if(readch('='))
                    return Word.ge;
                else 
                    return Word.gt;
            case '+':
                if(readch('+'))
                    return Word.inc;
                else if(peek == '='){
                    peek = ' ';
                    return Word.addass;
                } else
                    return Word.add;
            case '-':
                if(readch('-'))
                    return Word.dec;
                else if(peek == '='){
                    peek = ' ';
                    return Word.minass;
                }else
                    return Word.min;
            case '*':
                if(readch('='))
                    return Word.multass;
                else
                    return Word.mult;
            case '/'://won't happen
                if(readch('=')){
                    return Word.divass;
                }else
                    return Word.div;
            case '%':
                if(readch('='))
                    return Word.modass;
                else 
                    return Word.mod;
        }

        if(Character.isDigit(peek)){
            BigInteger v = BigInteger.ZERO;
            //int v = 0;
            do{
                //v = 10 * v + Character.digit(peek,10);
                v = v.multiply(BigInteger.TEN).add(BigInteger.valueOf(Character.digit(peek,10)));
                readch();
            }while(Character.isDigit(peek));
            if(peek != '.'){
                switch(peek){
                case 'R':
                    peek = ' ';
                    return new BigFloat(new BigDecimal(v));
                case 'r':
                    peek = ' ';
                    return new lexer.Float(v.floatValue());
                case 'i':
                    peek = ' ';
                    return new Num(v.intValue());
                case 'I':
                    peek = ' ';
                    return new BigNum(v);
                default:
                    if(v.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0)
                        return new Num(v.intValue());
                    else
                        return new BigNum(v);
                }
            }

            BigDecimal x = new BigDecimal(v);
            //float x = v;
            v = BigInteger.ZERO;
            int scale = 0;
            for(;;scale++){
                readch();
                if(! Character.isDigit(peek))
                    break;
                v = v.multiply(BigInteger.TEN).add(BigInteger.valueOf(Character.digit(peek,10)));
            }
            x = x.add(new BigDecimal(v,scale));
            switch(peek){
            case 'R':
                peek = ' ';
                return new BigFloat(x);
            case 'r':
                peek = ' ';
                return new lexer.Float(x.floatValue());
            default:
                if(x.compareTo(BigDecimal.valueOf(java.lang.Float.MAX_VALUE)) < 0)
                    return new lexer.Float(x.floatValue());
                else
                    return new BigFloat(x);
            }
        }

        if(Character.isLetter(peek)||peek == '_'){
            StringBuilder b = new StringBuilder();
            do{
                b.append((char)peek);
                readch();
            } while(Character.isLetterOrDigit(peek)||peek == '_');
            String s = b.toString();
            
            /*add three built-in variable*/
            switch (s) {
                case "_line_": /*line number*/
                    return new Num(line);
                case "_file_": /*file name*/
                    return new Str(filename);
                case "_version_": /*version*/
                    return new Num(Main.MAJOR_VERSION * 100 + Main.MINOR_VERSION);
            }
            Word w = (Word)words.get(s);
            if(w != null)
                return w;
            w = new Word(s,Tag.ID);
            words.put(s,w);
            return w;
        }

        if(peek == '\"'){
            StringBuilder b = new StringBuilder();
            readch();
            while(peek != '\"'){
                int c = peek;
                if(peek == '\\'){
                    readch();
                    c = peek;
                    switch(peek){
                    case '\'':
                    case '\"':
                    case '?':
                    case '\\':
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    default:
                        /*error*/
                    }
                }
                b.append((char)c);
                readch();
            }
            readch();
            return new Str(b.toString());
        } else if( peek == '\''){
            readch();
            int c = peek;

            if(peek == '\\'){
                readch();
                c = peek;
                switch(peek){
                case '\'':
                case '\"':
                case '?':
                case '\\':
                    break;
                case 'b':
                    c = '\b';
                    break;
                case 'f':
                    c = '\f';
                    break;
                case 'n':
                    c = '\n';
                    break;
                case 'r':
                    c = '\r';
                    break;
                case 't':
                    c = '\t';
                    break;
                default:
                    /*error*/
                }
                
            }
            if(!readch('\''))
                error("Wrong character constant.");
            return new Char((char)c);
        }

        Token tok = new Token(peek);
        peek = ' ';
        return tok;
    }

    static public void main(String[] args) throws IOException {
        Token t;
        Lexer lex = new Lexer();
        do{
            t = lex.scan();
            System.out.println(t.toString());
        }while(t.tag != -1);
    }
}