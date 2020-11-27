package net.grossfabrichackers.faucet.util;

import java.lang.reflect.*;

@SuppressWarnings("unchecked")
public class ReflectionUtil {

    private static final Field MODIFIERS;

    static {
        try {
            MODIFIERS = Field.class.getDeclaredField("modifiers");
            MODIFIERS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getField(Class<?> fieldOwner, Object owner, String name) {
        try {
            Field f = fieldOwner.getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(owner);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            ReflectionUtil.throwUnchecked(e);
            return null;
        }
    }

    public static void setField(Class<?> fieldOwner, Object owner, String name, Object value) {
        try {
            Field f = fieldOwner.getDeclaredField(name);
            f.setAccessible(true);
            int mods = f.getModifiers();
            if(Modifier.isFinal(mods)) {
                MODIFIERS.set(f, mods & ~Modifier.FINAL);
            }
            f.set(owner, value);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            ReflectionUtil.throwUnchecked(e);
        }
    }

    public static <T> T invoke(Class<?> methodOwner, Object owner, String name, Class<?>[] argTypes, Object[] args) {
        try {
            Method m = methodOwner.getDeclaredMethod(name, argTypes);
            m.setAccessible(true);
            return (T) m.invoke(owner, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
            ReflectionUtil.throwUnchecked(e);
            return null;
        }
    }

    public static <T> T invokeStatic(Class<?> methodOwner, String name, Class<?>[] argTypes, Object[] args) {
        return invoke(methodOwner, null, name, argTypes, args);
    }

    @SuppressWarnings("all")
    public static <T> T newInstance(Class<?> type, Class<?>[] argTypes, Object[] args) {
        try {
            Constructor c = type.getDeclaredConstructor(argTypes);
            c.setAccessible(true);
            return (T) c.newInstance(args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException | InstantiationException e) {
            ReflectionUtil.throwUnchecked(e);
            return null;
        }
    }

    @SuppressWarnings("all")
    public static <T extends Throwable> RuntimeException throwUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

}
