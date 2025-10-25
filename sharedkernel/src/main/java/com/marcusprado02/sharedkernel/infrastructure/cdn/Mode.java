package com.marcusprado02.sharedkernel.infrastructure.cdn;


public enum Mode { HARD, SOFT } // HARD = invalidação/expurgo imediato; SOFT = serve stale + revalidate

