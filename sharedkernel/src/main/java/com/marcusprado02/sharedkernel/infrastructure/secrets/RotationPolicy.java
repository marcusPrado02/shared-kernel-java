package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.time.Duration;
import java.util.*;
public record RotationPolicy(
    Duration interval, 
    String rotationLambdaOrWebhook,
    Duration minInterval,      // intervalo mínimo entre rotações
    Duration maxAge,           // validade máxima de uma versão
    boolean  keepPreviousStage, // manter versão anterior marcada como "previous"
    Map<String,String> params
) {}

