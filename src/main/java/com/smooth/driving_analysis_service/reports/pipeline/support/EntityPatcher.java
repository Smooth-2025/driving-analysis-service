package com.smooth.driving_analysis_service.reports.pipeline.support;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public final class EntityPatcher {
    private EntityPatcher(){}

    public static void set(Object target, Object value, String... setterCandidates) {
        if (target == null || value == null) return;
        Class<?> c = target.getClass();
        for (String name : setterCandidates) {
            for (Method m : c.getMethods()) {
                if (!m.getName().equals(name)) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 1) continue;
                if (isAssignable(p[0], value.getClass())) {
                    try { m.invoke(target, coerce(value, p[0])); return; } catch (Exception ignored) {}
                }
            }
        }
    }

    private static boolean isAssignable(Class<?> to, Class<?> from) {
        if (to.isAssignableFrom(from)) return true;
        // primitive ↔ wrapper 간단 매핑
        return (to == int.class && from == Integer.class)
                || (to == long.class && from == Long.class)
                || (to == double.class && from == Double.class)
                || (to == boolean.class && from == Boolean.class);
    }

    private static Object coerce(Object v, Class<?> to) {
        if (v == null) return null;
        if (to.isInstance(v)) return v;
        try {
            if (to == Double.class || to == double.class) return Double.valueOf(v.toString());
            if (to == Integer.class || to == int.class)    return Integer.valueOf(v.toString());
            if (to == Long.class || to == long.class)      return Long.valueOf(v.toString());
            if (to == Boolean.class || to == boolean.class)return Boolean.valueOf(v.toString());
        } catch (Exception ignored) {}
        return v;
    }

    public static void touchCreatedAt(Object target) {
        set(target, LocalDateTime.now(), "setCreatedAt");
        set(target, LocalDateTime.now(), "setCreateAt", "setCreatedTime");
    }
    public static void touchUpdatedAt(Object target) {
        set(target, LocalDateTime.now(), "setUpdatedAt");
        set(target, LocalDateTime.now(), "setUpdateAt", "setModifiedAt");
    }
}
