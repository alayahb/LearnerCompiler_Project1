//Alayah Benjamin
// COMP 360 - Project 1
//LearnCompiler

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * LearnerCompiler is a class used to implements a basic lexical analyzer (lexer) and recursive
 * descent parser for a simplified programming language defined by a specific BNF grammar.
 */
public class LearnerCompiler {
    enum TokenType { //Creating a special class to know the different type of Tokens in the language grammar
        FLOAT, // Keyword 'float'
        IDENTIFIER, // identifiers that are lowercase
        LEFT_PARENTHESES, // Symbol '('
        RIGHT_PARENTHESES, // Symbol '('
        LEFT_BRACKET, // Symbol '{'
        RIGHT_BRACKET, // Symbol '}'
        SEMICOLON, // Symbol ';'
        ASSIGN, // Symbol '='
        MULTIPLY, // Symbol '*'
        DIVIDE, // Symbol '/'
        EOF, // E.O.F stands for End of File/Input Marker
        INVALID // Represents unrecognized token inputs
    }

    //Token Class : representing an individual Token containing the type of grammar and lexeme string
    static class Token {
        TokenType tokentype; //TokenType enum category
        String lexeme; //the literal character string matching the token

        Token(TokenType tokentype, String lexeme) {
            this.tokentype = tokentype;
            this.lexeme = lexeme;
        }

        @Override
        public String toString() { //When the input file is written, it will have a display to show the lexeme on the left, token on the right
            return "Lexeme: " + lexeme +
                    ", Token: " + tokentype;
        }
    }

