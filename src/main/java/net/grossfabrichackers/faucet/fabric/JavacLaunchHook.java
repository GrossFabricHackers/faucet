package net.grossfabrichackers.faucet.fabric;

import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.internal.tasks.compile.JdkJavaCompiler;
import org.gradle.api.tasks.WorkResult;

import javax.tools.JavaCompiler;

public class JavacLaunchHook {

    public static WorkResult launch(JavaCompiler compiler, JavaCompileSpec compileSpec) throws ClassNotFoundException {
        if(compiler.getClass().getName().endsWith("DefaultIncrementalAwareCompiler")) {
            JavaCompiler delegate = ReflectionUtil.getField(compiler.getClass(), compiler, "delegate");
            ReflectionUtil.setField(
                    compiler.getClass(),
                    compiler,
                    "delegate",
                    ReflectionUtil.invokeStatic(
                            Thread.currentThread().getContextClassLoader().loadClass(delegate.getClass().getName()),
                            "create",
                            new Class<?>[0],
                            new Object[0]
                    )
            );
        }
        JdkJavaCompiler jdkJavaCompiler = new JdkJavaCompiler(() -> compiler);
        return jdkJavaCompiler.execute(compileSpec);
    }

}
