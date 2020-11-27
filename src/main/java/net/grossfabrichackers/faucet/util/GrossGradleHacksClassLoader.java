package net.grossfabrichackers.faucet.util;

import net.fabricmc.tinyremapper.TinyRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <blockquote><i>Why is Gradle 6.7 using 6.5 jars!??</i></blockquote>
 * An extremely questionable and concerning classloader
 * to deal with internal Gradle refactors.
 */
public class GrossGradleHacksClassLoader extends URLClassLoader {

    private static final Field CLASSES_FIELD;

    static {
        registerAsParallelCapable();

        Field classesFieldTemp = null;
        try {
            classesFieldTemp = ClassLoader.class.getDeclaredField("classes");
            classesFieldTemp.setAccessible(true);
        } catch (NoSuchFieldException e) {
            ReflectionUtil.throwUnchecked(e);
        }
        CLASSES_FIELD = classesFieldTemp;
    }

    private final HashMap<String, Class<?>> internalClassCache = new HashMap<>();
    private final Function<ClassWriter, ClassRemapper> classRemapperFactory;

    public GrossGradleHacksClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);

        // GrossTinyHacks ACTIVATE
        TinyRemapper remapper = TinyRemapper.newRemapper().resolveMissing(false).threads(1).build();
        ReflectionUtil.setField(TinyRemapper.class, remapper, "classMap", new LazyHashMap<String, String>(name -> {
            // Don't remap if the class is where it should be.
            try {
                Objects.requireNonNull(getParent().loadClass(name.replace('/', '.')));
                return name;
            } catch (ClassNotFoundException | NullPointerException e) {
                Class<?> mappedClass = findInternalClass(name);
                return mappedClass != null ? mappedClass.getName().replace('.', '/') : name;
            }
        }));

        classRemapperFactory = (writer) -> {
            try {
                return ReflectionUtil.newInstance(
                        Class.forName("net.fabricmc.tinyremapper.AsmClassRemapper"),
                        new Class<?>[]{ClassVisitor.class, Class.forName("net.fabricmc.tinyremapper.AsmRemapper"), boolean.class, boolean.class, boolean.class},
                        new Object[]{writer, remapper.getRemapper(), false, false, false}
                );
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Class<?> findInternalClass(String name) {
        if(!name.startsWith("org/gradle/") || !name.contains("internal") || name.contains("impldep")) return null;
        final String simpleName = name.substring(name.lastIndexOf('/') + 1);
        if(internalClassCache.containsKey(simpleName)) {
            return internalClassCache.get(simpleName);
        }
        try {
            // Try seeing if any class loaded by the parent
            // has the same simple name.
            @SuppressWarnings("unchecked") final List<Class<?>> classesMatchingThisName = ((Vector<Class<?>>) CLASSES_FIELD.get(getParent())).stream()
                    .filter(c -> c.getName().substring(c.getName().lastIndexOf('.') + 1).equals(simpleName))
                    .filter(c -> c.getName().startsWith("org.gradle"))
                    .collect(Collectors.toList());

            if (classesMatchingThisName.size() == 1) {
                internalClassCache.put(simpleName, classesMatchingThisName.get(0));
                return classesMatchingThisName.get(0);
            } else {
                internalClassCache.put(simpleName, null);
                return null;
            }
        } catch (Exception e2) {
            internalClassCache.put(simpleName, null);
            return null;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(name.startsWith("net.grossfabrichackers.faucet")) {
            String path = name.replace('.', '/').concat(".class");
            InputStream stream = super.getResourceAsStream(path);
            if (stream != null) {
                try {
                    byte[] clazzSource = IOUtil.readStream(stream);
                    ClassReader reader = new ClassReader(clazzSource);
                    ClassWriter writer = new ClassWriter(reader, 0);
                    ClassRemapper remapper = classRemapperFactory.apply(writer);
                    reader.accept(remapper, 0);
                    clazzSource = writer.toByteArray();
                    try {
                        stream.close();
                    } catch (IOException ignored) {}
                    return defineClass(name, clazzSource, 0, clazzSource.length);
                } catch (IOException ignored) {
                    throw new ClassNotFoundException(name);
                }
            }
        }
        return super.findClass(name);
    }

}
