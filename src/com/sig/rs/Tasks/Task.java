package com.sig.rs.Tasks;

import org.powerbot.script.ClientAccessor;
import org.powerbot.script.rt6.ClientContext;

public abstract class Task<C extends ClientContext> extends ClientAccessor<C> {
    public Task(C ctx) {
        super(ctx);
    }

    public abstract boolean activate();
    public abstract void execute();
}