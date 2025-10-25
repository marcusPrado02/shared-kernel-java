package com.marcusprado02.sharedkernel.observability.dashboard.model;


public record Threshold(double value, String color, String op) { // op: ">", "<", ">=", "<="
    public static Threshold warn(double v){ return new Threshold(v, "orange", ">"); }
    public static Threshold crit(double v){ return new Threshold(v, "red", ">"); }
}