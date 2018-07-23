package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.context.Context;

/**
 * Each {@link SphU}#entry() will return an {@link Entry}. This class holds information of current invocation:<br/>
 *
 * <ul>
 * <li>createTime, the create time of this entry, using for rt statistics.</li>
 * <li>current {@link Node}, that is statistics of the resource in current context.</li>
 * <li>origin {@link Node}, that is statistics for the specific origin. Usually the
 * origin could be the Service Consumer's app name, see
 * {@link ContextUtil#enter(String name, String origin)} </li>
 * <li>{@link ResourceWrapper}, that is resource name.</li>
 * <br/>
 * </ul>
 *
 * <p>
 * A invocation tree will be created if we invoke SphU#entry() multi times in the same {@link Context},
 * so parent or child entry may be held by this to form the tree. Since {@link Context} always holds
 * the current entry in the invocation tree, every {@link Entry#exit()} call should modify
 * {@link Context#setCurEntry(Entry)} as parent entry of this.
 * </p>
 *
 * @author qinan.qn
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @see SphU
 * @see Context
 * @see ContextUtil
 */
public abstract class Entry {

    private static final Object[] OBJECTS0 = new Object[0];

    private long createTime;
    private Node curNode;
    /**
     * {@link Node} of the specific origin, Usually the origin is the Service Consumer.
     */
    private Node originNode;
    private Throwable error;
    protected ResourceWrapper resourceWrapper;

    public Entry(ResourceWrapper resourceWrapper) {
        this.resourceWrapper = resourceWrapper;
        this.createTime = TimeUtil.currentTimeMillis();
    }

    public ResourceWrapper getResourceWrapper() {
        return resourceWrapper;
    }

    public void exit() throws ErrorEntryFreeException {
        exit(1, OBJECTS0);
    }

    public void exit(int count) throws ErrorEntryFreeException {
        exit(count, OBJECTS0);
    }

    /**
     * Exit this entry. This method should invoke if and only if once at the end of the resource protection.
     *
     * @param count tokens to release.
     * @param args
     * @throws ErrorEntryFreeException, if {@link Context#getCurEntry()} is not this entry.
     */
    public abstract void exit(int count, Object... args) throws ErrorEntryFreeException;

    /**
     * Exit this entry.
     *
     * @param count tokens to release.
     * @param args
     * @return next available entry after exit, that is the parent entry.
     * @throws ErrorEntryFreeException, if {@link Context#getCurEntry()} is not this entry.
     */
    protected abstract Entry trueExit(int count, Object... args) throws ErrorEntryFreeException;

    /**
     * Get related {@link Node} of the parent {@link Entry}.
     *
     * @return
     */
    public abstract Node getLastNode();

    public long getCreateTime() {
        return createTime;
    }

    public Node getCurNode() {
        return curNode;
    }

    public void setCurNode(Node node) {
        this.curNode = node;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    /**
     * Get origin {@link Node} of the this {@link Entry}.
     *
     * @return origin {@link Node} of the this {@link Entry}, may be null if no origin specified by
     * {@link ContextUtil#enter(String name, String origin)}.
     */
    public Node getOriginNode() {
        return originNode;
    }

    public void setOriginNode(Node originNode) {
        this.originNode = originNode;
    }

}