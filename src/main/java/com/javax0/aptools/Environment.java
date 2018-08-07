package com.javax0.aptools;

import javax.annotation.processing.ProcessingEnvironment;

public final class Environment {
    static final private ThreadLocal<ProcessingEnvironment> processingEnvironment = new ThreadLocal<>();

    public static void set(final ProcessingEnvironment pe) {
        processingEnvironment.set(pe);
    }

    public static ProcessingEnvironment get() {
        return processingEnvironment.get();
    }
}
