package sdfomin.parser;

import java.text.ParseException;

public class JetBrainsParser extends ParserBase {

    public JetBrainsParser(String source) {
        super(source);
    }

    /**
     * Program → StatementList
     * @return created node PROGRAM
     */
    private JetBrainsAstNode program() {
        JetBrainsAstNode res = new JetBrainsAstNode(JetBrainsAstNodeType.PROGRAM, statementList());
        if (!isEnd())
            res.getChild(0).addChild(new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                    "Extra symbol on pos " + getPos()));
        return res;
    }

    /**
     * StatementList → empty | StatementList Statement
     * @return StatementList node
     */
    private JetBrainsAstNode statementList() {
        JetBrainsAstNode statementNode = new JetBrainsAstNode(JetBrainsAstNodeType.STATEMENT_LIST);
        while (!isEnd() && !isMatch("}")) {
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
     * @return ConditionExpression node
     */
    private JetBrainsAstNode expression() {
        JetBrainsAstNode conditionExpression = conditionExpression();
        return new JetBrainsAstNode(JetBrainsAstNodeType.EXPRESSION, conditionExpression);
    }

    /**
     * ConditionExpression -> PlusMinusExpression | PlusMinusExpression < PlusMinusExpression
     * | PlusMinusExpression > PlusMinusExpression
     * @return ConditionExpression node OR ParseError if can't parse
     */
    private JetBrainsAstNode conditionExpression() {
        JetBrainsAstNode result = plusMinusExpression();
        while (isMatch("<", ">")) {
            String operation;
            try {
                operation = match("<", ">");
            } catch (ParseException ex) {
                return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                        ex.getMessage() + " in position " + ex.getErrorOffset());
            }
            JetBrainsAstNode expression = plusMinusExpression();
            result = new JetBrainsAstNode(JetBrainsAstNodeType.CONDITION_EXPRESSION, operation, result, expression);
        }
        return result;
    }

    /**
     * PlusMinusExpression → MultiplyDivisionExpression | PlusMinusExpression + MultiplyDivisionExpression
     * | PlusMinusExpression - MultiplyDivisionExpression
     * @return PlusMinusExpression node OR ParseError if can't parse
     */
    private JetBrainsAstNode plusMinusExpression() {
        JetBrainsAstNode result = multiplyDivisionExpression();
        while (isMatch("+", "-")) {
            String operation;
            try {
                operation = match("+", "-");
            } catch (ParseException ex) {
                return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                        ex.getMessage() + " in position " + ex.getErrorOffset());
            }
            JetBrainsAstNode expression = multiplyDivisionExpression();
            result = new JetBrainsAstNode(JetBrainsAstNodeType.PLUS_MINUS_EXPRESSION, operation, result, expression);
        }
        return result;
    }

    /**
     * MultiplyDivisionExpression → SimpleExpression | MultiplyDivisionExpression * SimpleExpression
     * | MultiplyDivisionExpression / SimpleExpression
     * @return MultiplyDivisionExpression node OR ParseError if can't parse
     */
    private JetBrainsAstNode multiplyDivisionExpression() {
        JetBrainsAstNode result = simpleExpression();
        while (isMatch("*", "/")) {
            String operation;
            try {
                operation = match("*", "/");
            } catch (ParseException ex) {
                return new JetBrainsAstNode(JetBrainsAstNodeType.PARSE_ERROR,
                        ex.getMessage() + " in position " + ex.getErrorOffset());
            }
            JetBrainsAstNode expression = simpleExpression();
            result = new JetBrainsAstNode(JetBrainsAstNodeType.MULTIPLY_DIVISION_EXPRESSION, operation, result, expression);
        }
        return result;
    }

    /**
     * SimpleExpression → Identifier | Integer | ( Expression )
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
     * @return true if reserved
     */
    private boolean isReserved() {
        return isMatch("if", "(", ")", "{", "}", "<", ">", "*", "/");
    }

    /**
     * Identifier → Identifier created from latin letters
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
     * @return Program node
     */
    public JetBrainsAstNode parse() {
        skip();
        return program();
    }

    /**
     * parse a source text
     * @param source source text
     * @return Program node
     */
    public static JetBrainsAstNode parse(String source) {
        JetBrainsParser mlp = new JetBrainsParser(source);
        return mlp.parse();
    }

}
