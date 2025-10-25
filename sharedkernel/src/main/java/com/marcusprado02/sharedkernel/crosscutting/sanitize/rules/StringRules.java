package com.marcusprado02.sharedkernel.crosscutting.sanitize.rules;


import java.text.Normalizer;
import java.util.regex.Pattern;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.Rule;

public final class StringRules {
    private static final Pattern ZWS = Pattern.compile("[\\u200B-\\u200D\\uFEFF]"); // zero width
    private static final Pattern BIDI = Pattern.compile("[\\u202A-\\u202E\\u2066-\\u2069]"); // bidi marks
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern MULTISPACE = Pattern.compile("\\s+");
    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{Print}]");

    public static Rule<String> trim(){ return (s,ctx) -> s==null?null:s.trim(); }
    public static Rule<String> collapseWhitespace(){ return (s,ctx)-> s==null?null: MULTISPACE.matcher(s.trim()).replaceAll(" "); }
    public static Rule<String> stripZeroWidth(){ return (s,ctx)-> s==null?null: ZWS.matcher(s).replaceAll(""); }
    public static Rule<String> stripBidi(){ return (s,ctx)-> s==null?null: BIDI.matcher(s).replaceAll(""); }
    public static Rule<String> stripControl(){ return (s,ctx)-> s==null?null: CONTROL.matcher(s).replaceAll(""); }
    public static Rule<String> toNFC(){ return (s,ctx)-> s==null?null: Normalizer.normalize(s, Normalizer.Form.NFC); }
    public static Rule<String> toNFKC(){ return (s,ctx)-> s==null?null: Normalizer.normalize(s, Normalizer.Form.NFKC); }

    public static Rule<String> maxLength(int n){ return (s,ctx)-> s==null?null: (s.length()<=n? s : s.substring(0,n)); }

    public static Rule<String> allowRegex(String regex){
        var p = Pattern.compile(regex);
        return (s,ctx)-> s==null?null: s.chars()
                .mapToObj(c->String.valueOf((char)c))
                .filter(ch -> p.matcher(ch).matches())
                .reduce("", String::concat);
    }

    public static Rule<String> denyChars(String chars){
        var p = Pattern.compile("[" + Pattern.quote(chars) + "]");
        return (s,ctx)-> s==null?null: p.matcher(s).replaceAll("");
    }

    public static Rule<String> lowerCase(){ return (s,ctx)-> s==null?null: s.toLowerCase(ctx.locale()); }
    public static Rule<String> upperCase(){ return (s,ctx)-> s==null?null: s.toUpperCase(ctx.locale()); }
}