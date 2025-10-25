package com.marcusprado02.sharedkernel.crosscutting.sanitize.rules;


import java.util.regex.Pattern;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

public final class StringValidations {
    public static RejectIf<String> notBlank(){ return (s,ctx)-> { if (s==null || s.isBlank()) throw new SanitizationException("blank"); }; }
    public static RejectIf<String> maxLength(int n){ return (s,ctx)-> { if (s!=null && s.length()>n) throw new SanitizationException("too-long"); }; }
    public static RejectIf<String> regex(String pattern){
        var p = Pattern.compile(pattern);
        return (s,ctx)-> { if (s!=null && !p.matcher(s).matches()) throw new SanitizationException("regex-mismatch"); };
    }
}
