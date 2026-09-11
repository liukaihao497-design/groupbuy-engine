package com.lkh.domain.activity.service.thread.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractTask<T> implements Callable<T> {
    private long timeout;
    private TimeUnit unit;
    public AbstractTask(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }
    public AbstractTask(){
        this.timeout = 2000;
        this.unit = TimeUnit.MILLISECONDS;
    }
    protected void setTimeout(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }
}
