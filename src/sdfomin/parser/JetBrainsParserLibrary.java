package sdfomin.parser;

import java.util.function.Function;

public class JetBrainsParserLibrary {

    /**
     * Count the number of nodes in current node that applies a checker
     *
     * @param node    node
     * @param checker function that returns true if we should count node
     * @return number of nodes that satisfy checker
     */
    public static int countNodes(JetBrainsAstNode node,
                                 Function<JetBrainsAstNode, Boolean> checker) {
        int res = 0;
        for (var x : node.getChildren()) {
            if (checker.apply(x))
                ++res;
        }
        return res;
    }

    /**
     * Recursive deep count the number of nodes that applies a checker
     *
     * @param node    root node
     * @param checker function that returns true if we should count node
     * @return number of nodes that satisfy checker
     */
    public static int countDeepNodes(JetBrainsAstNode node, Function<JetBrainsAstNode, Boolean> checker) {
        int res = countNodes(node, checker);
        for (var x : node.getChildren())
            res += countDeepNodes(x, checker);
        return res;
    }

    /**
     * checks if number of IF_STATEMENT with brackets has increased
     *
     * @param first  first node
     * @param second second node
     * @return true if number has increased, false otherwise
     */
    public static boolean isBlockStatementUpdated(JetBrainsAstNode first, JetBrainsAstNode second) {
        Function<JetBrainsAstNode, Boolean> isComplexIf = (node ->
                node.getType() == JetBrainsAstNodeType.IF_STATEMENT &&
                        // node is BLOCK_STATEMENT and it's STATEMENT_LIST contains elements
                        node.getChild(1).getChild(0).getType() == JetBrainsAstNodeType.BLOCK_STATEMENT &&
                        node.getChild(1).getChild(0).getChild(0).getChildren().size() > 0);

        Function<JetBrainsAstNode, Boolean> isIf = (node ->
                node.getType() == JetBrainsAstNodeType.IF_STATEMENT);

        return (countDeepNodes(first, isComplexIf) < countDeepNodes(second, isComplexIf)) &&
                (countDeepNodes(first, isIf) < countDeepNodes(second, isIf));
    }

    /**
     * counts statements to understand if we should update the tree
     *
     * @param node root node
     * @return number of statements with some corrections
     */
    public static int countIsTreeUpdated(JetBrainsAstNode node) {
        int res = 0;
        for (var x : node.getChildren()) {

            if (x.getType() != JetBrainsAstNodeType.STATEMENT ||
                    x.getChild(0).getType() == JetBrainsAstNodeType.PARSE_ERROR) {
                res += countIsTreeUpdated(x);
                continue;
            }

            JetBrainsAstNode statementNode = x.getChild(0);
            if (statementNode.getType() != JetBrainsAstNodeType.IF_STATEMENT) {
                res += countIsTreeUpdated(statementNode) + 1;
                continue;
            }

            JetBrainsAstNode ifStatementNode = statementNode.getChild(1).getChild(0);
            if (ifStatementNode.getType() == JetBrainsAstNodeType.IF_STATEMENT ||
                    ifStatementNode.getType() == JetBrainsAstNodeType.PARSE_ERROR)
                res += countIsTreeUpdated(ifStatementNode);
            else
                res += countIsTreeUpdated(ifStatementNode) + 1;

        }
        return res;
    }

    /**
     * Returns true if we should update a tree
     * We should update tree if user added new statement and it isn't:
     * IF_STATEMENT which contains not a BLOCK_STATEMENT
     *
     * @param first  first root node
     * @param second second root node
     * @return true if we should update
     */
    public static boolean isTreeUpdated(JetBrainsAstNode first, JetBrainsAstNode second) {
        return countIsTreeUpdated(first) != countIsTreeUpdated(second);
    }

    /**
     * prints a tree of node in console
     *
     * @param x number of spaces
     */
    public static void printTree(JetBrainsAstNode node, int x) {
        for (int i = 0; i < x; ++i)
            System.out.print(' ');
        System.out.println(node.getType());
        for (var i : node.getChildren())
            printTree(i, x + 2);
    }
}
