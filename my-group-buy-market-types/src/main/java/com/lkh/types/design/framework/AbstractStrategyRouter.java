package com.lkh.types.design.framework;

public abstract class AbstractStrategyRouter<T,D,R> implements StrategyHandler<T,D,R>,StrategyMapper<T,D,R>{

    protected StrategyHandler<T,D,R> defaultStrategyHandler = (T,D)->{return null;};
    protected R router(T requestparam,D context) throws Exception{
        StrategyHandler<T, D, R> next = get(requestparam,context);
        if(null != next){
            R r = next.apply(requestparam, context);
            return r;
        }
        return null;
    }

}
