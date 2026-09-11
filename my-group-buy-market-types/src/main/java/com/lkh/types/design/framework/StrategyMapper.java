package com.lkh.types.design.framework;

public interface StrategyMapper<T,D,R> {

    StrategyHandler<T,D,R> get(T requestparam,D context) throws Exception;
}
