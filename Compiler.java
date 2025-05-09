
import java.io.*;
import java.util.*;

enum TokenType
{
    ID, BREAK, CHAR, DOUBLE, ELSE, FOR, IF, INT, RETURN, STRUCT, VOID, WHILE,
    CT_INT, CT_REAL, CT_STRING, CT_CHAR,
    COMMA, SEMICOLON, LPAR, RPAR, LBRACKET, RBRACKET, LACC, RACC,
    ADD, SUB, MUL, DIV, DOT, AND, OR, NOT, ASSIGN, EQUAL, NOTEQ,
    LESS, LESSEQ, GREATER, GREATEREQ,
    SPACE, LINECOMMENT, COMMENT,
    END 
}

class Token
{
    TokenType type;
    String text;
    int line;
    Token next;

    public Token(TokenType type, String text, int line)
    {
        this.type = type;
        this.text = text;
        this.line = line;
    }

    public String toString()
    {
        return String.format("Token(%s, \"%s\", Line: %d)", type, text, line);
    }
}

class Lex
{
    private final String input;
    private int pos = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();

    public Lex(String input)
    {
        this.input = input+"\0";
    }

    private char peek()
    {
        return input.charAt(pos);
    }

    private char advance()
    {
        return input.charAt(pos++);
    }

    private boolean match(char expected)
    {
        if(peek()==expected)
        {
            advance();
            return true;
        }
        return false;
    }

    private void consumeWhitespace()
    {
        while(Character.isWhitespace(peek()))
        {
            if(peek() =='\n') line++;
            advance();
        }
    }

    private void tokenizeIdentifierOrKeyword()
    {
        int start=pos;

        while(Character.isLetterOrDigit(peek()) || peek()=='_') advance();

        String text = input.substring(start,pos);
        TokenType type;
        switch(text)
        {
            case "break": 
                type = TokenType.BREAK; 
                break;
            case "char": 
                type = TokenType.CHAR; 
                break;
            case "double": 
                type = TokenType.DOUBLE; 
                break;
            case "else": 
                type = TokenType.ELSE; 
                break;
            case "for": 
                type = TokenType.FOR; 
                break;
            case "if": 
                type = TokenType.IF; 
                break;
            case "int": 
                type = TokenType.INT; 
                break;
            case "return": 
                type = TokenType.RETURN; 
                break;
            case "struct": 
                type = TokenType.STRUCT; 
                break;
            case "void": 
                type = TokenType.VOID; 
                break;
            case "while": 
                type = TokenType.WHILE; 
                break;
            default: 
                type = TokenType.ID; 
                break;
        };
        tokens.add(new Token(type, text, line));
    }

    private void tokenizeNumber()
    {
        int start =pos;
        boolean isReal =false;

        if(match('0'))
        {
            if(match('x') || match('X'))
            {
                while(Character.isDigit(peek()) || "abcdefABCDEF".indexOf(peek())!=-1) advance();
                tokens.add(new Token(TokenType.CT_INT, input.substring(start, pos), line));
                return;
            }
            else if(Character.isDigit(peek()))
            {
                while(Character.isDigit(peek())) advance();
                tokens.add(new Token(TokenType.CT_INT, input.substring(start, pos), line));
                return;
            }
        }

        while(Character.isDigit(peek())) advance();

        if(match('.'))
        {
            isReal=true;
            while(Character.isDigit(peek())) advance();
        }
        if(match('e') || match('E'))
        {
            isReal =true;
            if(match('-') || match('+')) advance();
            while(Character.isDigit(peek())) advance();
        }

        TokenType type=isReal?TokenType.CT_REAL:TokenType.CT_INT;

        tokens.add(new Token(type, input.substring(start, pos), line));
    }

    private void tokenizeString()
    {
        int start=pos;
        advance();
        while(peek() != '"' && peek()!='\0') advance();
        advance();
        tokens.add(new Token(TokenType.CT_STRING, input.substring(start+1, pos-1), line));
    }

    private void tokenizeChar()
    {
        int start=pos;
        advance();
        while(peek()!='\'' && peek()!='\0') advance();
        advance();
        tokens.add(new Token(TokenType.CT_CHAR, input.substring(start+1, pos-1), line));
    }

    private void consumeLineComment()
    {
        while(peek()!='\n' && peek()!='\0') advance();
    }

    private void consumeBlockComment()
    {
        while(pos < input.length()-1)
        {
            if(match('*') && match('/')) return;
            advance();
        }
    }

