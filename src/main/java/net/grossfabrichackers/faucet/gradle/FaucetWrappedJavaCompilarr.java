package net.grossfabrichackers.faucet.gradle;

import net.fabricmc.loader.game.GameProviders;
import net.fabricmc.loader.launch.knot.Knot;
import net.grossfabrichackers.faucet.fabric.JavacGameProvider;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.internal.tasks.compile.JdkJavaCompiler;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.internal.Factory;
import org.gradle.internal.jvm.Jvm;
import org.gradle.language.base.internal.compile.Compiler;

import javax.inject.Inject;
import javax.tools.JavaCompiler;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

public class FaucetWrappedJavaCompilarr implements Compiler<JavaCompileSpec>, Serializable {

    private JdkJavaCompiler delegate;

    @Inject
    public FaucetWrappedJavaCompilarr(Factory<JavaCompiler> compilerFactory) {
        this.delegate = new JdkJavaCompiler(compilerFactory);
    }

    @Override
    public WorkResult execute(JavaCompileSpec t) {
        AtomicReference<WorkResult> result = new AtomicReference<>(WorkResults.didWork(true));
        GameProviders.setProvider(new JavacGameProvider(delegate, Jvm.current(), () -> result.set(delegate.execute(t))));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Knot.main(new String[0]);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return result.get();
    }

    public JdkJavaCompiler getDelegate() {
        return delegate;
    }

}
