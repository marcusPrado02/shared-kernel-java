package com.marcusprado02.sharedkernel.crosscutting.exception.core;

public final class FieldErrorDTO {
    public final String field;
    public final String message;

    public FieldErrorDTO(String f,String m){
        field=f;
        message=m;
    }
}