    private void tokenizeSymbol()
    {
        char ch=advance();
        switch (ch) 
        {
            case '+':
                tokens.add(new Token(TokenType.ADD, "+", line));
                break;
            case '-':
                tokens.add(new Token(TokenType.SUB, "-", line));
                break;
            case '*':
                tokens.add(new Token(TokenType.MUL, "*", line));
                break;
            case ',':
                tokens.add(new Token(TokenType.COMMA, ",", line));
                break;
            case ';':
                tokens.add(new Token(TokenType.SEMICOLON, "", line));
                break;
            case '(':
                tokens.add(new Token(TokenType.LPAR, "(", line));
                break;
            case ')':
                tokens.add(new Token(TokenType.RPAR, ")", line));
                break;
            case '[':
                tokens.add(new Token(TokenType.LBRACKET, "[", line));
                break;
            case ']':
                tokens.add(new Token(TokenType.RBRACKET, "]", line));
                break;
            case '{':
                tokens.add(new Token(TokenType.LACC, "{", line));
                break;
            case '}':
                tokens.add(new Token(TokenType.RACC, "}", line));
                break;
            case '<':
                tokens.add(match('=')? new Token(TokenType.LESSEQ, "<=", line) :new Token(TokenType.LESS, "<", line));
                break;
            case '>':
                tokens.add(match('=')? new Token(TokenType.GREATEREQ, ">=", line) :new Token(TokenType.GREATER, ">", line));
                break;
            case '=':
                tokens.add(match('=')? new Token(TokenType.EQUAL, "==", line) : new Token(TokenType.ASSIGN, "=", line));
                break;
            default:
                System.err.println("Unknown character "+ch);
                break;
        }
    }

    public List<Token> tokenize()
    {
        while(pos< input.length()-1)
        {
            char c = peek();
            {
                if(Character.isWhitespace(c))
                {
                    consumeWhitespace();
                }
                else if(Character.isLetter(c) || c=='_')
                {
                    tokenizeIdentifierOrKeyword();
                }
                else if(Character.isDigit(c))
                {
                    tokenizeNumber();
                }
                else if(c=='"')
                {
                    tokenizeString();
                }
                else if(c=='\'')
                {
                    tokenizeChar();
                }
                else if(c=='/')
                {
                    if(match('/'))
                    {
                        consumeLineComment();
                    }
                    else if(match('*'))
                    {
                        consumeBlockComment();
                    }
                }
                else
                {
                    tokenizeSymbol();
                }
            }
        }
        tokens.add(new Token(TokenType.END, "EOF", line));
        return tokens;
    }
}

class Syntactic
{
    static Token crtTk;
    static Token consumedTk;

    public static void parse(List<Token> tokens)
    {
        crtTk=tokens.get(0);

        if(!unit())
        {
            System.err.println("Syntax error at token: " + crtTk);
        }
        else
        {
            System.out.println("Parsed successfully");
        }
    }

    static boolean consume(TokenType code)
    {
        if(crtTk.type==code)
        {
            consumedTk=crtTk;
            crtTk=crtTk.next;
            return true;
        }

        return false;
    }

    static void tkerr(Token tk, String msg)
    {
        throw new RuntimeException("Syntax error at token: " + tk.line + " " + msg);
    }

    static boolean unit()
    {
        while(true)
        {
            if(declStruct() || declFunc() || declVar())
            {
                continue;
            }
            break;
        }

        return consume(TokenType.END);
    }

    static boolean declStruct()
    {
        Token startTk=crtTk;

        if(consume(TokenType.STRUCT))
        {
            if(consume(TokenType.ID))
            {
                if(consume(TokenType.LACC))
                {
                    while(declVar()) {}
                    if(consume(TokenType.RACC))
                    {
                        if(consume(TokenType.SEMICOLON))
                        {
                            return true;
                        }
                        else tkerr(crtTk, "missing ';' after struct declaration");
                    }
                    else tkerr(crtTk, "missing '}' in struct declaration");
                }
                else tkerr(crtTk, "missing '{' in struct declaration");
            }
            else tkerr(crtTk, "missing ID after 'struct'");
        }

        crtTk=startTk;
        return false;
    }

    static boolean declVar()
    {
        Token startTk=crtTk;
        
        if(typeBase())
        {
            if(consume(TokenType.ID))
            {
                arrayDecl();
                while(true)
                {
                    if(consume(TokenType.COMMA))
                    {
                        if(!consume(TokenType.ID)) tkerr(crtTk, "missing ID after ',' in variable list");
                        arrayDecl();
                    }
                    else
                    {
                        break;
                    }
                }
                if(!consume(TokenType.SEMICOLON)) tkerr(crtTk, "missing ';' after variable declaration");
                return true;
            }
            else
            {
                crtTk=startTk;
                return false;
            }
        }
        crtTk=startTk;
        return false;
    }

