package sdfomin.parser;

import java.text.ParseException;
import java.util.function.Supplier;

import static sdfomin.parser.JetBrainsAstNodeType.*;

public class JetBrainsParser {

    /**
     * symbols between operands
     */
    public final String DEFAULT_WHITESPACES = " \n\r\t";

    /**
     * text to parse
     */
    private final String source;

    /**
     * current position of caret
     */
    private int pos = 0;

    public int getPos() {
        return pos;
    }

    public char getCurrent() {
        return pos >= source.length() ? 0 : source.charAt(pos);
    }

    public boolean isNext() {
        return getCurrent() != 0;
    }

    public void next() {
        if (isNext())
            pos++;
    }

    /**
     * move caret to next non-whitespace symbol
     */
    public void skip() {
        while (DEFAULT_WHITESPACES.contains(String.valueOf(getCurrent())))
            next();
    }

    /**
     * match string from caret to args
     *
     * @param args strings to match
     * @return arg which found or null if nothing was found
     */
    private String matchNoExcept(String... args) {
        int pos = getPos();
        for (String s : args) {
            boolean match = true;
            for (int j = 0; j < s.length(); ++j) {
                if (getCurrent() == s.charAt(j))
                    next();
                else {
                    this.pos = pos;
                    match = false;
                    break;
                }
            }
            if (match) {
                skip();
                return s;
            }
        }
        return null;
    }

    /**
     * is string matching to args
     *
     * @param terms string try to match
     * @return true if matched, otherwise false
     */
    public boolean isMatch(String... terms) {
        int pos = getPos();
        String result = matchNoExcept(terms);
        this.pos = pos;
        return result != null;
    }

    /**
     * try to match string with one of the terms
     *
     * @param terms strings try to match with
     * @return term that was found
     * @throws ParseException if nothing was found
     */
    public String match(String... terms) throws ParseException {
        int pos = getPos();
        String result = matchNoExcept(terms);
        if (result == null) {
            StringBuilder message = new StringBuilder("One of the strings were expected: ");
            boolean first = true;
            for (String term : terms) {
                if (!first)
                    message.append(", ");
                message.append(String.format("\"%s\"", term));
                first = false;
            }
            throw new ParseException(message.toString(), pos);
        }
        return result;
    }

    public JetBrainsParser(String source) {
        this.source = source;
    }