    static class LexicalAnalyzer {
        // LexicalAnalyzer is a class to handle breaking down raw source code input string into a stream of tokens
        static List<Token> tokenize(String input) {
            List<Token> tokens = new ArrayList<>();
            //Input replaces are bas to pad single character operators with spaces to easily be able to split and identitfy the symbols to the tokens
            input = input.replace("(", " ( ");
            input = input.replace(")", " ) ");
            input = input.replace("{", " { ");
            input = input.replace("}", " } ");
            input = input.replace(";", " ; ");
            input = input.replace("=", " = ");
            input = input.replace("*", " * ");
            input = input.replace("/", " / ");

            //Split source string by one or more whitespace characters
            String[] parts = input.split("\\s+");

            for (String part : parts) {
                if (part.equals("")) {
                    continue; //If there are empty tokens located, this will allow them to be skipped so there isn't a trail of whitespace
                }

                //if and else-if statments are used to match the exact lexemes to the corresponding token types
                if (part.equals("float")) {
                    tokens.add(new Token(TokenType.FLOAT, part));
                } else if (part.equals("(")) {
                    tokens.add(new Token(TokenType.LEFT_PARENTHESES, part));
                } else if (part.equals(")")) {
                    tokens.add(new Token(TokenType.RIGHT_PARENTHESES, part));
                } else if (part.equals("{")) {
                    tokens.add(new Token(TokenType.LEFT_BRACKET, part));
                } else if (part.equals("}")) {
                    tokens.add(new Token(TokenType.RIGHT_BRACKET, part));
                } else if (part.equals(";")) {
                    tokens.add(new Token(TokenType.SEMICOLON, part));
                } else if (part.equals("=")) {
                    tokens.add(new Token(TokenType.ASSIGN, part));
                } else if (part.equals("*")) {
                    tokens.add(new Token(TokenType.MULTIPLY, part));
                } else if (part.equals("/")) {
                    tokens.add(new Token(TokenType.DIVIDE, part));
                } else if (isIdentifier(part)) {
                    tokens.add(new Token(TokenType.IDENTIFIER, part));
                } else {
                    tokens.add(new Token(TokenType.INVALID, part));
                }
            }
            //Append EOF marker to denote and end of the token stream
            tokens.add(new Token(TokenType.EOF, "EOF"));
            return tokens;
        }
        //validaties if a string qualifies as an identifer (has to be strictly lowercase words and a string)
        static boolean isIdentifier(String word) {
            for (int i = 0; i < word.length(); i++) {
                if (!Character.isLowerCase(word.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }
// Top-Down Recursive Descent Parser to validate token stream against the BNF grammar rules.
    static class Parser {
        List<Token> tokens;
        int current = 0;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        String errorMessage = null;
    //Checking if the current token matches an expected Token Type, if it does then it'll advance to the next sep
    //If not, it will display a syntax message and show the error
        boolean match(TokenType expected) {
            Token token = tokens.get(current);

            if (token.tokentype == expected) {
                current++; //consume token
                return true;
            }
            //store first syntax error encountered during evaluation
            if (errorMessage == null) {
                errorMessage = "Syntax Error : Expected " + expected
                        + " but found " + token.tokentype
                        + " (\"" + token.lexeme + "\").";
            }

            return false;
        }

        boolean program() {
            //Function pattern : (ex: float<id> --> (float<id>)
            if (!match(TokenType.FLOAT)) return false;
            if (!match(TokenType.IDENTIFIER)) return false;
            if (!match(TokenType.LEFT_PARENTHESES)) return false;
            if (!match(TokenType.FLOAT)) return false;
            if (!match(TokenType.IDENTIFIER)) return false;
            if (!match(TokenType.RIGHT_PARENTHESES)) return false;
            if (!match(TokenType.LEFT_BRACKET)) return false;

            //Parse mandatory initial variable declaration
            if (!declaration()) return false;

            //Parse optional additional declarations starting with 'float'
            while (tokens.get(current).tokentype == TokenType.FLOAT) {
                if (!declaration()) return false;
            }
            //Parse required assignment statement
            if (!assignment()) return false;
            // Matching the closing brace and E.O.F token
            if (!match(TokenType.RIGHT_BRACKET)) return false;
            if (!match(TokenType.EOF)) return false;

            return true;
        }
        // Parses a <declaration> rule: <declaration -> float id;
        //It'll return true if the declaration is syntactically valid; false otherwise
        boolean declaration() {
            if (!match(TokenType.FLOAT)) return false;
            if (!match(TokenType.IDENTIFIER)) return false;
            if (!match(TokenType.SEMICOLON)) return false;

            return true;
        }
        // Parses an <assignment> rule : <assignment> -> id = <expression>;
        //It'll return true if assignment is syntactically valid; false otherwise
        boolean assignment() {
            if (!match(TokenType.IDENTIFIER)) return false;
            if (!match(TokenType.ASSIGN)) return false;
            if (!expression()) return false;
            if (!match(TokenType.SEMICOLON)) return false;

            return true;
        }

        boolean expression() {
            // Parses is used to have a <expression> rile for allowing the multiplication and division
            // Using <expression> --> id :(* | /) id} , return if expression structure is valid, false otherwise
            if (!match(TokenType.IDENTIFIER)) return false;

            // Loop used to handle chained multiplication or division operations
            while (tokens.get(current).tokentype == TokenType.MULTIPLY ||
                    tokens.get(current).tokentype == TokenType.DIVIDE) {
                current++;

                if (!match(TokenType.IDENTIFIER)) {
                    return false;
                }
            }
            return true;

        }
    }
 // Main function to use to enter the input file or put in the file to execute the lexer and parser
    public static void main(String[] args) throws FileNotFoundException {
        Scanner keyboard = new Scanner(System.in);
        // Display like a "menu" to help the user chose what method they want to use
        System.out.println("Choose input method:");
        System.out.println("1. Enter program/input manually");
        System.out.println("2. Read Program from file");

        int choice = keyboard.nextInt();
        keyboard.nextLine(); //Clear buffer newline character

        String program = "";
        String line;

        //If statement for Option 1: Manually string entry loop until the use put 'END' and click enter
        if (choice == 1) {
            System.out.println("Enter the Sample Program.");
            System.out.println("Type END when you are finished:");

            while (true) {
                line = keyboard.nextLine();

                if (line.equals("END")) {
                    break;
                }
                program = program + line + "\n";
            }
        }
        // Option 2: File reading input
             else if (choice == 2) {
                System.out.print("Enter file name: ");
                String fileName = keyboard.nextLine();

                Scanner inputFile = new Scanner(new File(fileName));

                while (inputFile.hasNextLine()) {
                    program = program + inputFile.nextLine() + "\n";
                }
                inputFile.close();
            }
             else { //Handles invalid menu selection
                System.out.println("Invalid choice.");
                keyboard.close();
                return;
            }

            List<Token> tokens = LexicalAnalyzer.tokenize(program);
            System.out.println();
            System.out.println("Lexemes and Tokens:");

            for (Token token : tokens) {
                System.out.println(token);
            }
            Parser parser = new Parser(tokens);

            System.out.println();

            //If the input is able to successfully go through then it will be considered a BNF grammar
            if (parser.program()) {
                System.out.println(
                        "The Sample Program is generated by the BNF grammar."
                );
            } else { //Else, as the program or input is being executed if the program notices a syntax error it will not only show the 'errorMessage' but it will display a statement saying its not BNF grammar
                System.out.println(parser.errorMessage);
                System.out.println(
                        "The Sample Program cannot be generated by the LearnCompiler BNF Grammar."
                );
            }
            keyboard.close();

        }
    }