package com.marcusprado02.sharedkernel.crosscutting.l10n;

import java.text.*;
import java.util.*;

public final class JdkMessageFormatter implements MessageFormatter {
    @Override public String format(String pattern, LocalizationContext ctx, Object... args) {
        MessageFormat mf = new MessageFormat(pattern, ctx.locale());
        // Ajusta *formats* padrão sensíveis a timezone/locale
        for (Format f : mf.getFormatsByArgumentIndex()) {
            if (f instanceof DateFormat df) df.setTimeZone(TimeZone.getTimeZone(ctx.zoneId()));
            if (f instanceof NumberFormat nf && ctx.attributes().get("currency") instanceof Currency cur) {
                if (nf instanceof DecimalFormat dfmt) dfmt.setCurrency(cur);
            }
        }
        return mf.format(args == null ? new Object[]{} : args);
    }
}