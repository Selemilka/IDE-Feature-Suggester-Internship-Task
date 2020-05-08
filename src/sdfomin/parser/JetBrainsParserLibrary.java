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

            if (x.getType() != JetBrainsAstNodeType.STATEMENT)
                res += countIsTreeUpdated(x);

            else if (x.getChild(0).getType() == JetBrainsAstNodeType.IF_STATEMENT &&
                    x.getChild(0).getChild(1).getChild(0).getType() == JetBrainsAstNodeType.BLOCK_STATEMENT)
                res += countIsTreeUpdated(x.getChild(0).getChild(1)) + 1;
            else if (x.getChild(0).getType() == JetBrainsAstNodeType.IF_STATEMENT &&
                    x.getChild(0).getChild(1).getChild(0).getType() == JetBrainsAstNodeType.IF_STATEMENT)
                res += countIsTreeUpdated(x.getChild(0).getChild(1));
            else if (x.getChild(0).getType() == JetBrainsAstNodeType.IF_STATEMENT &&
                    x.getChild(0).getChild(1).getChild(0).getType() == JetBrainsAstNodeType.PARSE_ERROR)
                res += countIsTreeUpdated(x.getChild(0).getChild(1));
            else if (x.getChild(0).getType() == JetBrainsAstNodeType.IF_STATEMENT)
                res += countIsTreeUpdated(x.getChild(0).getChild(1)) + 1;

            else if (x.getChild(0).getType() != JetBrainsAstNodeType.PARSE_ERROR)
                res += countIsTreeUpdated(x) + 1;
            else
                res += countIsTreeUpdated(x);
        }
        return res;
    }

    /**
     * Returns true if we should update a tree
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
