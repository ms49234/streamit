/**
 * 
 */
package at.dms.kjc.slicegraph;

/**
 * A SimpleSlice is a Slice with exactly one FilterSliceNode
 * FilterSliceNode
 * @author dimock
 *
 */
public class SimpleSlice extends Slice {

    protected FilterSliceNode body;
    
    /** Constructor: creates a slice with one filter and sets
     * previous parent and next links the supplied
     * InputSliceNode, FilterSliceNode, and OutputSliceNode.
     * <br/>
     * One of head, body, tail must be non-null.
     * If body is null, then a FilterSliceNode for the body
     * must be reachable from the head or the tail.
     * If head is null and no InputSliceNode is connected to body,
     * then a default InputSliceNode is generated.
     * If tail is null and no OutputSliceNode is connected to body,
     * then a default OutputSliceNode is created.
     * @param head InputSliceNode at head of slice
     * @param body FilterSliceNode in simple slice.
     * @param tail OutputSliceNode at tail of slice.
     */
    public SimpleSlice(InputSliceNode head, 
       FilterSliceNode body, OutputSliceNode tail) {
            if (body == null && head != null) {
                body = (FilterSliceNode)head.getNext();
            }
            if (tail == null && body != null) {
                tail = (OutputSliceNode)body.getNext();
            }
            if (body == null && tail != null) {
                body = (FilterSliceNode)tail.getPrevious();
            }
            if (head == null && body != null) {
                head = (InputSliceNode)body.getPrevious();
            }
            if (head == null) {
                head = new InputSliceNode();
            }
            if (tail == null) {
                tail = new OutputSliceNode();
            }
            assert body != null : "SimpleSlice must be created with a non-null body.";
            
            this.head = head;
            this.body = body; 
            this.tail = tail;
            head.setParent(this);
            body.setParent(this);
            tail.setParent(this);
            len = 1;
            head.setPrevious(null);
            head.setNext(body);
            body.setPrevious(head);
            body.setNext(tail);
            tail.setPrevious(body);
            tail.setNext(null);
    }
    
    /**
     * @param head
     */
    public SimpleSlice(InputSliceNode head) {
        this(head,null,null);
    }

    /**
     * @param node
     */
    public SimpleSlice(SliceNode node) {
        this(null,(FilterSliceNode)node,null);
    }

    /**
     * Not needed for SimpleSlice, kept as a sanity check.
     */
    public int finish() {
        assert head.getNext() == body.getPrevious() 
        && tail.getPrevious() == body.getNext()
        && head.getParent() == this 
        && filterNodes[0].getParent() == this
        && tail.getParent() == this;
        return 1;
    }
    
    /**
     * Should not need to be called for SimpleSlice: always 1.
     */
    public int getNumFilters() {
        return 1;
    }
    
    /**
     * For SimpleSlice: call {@link #getBody()} instead.
     * For compatability with Slice, returns a one-element array of
     * FilterSliceNode, but unlike {@link Slice#getFilterNodes()}
     * changes to the array are not reflected in this.  
     */
    public FilterSliceNode[] getFilterNodes() {
        return new FilterSliceNode[]{body};
    }

    /**
     * Preferred way to access body of a SimpleSlice.
     * @return the FilterSliceNode
     */
    public FilterSliceNode getBody() {
        return body;
    }
    
    /**
     * Set the body.
     * Updates parent pointer in the body, but not the previous
     * or next pointers.
     * @param body a FilterSliceNode.
     */
    public void setBody(FilterSliceNode body) {
        this.body = body; 
        body.setParent(this);
    }
}