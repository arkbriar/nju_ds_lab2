package utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class TreeNode<T> {
    private TreeNode<T> parent;
    private T value;
    private Set<TreeNode<T>> childrenSet = new HashSet<>();

    public TreeNode(T value) {
        this(null, value);
    }

    public TreeNode(TreeNode<T> parent, T value) {
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
    public TreeNode<T> getParent() {
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

    public boolean removeChild(T value) {
        return childrenSet.remove(new TreeNode<T>(value));
    }

    public boolean removeChild(TreeNode<T> node) {
        return childrenSet.remove(node);
    }

    public void removeAllChild() {
        childrenSet.clear();
    }

    public TreeNode<T> addChild(T value) {
        TreeNode<T> newNode = new TreeNode<>(this, value);
        childrenSet.add(newNode);
        return newNode;
    }

    public TreeNode<T> addChild(TreeNode<T> child) {
        child.setParent(this);
        childrenSet.add(child);
        return child;
    }

    public void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TreeNode)) {
            return false;
        }
        final TreeNode other = (TreeNode) obj;
        return value.getClass().isAssignableFrom(other.value.getClass())
            && Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
