package sdfomin.parser;

public class JetBrainsParserLibrary {

    /**
     * Counts number of if(...) {...} with brackets
     * @param node checking node
     * @return number of if statements with brackets in node
     */
    private static int countBracketsIfStatement(JetBrainsAstNode node) {
        int res = 0;
        if (node.getType() == JetBrainsAstNodeType.IF_STATEMENT
                && node.getChild(1).getType() == JetBrainsAstNodeType.BLOCK_STATEMENT)
            ++res;

        for (var x : node.getChildren()) {
            res += countBracketsIfStatement(x);
        }
        return res;
    }

    /**
     * checks if number of if statements with brackets has increased
     * @param first first node
     * @param second second node
     * @return true if number has increased, false otherwise
     */
    public static boolean isBlockStatementUpdated(JetBrainsAstNode first, JetBrainsAstNode second) {
        return countBracketsIfStatement(first) < countBracketsIfStatement(second);
    }

    /**
     * prints a tree of node in console
     * @param x number of spaces
     */
    public static void printTree(JetBrainsAstNode node, int x) {
        for (int i = 0; i < x; ++i)
            System.out.print(' ');
        System.out.println(node.getType());
        for (var i : node.getChildren())
            printTree(i, x + 2);
    }

    /**
     * prints a tree of node in console
     * @param node node to print
     */
    public static void printTree(JetBrainsAstNode node) {
        printTree(node, 0);
    }
}
