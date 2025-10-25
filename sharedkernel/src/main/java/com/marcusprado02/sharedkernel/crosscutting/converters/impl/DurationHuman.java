package com.marcusprado02.sharedkernel.crosscutting.converters.impl;

import java.time.Duration;
import java.util.regex.Pattern;

import com.marcusprado02.sharedkernel.crosscutting.converters.core.*;

public final class DurationHuman implements BidiConverter<Duration,String> {
    @Override public String convert(Duration d) {
        long ms = d.toMillis(); long d_ = ms/86_400_000; ms%=86_400_000;
        long h = ms/3_600_000; ms%=3_600_000; long m = ms/60_000; ms%=60_000; long s = ms/1000;
        var sb = new StringBuilder();
        if (d_>0) sb.append(d_).append("d");
        if (h>0) sb.append(h).append("h");
        if (m>0) sb.append(m).append("m");
        if (s>0) sb.append(s).append("s");
        if (sb.length()==0) sb.append("0s");
        return sb.toString();
    }
    @Override public Converter<String,Duration> inverse() {
        return s -> { // parser simples compatível
            var m = Pattern.compile("(\\d+)(ms|s|m|h|d)").matcher(s.replaceAll("\\s+",""));
            long total=0; int idx=0; boolean any=false;
            while(m.find(idx)){ any=true; long n=Long.parseLong(m.group(1));
                total += switch(m.group(2)) { case "ms" -> n; case "s"-> n*1000; case "m"-> n*60_000; case "h"-> n*3_600_000; case "d"-> n*86_400_000; default->0; };
                idx=m.end();
            }
            if(!any) throw new ConversionException("Invalid duration: "+s);
            return Duration.ofMillis(total);
        };
    }
}

