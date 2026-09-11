package com.lkh.types.design.framework;

public interface StrategyHandler<T,D,R> {

    R apply(T requestparam,D context) throws Exception;
}
