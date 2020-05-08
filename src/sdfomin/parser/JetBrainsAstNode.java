package sdfomin.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodes of AST-tree
 * Grammar took from JetBrains task
 */
public class JetBrainsAstNode {

    /**
     * type of node
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
     *
     * @param type     type of node
     * @param text     additional information
     * @param children children nodes
     */
    public JetBrainsAstNode(JetBrainsAstNodeType type, String text, JetBrainsAstNode... children) {
        this.type = type;
        this.text = text;
        for (var i : children)
            if (i != null)
                this.children.add(i);
    }

    public JetBrainsAstNode(JetBrainsAstNodeType type, JetBrainsAstNode... children) {
        this(type, null, children);
    }

    public JetBrainsAstNode(JetBrainsAstNodeType type) {
        this(type, (String) null);
    }

    /**
     * add child to a node
     *
     * @param child child node
     */
    public void addChild(JetBrainsAstNode child) {
        children.add(child);
    }

    /**
     * get child from node
     *
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

