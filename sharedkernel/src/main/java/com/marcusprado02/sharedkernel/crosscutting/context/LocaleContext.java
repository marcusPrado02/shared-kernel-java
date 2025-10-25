package com.marcusprado02.sharedkernel.crosscutting.context;


public final class LocaleContext {
    private static final ThreadLocal<String> LOCALE = new ThreadLocal<>();
    private LocaleContext() {}

    public static void setLocale(String locale) { LOCALE.set(locale); }
    public static String getLocale() { return LOCALE.get(); }
    public static void clear() { LOCALE.remove(); }
}