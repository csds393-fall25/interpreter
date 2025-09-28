import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpartieScanner {
    private String source;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("while", TokenType.WHILE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fun", TokenType.FUN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("var", TokenType.VAR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("null", TokenType.NULL);
    }

    public SpartieScanner(String source) {
        this.source = source;
    }

    public List<Token> scan() {
        List<Token> tokens = new ArrayList<>();

        Token token = null;
        while (!isAtEnd() && (token = getNextToken()) != null) {
            if (token.type != TokenType.IGNORE)
                tokens.add(token);
        }

        return tokens;
    }

    private Token getNextToken() {
        Token token = null;

        // Try to get each type of token, starting with a simple token, and getting a
        // little more complex
        token = getSingleCharacterToken();
        if (token == null)
            token = getComparisonToken();
        if (token == null)
            token = getDivideOrComment();
        if (token == null)
            token = getStringToken();
        if (token == null)
            token = getNumericToken();
        if (token == null)
            token = getIdentifierOrReservedWord();
        if (token == null) {
            error(line, String.format("Unexpected character '%c' at %d", source.charAt(current), current));
        }

        return token;
    }

    private Token getSingleCharacterToken() {

        // Hint: Examine the character, if you can get a token, return it, otherwise
        // return null
        // Hint: Be careful with the divide, we have ot know if it is a single character
        TokenType type = TokenType.UNDEFINED;
        String text = "";
        int lineForConstructor = line;
        boolean flagForIncrement = false;

        char nextCharacter = source.charAt(current);
        // if hit eol, increase line
        // difference between subtract and negative??

        if (nextCharacter == ';') {
            flagForIncrement = true;
            type = TokenType.SEMICOLON;
            text = ";";
            lineForConstructor = line;

        } else if (nextCharacter == ' ') {
            flagForIncrement = true;
            type = TokenType.IGNORE;
            text = " ";
            lineForConstructor = line;
        }

        else if (nextCharacter == '+') {
            flagForIncrement = true;
            type = TokenType.ADD;
            text = "+";
            lineForConstructor = line;

        }
           else if (nextCharacter == ',') {
            flagForIncrement = true;
            type = TokenType.COMMA;
            text = ",";
            lineForConstructor = line;

        }

        else if (nextCharacter == '-') {

            flagForIncrement = true;
            type = TokenType.SUBTRACT;
            text = "-";
            lineForConstructor = line;
        } else if (nextCharacter == '*') {
            flagForIncrement = true;
            type = TokenType.MULTIPLY;
            text = "*";
            lineForConstructor = line;
        } else if (nextCharacter == '(') {
            flagForIncrement = true;
            type = TokenType.LEFT_PAREN;
            text = "(";
            lineForConstructor = line;
        } else if (nextCharacter == ')') {
            flagForIncrement = true;
            type = TokenType.RIGHT_PAREN;
            text = ")";
            lineForConstructor = line;
        } else if (nextCharacter == '{') {
            flagForIncrement = true;
            type = TokenType.LEFT_BRACE;
            text = "{";
            lineForConstructor = line;
        } else if (nextCharacter == '}') {
            flagForIncrement = true;
            type = TokenType.RIGHT_BRACE;
            text = "}";
            lineForConstructor = line;
        } else if (nextCharacter == '&') {
            flagForIncrement = true;
            type = TokenType.AND;
            text = "&";
            lineForConstructor = line;
        } else if (nextCharacter == '|') {
            flagForIncrement = true;
            type = TokenType.OR;
            text = "|";
            lineForConstructor = line;
        } else if (nextCharacter == '\r') {
            flagForIncrement = true;
            type = TokenType.EOL;
            text = "\r";
            lineForConstructor = line;

        }

        else if (nextCharacter == '\n') {
            line++;
            flagForIncrement = true;
            type = TokenType.EOL;
            text = "\n";
            lineForConstructor = line - 1;

        }

        if (flagForIncrement) {
            start++;
            current++;
            return new Token(type, text, lineForConstructor);

        }
        return null;

        // Hint: Start of not knowing what the token is, if we can determine it, return
        // it, otherwise, return null

    }

    private Token getComparisonToken() {
        // Hint: Examine the character for a comparison but check the next character (as
        // long as one is available)
        // For example: < or <=
        char nextCharacter = source.charAt(current);
        if (nextCharacter == '<') {
            start++;

            if (examine('=')) {
                current++;
                current++;
                start = current;

                return new Token(TokenType.LESS_EQUAL, "<=", line);

            } else {
                current++;

                return new Token(TokenType.LESS_THAN, "<", line);
            }

        }
        if (nextCharacter == '>') {

            start++;

            if (examine('=')) {

                current++;
                current++;
                start = current;
                return new Token(TokenType.GREATER_EQUAL, ">=", line);

            } else {
                current++;
                return new Token(TokenType.GREATER_THAN, ">", line);
            }

        }

        if (nextCharacter == '!') {

            start++;

            if (examine('=')) {

                current++;
                current++;
                start = current;
                return new Token(TokenType.NOT_EQUAL, "!=", line);

            } else {
                current++;
                return new Token(TokenType.NOT, "!", line);
            }

        }

        if (nextCharacter == '=') {
            start++;

            if (examine('=')) {
                current++;
                current++;
                start = current;
                return new Token(TokenType.EQUIVALENT, "==", line);

            } else {
                current++;
                return new Token(TokenType.ASSIGN, "=", line);
            }

        }

        return null;
    }

    // TODO: Complete implementation
    private Token getDivideOrComment() {
        // Hint: Examine the character for a comparison but check the next character (as
        // long as one is available)
        char nextCharacter = source.charAt(current);

        if (nextCharacter == '/') {
            start++;
            if (examine('/')) {
                // TODO: Question: do we capture the // or just the comment content?
                current +=2;
                start = current;
                // TODO: Edge case: Empty comment at eof
                if (current+2 ==  source.length()) {
                    return new Token(TokenType.IGNORE, "//", line);
                }
                // Typical case
                while (nextCharacter != '\n' && current <= source.length()-1) {
                    current++;
                    nextCharacter = source.charAt(current);
                }
                // comment is from start (inclusive) to current (exclusive)
                String comment = source.substring(start, current);
                start = current++;
                return new Token(TokenType.IGNORE, comment, line);
            } else {
                // If there is only one / not followed by another /, it's a divide token
                current++;
                return new Token(TokenType.DIVIDE, "/", line);
            }
        }

        return null;
    }

    // TODO: Complete implementation
    private Token getStringToken() {
        // Hint: Check if you have a double quote, then keep reading until you hit another double quote
        // But, if you do not hit another double quote, you should report an error
        char nextCharacter = source.charAt(current);
        start = current;
        if (nextCharacter == '"') {
            // Point to first character in the String
            start++;
            current++;
            nextCharacter = source.charAt(current);
            while (nextCharacter != '"') {
                if (current == source.length() - 1 || nextCharacter == '\n') {
                    error(line, "Closing double quote not found for String Token, reached end of file or end of line");
                }
                current++;
                nextCharacter = source.charAt(current);
            }
            // string is from start (inclusive) to current (exclusive)
            String string = source.substring(start, current);
            start = current++;
            return new Token(TokenType.STRING, string, line, string);
        }

        return null;
    }

    private Token getNumericToken() {
        // Hint: Follow similar idea of String, but in this case if it is a digit
        // You should only allow one period in your scanner
        boolean hasPeriod = false;

        // number is from start (inclusive) to current (exclusive)
        while (!isAtEnd() && (isDigit(source.charAt(current)) || (source.charAt(current) == '.' && !hasPeriod))) {
            if (source.charAt(current) == '.') {
                hasPeriod = true;
            }
            current++;
        }

        if (current != start) {
            String number = source.substring(start, current);
            start = current;
            return new Token(TokenType.NUMBER, number, line, Double.parseDouble(number));
        }

        return null;
    }

    private Token getIdentifierOrReservedWord() {
        // Hint: Assume first it is an identifier and once you capture it, then check if
        // it is a reserved word.

        while (!isAtEnd() && isAlpha(source.charAt(current))) {
            current++;
        }

        if (current != start) {
            String identifier = source.substring(start, current);
            start = current;
            if (keywords.containsKey(identifier)) {
                return new Token(keywords.get(identifier), identifier, line, identifier);
            }

            return new Token(TokenType.IDENTIFIER, identifier, line, identifier);
        }
        return null;
    }

    // Helper Methods
    private boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private boolean isAlpha(char character) {
        return character >= 'a' && character <= 'z' ||
                character >= 'A' && character <= 'Z';
    }

    // This will check if a character is what you expect, if so, it will advance
    // Useful for checking <= or //
    private boolean examine(char expected) {
        if (current + 1 >= source.length())
            return false;
        if (source.charAt(current + 1) != expected)
            return false;

        // Otherwise, it matches it, so advance
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Error handling
    private void error(int line, String message) {
        System.err.printf("Error occurred on line %d : %s\n", line, message);
        System.exit(ErrorCode.INTERPRET_ERROR);
    }
}
