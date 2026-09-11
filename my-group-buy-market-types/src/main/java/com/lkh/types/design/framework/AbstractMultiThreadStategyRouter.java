package com.lkh.types.design.framework;

public abstract class AbstractMultiThreadStategyRouter<T,D,R> extends AbstractStrategyRouter<T,D,R>{


    protected abstract void multiThreadLoadContext(T requestparamj,D context) throws Exception;
    public abstract R doApply(T requestparam, D context) throws Exception;
    @Override
    public R apply(T requestparam, D context) throws Exception{
        multiThreadLoadContext(requestparam, context);
        return doApply(requestparam, context);
    }

}
