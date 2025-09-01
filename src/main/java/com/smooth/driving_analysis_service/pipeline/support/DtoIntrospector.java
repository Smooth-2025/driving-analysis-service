// src/main/java/com/smooth/driving_analysis_service/pipeline/support/DtoIntrospector.java
package com.smooth.driving_analysis_service.pipeline.support;

import java.lang.reflect.Method;
import java.time.*;

public final class DtoIntrospector {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private DtoIntrospector() {}

    public static String str(Object dto, String... getters) {
        Object v = invoke(dto, getters);
        return v == null ? null : String.valueOf(v);
    }
    public static Integer integer(Object dto, String... getters) {
        Object v = invoke(dto, getters);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    public static Long longNum(Object dto, String... getters) {
        Object v = invoke(dto, getters);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    public static LocalDateTime dateTime(Object dto, String... getters) {
        Object v = invoke(dto, getters);
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Instant i)         return LocalDateTime.ofInstant(i, KST);
        if (v instanceof ZonedDateTime z)   return z.withZoneSameInstant(KST).toLocalDateTime();
        if (v instanceof OffsetDateTime o)  return o.atZoneSameInstant(KST).toLocalDateTime();
        if (v instanceof Number n)          return LocalDateTime.ofInstant(Instant.ofEpochMilli(n.longValue()), KST);
        return null;
    }
    private static Object invoke(Object dto, String... names) {
        if (dto == null) return null;
        Class<?> c = dto.getClass();
        for (String name : names) {
            try { Method m = c.getMethod(name); return m.invoke(dto); }
            catch (NoSuchMethodException ignored) {}
            catch (Exception e) {}
        }
        return null;
    }
}
