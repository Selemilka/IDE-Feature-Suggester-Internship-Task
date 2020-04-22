package sdfomin.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JetBrainsInterpreter {

    /**
     * table of variables in program
     */
    private final Map<String, Integer> varTable = new HashMap<>();

    /**
     * main program node
     */
    private final JetBrainsAstNode programNode;

    /**
     * output of a program
     */
    private final ArrayList<Integer> output = new ArrayList<>();

    /**
     * constructor
     *
     * @param programNode Program node
     */
    public JetBrainsInterpreter(JetBrainsAstNode programNode) {
        if (programNode.getType() != JetBrainsAstNodeType.PROGRAM)
            throw new IllegalArgumentException("AST-tree is not a PROGRAM!");
        this.programNode = programNode;
    }

    /**
     * execute node
     *
     * @param node node to execute
     * @return result of executing if needed
     * @throws InterpretException if has ParseErrors
     */
    private int executeNode(JetBrainsAstNode node) throws InterpretException {
        switch (node.getType()) {

            case UNKNOWN:
                throw new IllegalArgumentException("Undefined type of AST-node!");
            case INTEGER:
                return Integer.parseInt(node.getText());
            case IDENTIFIER:
                if (varTable.containsKey(node.getText()))
                    return varTable.get(node.getText());
                else
                    throw new InterpretException("Undeclared identifier");
            case EXPRESSION:
            case STATEMENT:
            case PROGRAM:
            case BLOCK_STATEMENT:
            case SIMPLE_EXPRESSION:
                return executeNode(node.getChild(0));
            case CONDITION_EXPRESSION:
                if (node.getText().equals("<")) {
                    return (executeNode(node.getChild(0)) < executeNode(node.getChild(1))) ? 1 : 0;
                } else {
                    return (executeNode(node.getChild(0)) > executeNode(node.getChild(1))) ? 1 : 0;
                }
            case PLUS_MINUS_EXPRESSION:
                if (node.getText().equals("+"))
                    return executeNode(node.getChild(0)) + executeNode(node.getChild(1));
                else
                    return executeNode(node.getChild(0)) - executeNode(node.getChild(1));
            case MULTIPLY_DIVISION_EXPRESSION:
                if (node.getText().equals("*"))
                    return executeNode(node.getChild(0)) * executeNode(node.getChild(1));
                else
                    return executeNode(node.getChild(0)) / executeNode(node.getChild(1));
            case EXPRESSION_STATEMENT: // PRINT
                int res = executeNode(node.getChild(0));
                output.add(res);
                return res;
            case IF_STATEMENT:
                if (executeNode(node.getChild(0)) != 0)
                    return executeNode(node.getChild(1));
                return 0;
            case ASSIGN_STATEMENT:
                varTable.put(node.getChild(0).getText(), executeNode(node.getChild(1)));
                return varTable.get(node.getChild(0).getText());
            case STATEMENT_LIST:
                for (int i = 0; i < node.childCount(); ++i)
                    executeNode(node.getChild(i));
                return 1;
            case PARSE_ERROR:
                throw new InterpretException(node.getText());
            default:
                throw new IllegalArgumentException("Unknown type of operation");
        }
    }

    /**
     * execute a program
     *
     * @return output of a program
     * @throws InterpretException if program has ParseErrors
     */
    public ArrayList<Integer> execute() throws InterpretException {
        executeNode(programNode);
        return output;
    }

    /**
     * execute a program
     *
     * @param programNode Program node to execute
     * @return output of a program
     * @throws InterpretException if program has ParseErrors
     */
    public static ArrayList<Integer> execute(JetBrainsAstNode programNode) throws InterpretException {
        JetBrainsInterpreter mei = new JetBrainsInterpreter(programNode);
        return mei.execute();
    }

}
