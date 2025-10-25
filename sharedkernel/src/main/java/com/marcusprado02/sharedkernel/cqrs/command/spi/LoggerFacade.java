package com.marcusprado02.sharedkernel.cqrs.command.spi;

public interface LoggerFacade {
    void info(String msg, String... kvPairs);
    void warn(String msg, String... kvPairs);
    void error(String msg, Throwable t, String... kvPairs);

    static LoggerFacade slf4j(org.slf4j.Logger log){
        return new LoggerFacade() {
            private String fmt(String msg, String... kv){
                if (kv == null || kv.length == 0) return msg;
                var sb = new StringBuilder(msg).append(" ");
                for (int i=0;i<kv.length;i+=2) {
                    sb.append(kv[i]).append("=").append(i+1<kv.length?kv[i+1]:"").append(" ");
                }
                return sb.toString().trim();
            }
            public void info(String m, String...k){ log.info(fmt(m,k)); }
            public void warn(String m, String...k){ log.warn(fmt(m,k)); }
            public void error(String m, Throwable t, String...k){ log.error(fmt(m,k), t); }
        };
    }
}
