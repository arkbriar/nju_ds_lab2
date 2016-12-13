package utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class TreeNode<T> {
    private TreeNode parent;
    private T value;
    private Set<TreeNode<T>> childrenSet = new HashSet<>();

    public TreeNode(T value) {
        this(null, value);
    }

    public TreeNode(TreeNode parent, T value) {
        this.parent = parent;
        this.value = value;
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount() {
        return childrenSet.size();
    }

    /**
     * Returns the parent <code>TreeNode</code> of the receiver.
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Returns true if the receiver is a leaf.
     */
    public boolean isLeaf() {
        return childrenSet.isEmpty();
    }

    /**
     * Returns the children of the receiver as an <code>Enumeration</code>.
     */
    public Set<TreeNode<T>> children() {
        return childrenSet;
    }

    public TreeNode<T> addChild(T value) {
        TreeNode<T> newNode = new TreeNode<T>(this, value);
        childrenSet.add(newNode);
        return newNode;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public T getValue() {
        return value;
    }
}
