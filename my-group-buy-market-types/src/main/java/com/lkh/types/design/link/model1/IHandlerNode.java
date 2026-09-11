package com.lkh.types.design.link.model1;

public interface IHandlerNode<T,D,R> {
    R apply(T requestParam,D context);
}
