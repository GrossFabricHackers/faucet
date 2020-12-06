package net.grossfabrichackers.faucet.gradle;

import net.fabricmc.loader.game.GameProviders;
import net.fabricmc.loader.launch.knot.Knot;
import net.grossfabrichackers.faucet.fabric.JavacGameProvider;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.Factory;
import org.gradle.internal.jvm.Jvm;
import org.gradle.language.base.internal.compile.Compiler;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import java.io.Serializable;

public class FaucetJavaCompiler implements Compiler<JavaCompileSpec>, Serializable {

    private final Factory<JavaCompiler> compilerFactory;

    @Inject
    public FaucetJavaCompiler(Factory<JavaCompiler> compilerFactory) {
        this.compilerFactory = compilerFactory;
    }

    @Override
    public WorkResult execute(JavaCompileSpec t) {
        JavacGameProvider gameProvider = new JavacGameProvider(compilerFactory, t, Jvm.current());
        GameProviders.setProvider(gameProvider);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Knot.main(new String[0]);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return gameProvider.getWorkResult();
    }

}