    static boolean declFunc()
    {
        Token startTk=crtTk;

        if(typeBase())
        {
            if(!consume(TokenType.ID))
            {
                crtTk=startTk;
                return false;
            }
        }
        else if(consume(TokenType.VOID))
        {
            if(!consume(TokenType.ID))
            {
                crtTk=startTk;
                return false;
            }
        }
        else
        {
            crtTk=startTk;
            return false;
        }

        if(!consume(TokenType.LPAR)) tkerr(crtTk, "missing '(' in function declaration");

        if(funcArg())
        {
            while(consume(TokenType.COMMA))
            {
                if(!funcArg()) tkerr(crtTk, "invalid funcArg after ','");
            }
        }

        if(!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' in function declaration");

        if(!stmCompound()) tkerr(crtTk, "invalid function body");

        return true;
    }

    static boolean funcArg()
    {
        Token startTk=crtTk;

        if(typeBase())
        {
            if(consume(TokenType.ID))
            {
                arrayDecl();
                return true;
            }
        }

        crtTk=startTk;
        return false;
    }

    static boolean typeBase()
    {
        if(consume(TokenType.INT)) return true;
        if(consume(TokenType.DOUBLE)) return true;
        if(consume(TokenType.CHAR)) return true;
        if(consume(TokenType.STRUCT))
        {
            if(!consume(TokenType.ID)) tkerr(crtTk, "missing ID after 'struct'");
            return false;
        }
        return false;
    }

    static boolean arrayDecl()
    {
        Token startTk=crtTk;

        if(consume(TokenType.LBRACKET))
        {
            if(!consume(TokenType.RBRACKET)) tkerr(crtTk, "missing ']' in array declaration");
            return true;
        }

        crtTk=startTk;
        return false;
    }

    static boolean typeName() 
    {
        if (!typeBase()) return false;
        return true;
    }

    static boolean stm()
    {
        Token startToken=crtTk;

        if(stmCompound())
        {
            return true;
        }

        if(consume(TokenType.IF))
        {
            if(!consume(TokenType.LPAR)) tkerr(crtTk, "missing '(' after if");
            if(!expr()) tkerr(crtTk, "missing '(' after if");
            if(!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' after if condition");
            if(!stm()) tkerr(crtTk, "missing statement after if");
            if(consume(TokenType.ELSE))
                if(!stm()) tkerr(crtTk, "missing statement after else");

            return true;
        }

        if (consume(TokenType.WHILE)) 
        {
            if (!consume(TokenType.LPAR)) tkerr(crtTk, "missing '(' after while");
            if (!expr()) tkerr(crtTk, "invalid expression inside while");
            if (!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' after while condition");
            if (!stm()) tkerr(crtTk, "missing statement after while");

            return true;
        }

        if (consume(TokenType.FOR)) 
        {
            if (!consume(TokenType.LPAR)) tkerr(crtTk, "missing '(' after for");
            if (!consume(TokenType.SEMICOLON)) tkerr(crtTk, "missing ';' after first for expression");
            if (!consume(TokenType.SEMICOLON)) tkerr(crtTk, "missing ';' after second for expression");
            if (!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' after for expressions");
            if (!stm()) tkerr(crtTk, "missing statement after for");

            return true;
        }

        if(consume(TokenType.BREAK))
        {
            if(!consume(TokenType.SEMICOLON)) tkerr(crtTk, "missing ';' after break");
            return true;
        }

        if(consume(TokenType.RETURN))
        {
            if(!consume(TokenType.SEMICOLON)) tkerr(crtTk, "missing ';' after return");
            return true;
        }

        if(expr())
        {
            if(!consume(TokenType.SEMICOLON)) tkerr(crtTk, "missing ';' after expression");
            return true;
        }

        if(consume(TokenType.SEMICOLON))
        {
            return true;
        }

        crtTk=startToken;

        return false;
    }

    static boolean stmCompound()
    {
        if(!consume(TokenType.LACC)) return false;

        while (true) 
        {
            if(declVar()) {}
            else if(stm()) {}
            else
            {
                break;
            }    
        }

        if(!consume(TokenType.RACC)) tkerr(crtTk, "missing '}' or syntax error in stmCompound");

        return true;
    }

    static boolean expr()
    {
        return exprAssign();
    }

    static boolean exprAssign()
    {
        Token startToken=crtTk;

        if(exprUnary())
        {
            if(consume(TokenType.ASSIGN))
            {
                if(!exprAssign()) tkerr(crtTk, "invalid assignment");
                return true;
            }

            crtTk=startToken;
        }

        return exprOr();
    }

    static boolean exprOr()
    {
        if(!exprAnd()) return false;

        while(consume(TokenType.OR))
        {
            if(!exprAnd()) tkerr(crtTk, "invalid expression after ||");
        }

        return true;
    }

    static boolean exprAnd()
    {
        if(!exprEq()) return false;
        while (consume(TokenType.AND)) 
        {
            if (!exprEq()) tkerr(crtTk, "invalid expression after &&");    
        }

        return true;
    }

    static boolean exprEq() 
    {
        if (!exprRel()) return false;
        while (true) 
        {
            if (consume(TokenType.EQUAL) || consume(TokenType.NOTEQ)) 
            {
                if (!exprRel()) tkerr(crtTk, "invalid expression after == or !=");
            } 
            else break;
        }
        return true;
    }

    static boolean exprRel() 
    {
        if (!exprAdd()) return false;
        while (true) 
        {
            if (consume(TokenType.LESS) || consume(TokenType.LESSEQ) || consume(TokenType.GREATER) || consume(TokenType.GREATEREQ)) 
            {
                if (!exprAdd()) tkerr(crtTk, "invalid expression after relational operator");
            } 
            else break;
        }
        return true;
    }

    static boolean exprAdd() 
    {
        if (!exprMul()) return false;
        while (true) 
        {
            if (consume(TokenType.ADD) || consume(TokenType.SUB)) 
            {
                if (!exprMul()) tkerr(crtTk, "invalid expression after '+' or '-'");
            } 
            else break;
        }
        return true;
    }

    static boolean exprMul() 
    {
        if (!exprCast()) return false;

        while (true) 
        {
            if (consume(TokenType.MUL) || consume(TokenType.DIV)) 
            {
                if (!exprCast()) tkerr(crtTk, "invalid expression after '*' or '/'");
            } 
            else break;
        }
        return true;
    }

    static boolean exprCast() 
    {
        Token startTk = crtTk;
        if (consume(TokenType.LPAR)) 
        {
            if (typeName()) 
            {
                if (!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' after cast");
                if (!exprCast()) tkerr(crtTk, "invalid expression after cast");
                return true;
            }
            crtTk = startTk;
        }
        return exprUnary();
    }

    static boolean exprUnary() 
    {
        if (consume(TokenType.SUB) || consume(TokenType.NOT)) 
        {
            if (!exprUnary()) tkerr(crtTk, "invalid expression after unary '-' or '!'");
            return true;
        }
        return exprPostfix();
    }

    static boolean exprPostfix() 
    {
        if (!exprPrimary()) return false;
        while (true) 
        {
            if (consume(TokenType.LBRACKET)) 
            {
                if (!expr()) tkerr(crtTk, "invalid index expression in array access");
                if (!consume(TokenType.RBRACKET)) tkerr(crtTk, "missing ']' in array access");
            } 
            else if (consume(TokenType.DOT)) 
            {
                if (!consume(TokenType.ID)) tkerr(crtTk, "missing field name after '.'");
            } 
            else 
            {
                break;
            }
        }
        return true;
    }

    static boolean exprPrimary() 
    {
        Token startTk = crtTk;
    
        if (consume(TokenType.ID)) 
        {
            if (consume(TokenType.LPAR)) 
            {
                if (expr()) 
                {
                    while (consume(TokenType.COMMA)) 
                    {
                        if (!expr()) tkerr(crtTk, "invalid expression after ',' in call");
                    }
                }
                if (!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' after function call arguments");
            }
            return true;
        }
        if (consume(TokenType.CT_INT)) return true;
        if (consume(TokenType.CT_REAL)) return true;
        if (consume(TokenType.CT_CHAR)) return true;
        if (consume(TokenType.CT_STRING)) return true;
        if (consume(TokenType.LPAR)) 
        {
            if (!expr()) tkerr(crtTk, "invalid expression inside '(' ')'");
            if (!consume(TokenType.RPAR)) tkerr(crtTk, "missing ')' after expression");
            return true;
        }
    
        crtTk = startTk;
        return false;
    }
}

public class Compiler
{
    public static void main(String[] args) 
    {
        StringBuilder sourceCode = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader("0.c")))
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                sourceCode.append(line).append("\n");
            }
        }
        catch(IOException e)
        {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        Lex lexer=new Lex(sourceCode.toString());
        List<Token> tokens = lexer.tokenize();

        for(Token token : tokens)
        {
            System.out.println(token);
        }

        Syntactic.parse(tokens);
    }
}