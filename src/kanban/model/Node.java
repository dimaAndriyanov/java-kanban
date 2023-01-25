package kanban.model;

public class Node<T> {
    private final T value;
    private Node<T> prev;
    private Node<T> next;

    public Node(T value) {
        this.value = value;
        prev = null;
        next = null;
    }

    public T getValue() {
        return value;
    }

    public Node<T> getPrevious() {
        return prev;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setPrevious(Node<T> node) {
        prev = node;
    }

    public void setNext(Node<T> node) {
        next = node;
    }
}