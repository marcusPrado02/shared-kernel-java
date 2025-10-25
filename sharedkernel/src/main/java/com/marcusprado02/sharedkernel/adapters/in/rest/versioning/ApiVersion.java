package com.marcusprado02.sharedkernel.adapters.in.rest.versioning;


public record ApiVersion(int major, int minor) implements Comparable<ApiVersion> {
    public static ApiVersion of(int major, int minor){ return new ApiVersion(major, minor); }
    public static ApiVersion parse(String s){
        // "1", "1.2", "v2", "v2.1"
        var t = s.trim().replaceFirst("^[vV]", "");
        var parts = t.split("\\.");
        int M = Integer.parseInt(parts[0]);
        int m = parts.length>1 ? Integer.parseInt(parts[1]) : 0;
        return new ApiVersion(M,m);
    }
    @Override public int compareTo(ApiVersion o){ int c = Integer.compare(major, o.major); return c!=0?c:Integer.compare(minor, o.minor); }
    @Override public String toString(){ return major + (minor>0? "."+minor:""); }
}

