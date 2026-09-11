package com.lkh.types.design.link.model1;

public interface IChainArmory<T,D,R>  extends IHandlerNode<T,D,R>{
    void appendNext(IHandlerNode<T,D,R> node);
    IHandlerNode<T,D,R> next();
}
