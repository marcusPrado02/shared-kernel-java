package com.marcusprado02.sharedkernel.crosscutting.policies.core;


import org.aspectj.lang.ProceedingJoinPoint;

public record CallContext(Subject subject, Resource resource, Environment env) {

    public static CallContext from(ProceedingJoinPoint pjp, Policy pol) {
        Subject s = findArg(pjp, Subject.class);
        Resource r = findArg(pjp, Resource.class);
        Environment e = findArg(pjp, Environment.class);

        if (s == null || r == null || e == null) {
            String msg = "Policy context incomplete: "
                    + "subject=" + (s != null) + ", resource=" + (r != null) + ", env=" + (e != null)
                    + ". Ensure the advised method receives these types as parameters "
                    + "(Subject, Resource, Environment) or provide an extractor.";
            throw new IllegalStateException(msg);
        }
        return new CallContext(s, r, e);
    }

    @SuppressWarnings("unchecked")
    private static <T> T findArg(ProceedingJoinPoint pjp, Class<T> type) {
        for (Object a : pjp.getArgs()) {
            if (type.isInstance(a)) return (T) a;
        }
        return null;
    }
}