    /**
     * Program → StatementList
     *
     * @return created node PROGRAM
     */
    private JetBrainsAstNode program() {
        JetBrainsAstNode res = new JetBrainsAstNode(JetBrainsAstNodeType.PROGRAM, statementList());
        if (isNext())
            res.getChild(0).addChild(new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    "Extra symbol on pos " + getPos()));
        return res;
    }

    /**
     * StatementList → empty | StatementList Statement
     *
     * @return StatementList node
     */
    private JetBrainsAstNode statementList() {
        JetBrainsAstNode statementNode = new JetBrainsAstNode(JetBrainsAstNodeType.STATEMENT_LIST);
        while (isNext() && !isMatch("}")) {
            int pos = this.getPos();
            statementNode.addChild(statement());
            if (this.getPos() == pos) {
                next();
                skip();
            }
        }
        return statementNode;
    }

    /**
     * Statement → ExpressionStatement | IfStatement | AssignStatement | BlockStatement
     *
     * @return one of four statements OR ParseError if can't parse
     */
    private JetBrainsAstNode statement() {
        JetBrainsAstNode res;
        try {

            if (isMatch("if")) { // IfStatement
                JetBrainsAstNode expression;
                match("if");
                match("(");
                if (isMatch(")"))
                    expression = new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                            "Empty \"if\" expression in position " + getPos());
                else
                    expression = expression();
                match(")");
                JetBrainsAstNode statement = statement();
                res = new JetBrainsAstNode(JetBrainsAstNodeType.IF_STATEMENT, expression, statement);
            } else if (isMatch("@")) { // AssignStatement
                match("@");
                JetBrainsAstNode identifier = identifier();
                match("=");
                JetBrainsAstNode expression = expression();
                match(";");
                res = new JetBrainsAstNode(JetBrainsAstNodeType.ASSIGN_STATEMENT, identifier, expression);
            } else if (isMatch("{")) { // BlockStatement
                match("{");
                res = new JetBrainsAstNode(JetBrainsAstNodeType.BLOCK_STATEMENT, statementList());
                match("}");
            } else { // ExpressionStatement
                res = new JetBrainsAstNode(JetBrainsAstNodeType.EXPRESSION_STATEMENT, expression());
                match(";");
            }
        } catch (ParseException ex) {
            return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    ex.getMessage() + " in position " + ex.getErrorOffset());
        }
        return res;
    }

    /**
     * Expression → ConditionExpression
     *
     * @return ConditionExpression node
     */
    private JetBrainsAstNode expression() {
        JetBrainsAstNode conditionExpression = conditionExpression();
        return new JetBrainsAstNode(JetBrainsAstNodeType.EXPRESSION, conditionExpression);
    }

    /**
     * Find all expressions with type nodeType in current expression
     * with parsing nextExpression inside
     * @param nextExpression type of operands
     * @param nodeType type of current expression
     * @param terms operators of current expression
     * @return nodeType parsed expression OR ParseError if can't parse
     */
    private JetBrainsAstNode countExpression(Supplier<JetBrainsAstNode> nextExpression,
                                             JetBrainsAstNodeType nodeType,
                                             String... terms) {
        JetBrainsAstNode result = nextExpression.get();
        while (isMatch(terms)) {
            String operation;
            try {
                operation = match(terms);
            } catch (ParseException e) {
                return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                        e.getMessage() + " in position " + e.getErrorOffset());
            }
            JetBrainsAstNode expression = nextExpression.get();
            result = new JetBrainsAstNode(nodeType, operation, result, expression);
        }
        return result;
    }

    /**
     * ConditionExpression -> PlusMinusExpression | PlusMinusExpression < PlusMinusExpression
     * | PlusMinusExpression > PlusMinusExpression
     *
     * @return ConditionExpression node OR ParseError if can't parse
     */
    private JetBrainsAstNode conditionExpression() {
        return countExpression(this::plusMinusExpression, CONDITION_EXPRESSION, "<", ">");
    }

    /**
     * PlusMinusExpression → MultiplyDivisionExpression | PlusMinusExpression + MultiplyDivisionExpression
     * | PlusMinusExpression - MultiplyDivisionExpression
     *
     * @return PlusMinusExpression node OR ParseError if can't parse
     */
    private JetBrainsAstNode plusMinusExpression() {
        return countExpression(this::multiplyDivisionExpression, PLUS_MINUS_EXPRESSION, "+", "-");
    }

    /**
     * MultiplyDivisionExpression → SimpleExpression | MultiplyDivisionExpression * SimpleExpression
     * | MultiplyDivisionExpression / SimpleExpression
     *
     * @return MultiplyDivisionExpression node OR ParseError if can't parse
     */
    private JetBrainsAstNode multiplyDivisionExpression() {
        return countExpression(this::simpleExpression, MULTIPLY_DIVISION_EXPRESSION, "*", "/");
    }

    /**
     * SimpleExpression → Identifier | Integer | ( Expression )
     *
     * @return one of three node types OR ParseError if can't parse
     */
    private JetBrainsAstNode simpleExpression() {
        JetBrainsAstNode res;
        if (isMatch("(")) { // Expression
            try {
                match("(");
                res = expression();
                match(")");
            } catch (ParseException ex) {
                return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                        ex.getMessage() + " in position " + ex.getErrorOffset());
            }
        } else if (Character.isLetter(getCurrent())) { // Identifier
            res = identifier();
        } else { // Integer
            res = integer();
        }
        return new JetBrainsAstNode(JetBrainsAstNodeType.SIMPLE_EXPRESSION, res);
    }

    /**
     * check if word is reserved
     *
     * @return true if reserved
     */
    private boolean isReserved() {
        return isMatch("if", "(", ")", "{", "}", "<", ">", "*", "/");
    }

    /**
     * Identifier → Identifier created from latin letters
     *
     * @return Identifier OR ParseError if can't parse
     */
    private JetBrainsAstNode identifier() {
        if (isReserved())
            return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    "Not proper symbol in position " + getPos());
        StringBuilder identifier = new StringBuilder();
        if (Character.isLetter(getCurrent())) {
            identifier.append(getCurrent());
            next();
            while (Character.isLetterOrDigit(getCurrent())) {
                identifier.append(getCurrent());
                next();
            }
        } else {
            return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    "Identifier expected in position " + getPos());
        }
        skip();

        return new JetBrainsAstNode(JetBrainsAstNodeType.IDENTIFIER, identifier.toString());
    }

    /**
     * Integer → number like int in Java
     *
     * @return Integer OR ParseError if can't parse
     */
    private JetBrainsAstNode integer() {
        if (isReserved())
            return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    "Not proper symbol in position " + getPos());
        StringBuilder number = new StringBuilder();
        boolean ok = false;
        while (Character.isDigit(getCurrent()) || (getCurrent() == '-' && !ok) || (getCurrent() == '+' && !ok)) {
            if (getCurrent() != '+') number.append(getCurrent());
            if (getCurrent() != '-' && getCurrent() != '+') ok = true;
            next();
        }

        if (number.length() == 0 || !ok)
            // we always can replace number to identifier
            return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    "Number or identifier expected on pos " + getPos());
        while (number.charAt(0) == '-' && number.charAt(1) == '-')
            number.delete(0, 2);
        skip();
        return new JetBrainsAstNode(JetBrainsAstNodeType.INTEGER, number.toString());
    }

    /**
     * parse a source text
     *
     * @return Program node
     */
    public JetBrainsAstNode parse() {
        skip();
        return program();
    }

}
