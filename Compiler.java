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
        TokenType type = switch(text)
        {
            case "break" : TokenType.BREAK;
            case "char" : TokenType.CHAR;
            case "double" : TokenType.DOUBLE;
            case "else" : TokenType.ELSE;
            case "float" : TokenType.FLOAT;
            case "for" : TokenType.FOR;
            case "if" : TokenType.IF;
            case "int" : TokenType.INT;
            case "return" : TokenType.RETURN;
            case "struct" : TokenType.STRUCT;
            case "void" : TokenType.VOID;
            case "while" : TokenType.WHILE;
            default -> TokenType.ID;
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
                tokens.add(new Token(TokenType.ADD, "+", line))
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