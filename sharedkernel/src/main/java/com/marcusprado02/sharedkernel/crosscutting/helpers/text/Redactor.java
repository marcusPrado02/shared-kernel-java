package com.marcusprado02.sharedkernel.crosscutting.helpers.text;

public final class Redactor {
  private Redactor(){}
  public static String redactToken(String s) {
    if (s == null || s.length() < 8) return "****";
    return s.substring(0,4) + "****" + s.substring(s.length()-4);
  }
  public static String redactEmail(String mail) {
    if (mail==null || !mail.contains("@")) return "****";
    var parts = mail.split("@",2);
    String u = parts[0]; String d = parts[1];
    String u2 = u.length() <= 2 ? "*" : u.charAt(0)+"*"+u.charAt(u.length()-1);
    return u2 + "@" + d;
  }
}

