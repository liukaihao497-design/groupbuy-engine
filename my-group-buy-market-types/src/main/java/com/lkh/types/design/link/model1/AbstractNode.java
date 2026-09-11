package com.lkh.types.design.link.model1;

public abstract class AbstractNode<T,D,R> implements IChainArmory<T,D,R> {

    IHandlerNode<T,D,R> nextHandler;
    @Override
    public IHandlerNode<T,D,R> next() {
        return nextHandler;
    }

    public R  next(T requestParam, D context) {

        return nextHandler.apply(requestParam, context);
    }

    @Override
    public void appendNext(IHandlerNode<T, D, R> node) {
        this.nextHandler = node;
    }
}
