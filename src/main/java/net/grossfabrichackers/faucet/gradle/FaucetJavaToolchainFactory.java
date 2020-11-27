package net.grossfabrichackers.faucet.gradle;

import org.gradle.api.internal.tasks.CurrentJvmJavaToolChain;
import org.gradle.api.internal.tasks.JavaHomeBasedJavaToolChain;
import org.gradle.api.internal.tasks.JavaToolChainFactory;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.internal.jvm.inspection.JvmVersionDetector;
import org.gradle.jvm.toolchain.JavaToolChain;
import org.gradle.jvm.toolchain.internal.JavaCompilerFactory;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;

public class FaucetJavaToolchainFactory extends JavaToolChainFactory {

    private final JavaCompilerFactory javaCompilerFactory;
    private final ExecActionFactory execActionFactory;
    private final JvmVersionDetector jvmVersionDetector;

    public FaucetJavaToolchainFactory(JavaCompilerFactory javaCompilerFactory, ExecActionFactory execActionFactory, JvmVersionDetector jvmVersionDetector) {
        super(javaCompilerFactory, execActionFactory, jvmVersionDetector);
        this.javaCompilerFactory = javaCompilerFactory;
        this.execActionFactory = execActionFactory;
        this.jvmVersionDetector = jvmVersionDetector;
    }

    public static JavaToolChainFactory newFJTFHook(JavaCompilerFactory javaCompilerFactory, ExecActionFactory execActionFactory, JvmVersionDetector jvmVersionDetector) {
        return new FaucetJavaToolchainFactory(javaCompilerFactory, execActionFactory, jvmVersionDetector);
    }

    @Override
    @SuppressWarnings("all")
    public JavaToolChain forCompileOptions(CompileOptions compileOptions) {
        if (compileOptions.isFork()) {
            ForkOptions forkOptions = compileOptions.getForkOptions();
            File javaHome = forkOptions.getJavaHome();
            if (javaHome != null) {
                return new JavaHomeBasedJavaToolChain(javaHome, this.javaCompilerFactory, this.execActionFactory, this.jvmVersionDetector);
            }
        }

        return new CurrentJvmJavaToolChain(this.javaCompilerFactory, this.execActionFactory);
    }

}
