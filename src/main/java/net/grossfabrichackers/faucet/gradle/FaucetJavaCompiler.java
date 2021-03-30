package net.grossfabrichackers.faucet.gradle;

import net.fabricmc.loader.game.GameProvider;
import net.fabricmc.loader.game.GameProviders;
import net.fabricmc.loader.launch.knot.FaucetKnotHelper;
import net.fabricmc.loader.launch.knot.Knot;
import net.grossfabrichackers.faucet.fabric.JavacGameProvider;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.Factory;
import org.gradle.internal.jvm.Jvm;
import org.gradle.language.base.internal.compile.Compiler;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import java.io.Serializable;

public class FaucetJavaCompiler implements Compiler<JavaCompileSpec>, Serializable {

    private static final Object GLOBAL_KNOT_LOCK = new Object();
    private static ClassLoader GLOBAL_KNOT_CLASS_LOADER = null;
    private final Factory<JavaCompiler> compilerFactory;

    @Inject
    public FaucetJavaCompiler(Factory<JavaCompiler> compilerFactory) {
        this.compilerFactory = compilerFactory;
    }

    @Override
    public WorkResult execute(JavaCompileSpec t) {
        JavacGameProvider gameProvider = new JavacGameProvider(compilerFactory, t, Jvm.current());
        // because we override GameProvider, Javac sometimes fails on this call
        // but the right class is guaranteed to be loaded at runtime
        ReflectionUtil.invoke(
                GameProviders.class,
                null,
                "setProvider",
                new Class<?>[] {GameProvider.class }, new Object[] { gameProvider }
        );
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            synchronized (GLOBAL_KNOT_LOCK) {
                if (GLOBAL_KNOT_CLASS_LOADER == null) {
                    // init Knot the first time
                    Knot knot = FaucetKnotHelper.createKnot();
                    try {
                        FaucetKnotHelper.initKnot(knot);
                    } finally {
                        GLOBAL_KNOT_CLASS_LOADER = ReflectionUtil.getField(Knot.class, knot, "classLoader");
                    }
                } else {
                    Thread.currentThread().setContextClassLoader(GLOBAL_KNOT_CLASS_LOADER);
                    gameProvider.launch(GLOBAL_KNOT_CLASS_LOADER);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return gameProvider.getWorkResult();
    }

}
