package com.marcusprado02.sharedkernel.crosscutting.aop.annotations;


import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Encrypted {
    /** Decriptar argumentos que sejam CipherText antes da execução. */
    boolean decryptArgs() default true;
    /** Encriptar o retorno (quando não nulo) após a execução. */
    boolean encryptReturn() default false;
    /** Perfil/chave lógica a usar no serviço de criptografia (KMS, Vault etc.). */
    String profile() default "default";
}