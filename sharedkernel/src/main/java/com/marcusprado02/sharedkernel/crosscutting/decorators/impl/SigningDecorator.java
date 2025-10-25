package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Signer;

public class SigningDecorator<I,O> extends PortDecorator<I,O> {
    private final java.util.function.BiConsumer<I,Signer> signFn;
    private final Signer signer; // sua interface: calcula HMAC/assinatura e injeta no input

    public SigningDecorator(Port<I,O> delegate, Signer signer,
                            java.util.function.BiConsumer<I,Signer> signFn) {
        super(delegate); this.signer = signer; this.signFn = signFn;
    }

    @Override
    public O execute(I input) throws Exception {
        signFn.accept(input, signer); // ex.: injeta header "Authorization" HMAC
        return delegate.execute(input);
    }
}

