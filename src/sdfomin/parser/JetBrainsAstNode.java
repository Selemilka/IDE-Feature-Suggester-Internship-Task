package sdfomin.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodes of AST-tree
 * Grammar took from JetBrains task
 */
public class JetBrainsAstNode {

    /**
     *  type of node
     */
    private final JetBrainsAstNodeType type;

    /**
     * additional information about node (integers, error messages, identifiers, operators)
     */
    private final String text;

    /**
     * children of node
     */
    private final List<JetBrainsAstNode> children = new ArrayList<>();

    /**
     * construct a node
     * @param type type of node
     * @param text additional information
     * @param child1 first child
     * @param child2 second child
     */
    public JetBrainsAstNode(JetBrainsAstNodeType type, String text, JetBrainsAstNode child1, JetBrainsAstNode child2) {
        this.type = type;
        this.text = text;
        if (child1 != null)
            addChild(child1);
        if (child2 != null)
            addChild(child2);
    }

    public JetBrainsAstNode(JetBrainsAstNodeType type, JetBrainsAstNode child1, JetBrainsAstNode child2) {
        this(type, null, child1, child2);
    }

    public JetBrainsAstNode(JetBrainsAstNodeType type, JetBrainsAstNode child1) {
        this(type, child1, null);
    }

    public JetBrainsAstNode(JetBrainsAstNodeType type, String text) {
        this(type, text, null, null);
    }

    public JetBrainsAstNode(JetBrainsAstNodeType type) {
        this(type, (String) null);
    }

    /**
     * add child to a node
     * @param child child node
     */
    public void addChild(JetBrainsAstNode child) {
        children.add(child);
    }

    /**
     * get child from node
     * @param index index of child node
     * @return child node
     */
    public JetBrainsAstNode getChild(int index) {
        return children.get(index);
    }

    /**
     * @return number of children
     */
    public int childCount() {
        return children.size();
    }

    public JetBrainsAstNodeType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text != null ? text : type.toString();
    }

    public List<JetBrainsAstNode> getChildren() {
        return children;
    }
}

