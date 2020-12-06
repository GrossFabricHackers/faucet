package net.grossfabrichackers.faucet;

import net.grossfabrichackers.faucet.util.GrossGradleHacksClassLoader;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

public class FaucetPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if(!project.getName().equals("buildSrc")) {
            final String errorMessage = "Faucet MUST BE applied to the buildSrc project.";
            project.getLogger().error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        project.getPlugins().apply(JavaBasePlugin.class);

        AtomicReference<Throwable> innerThreadException = new AtomicReference<>();
        Thread innerThread = new Thread(() -> {
            try {
                Thread.currentThread().setName("Faucet");
                ClassLoader internalClassloader = JavaBasePlugin.class.getClassLoader();
                URLClassLoader ourClassLoader = (URLClassLoader) FaucetPlugin.class.getClassLoader();
                URL[] urls = ourClassLoader.getURLs();
                ClassLoader faucetInternalClassLoader = new GrossGradleHacksClassLoader(urls, internalClassloader);
                Thread.currentThread().setContextClassLoader(faucetInternalClassLoader);
                Class<?> hookClass = faucetInternalClassLoader.loadClass("net.grossfabrichackers.faucet.FaucetPluginInternalHook");
                ReflectionUtil.invokeStatic(
                        hookClass,
                        "apply",
                        new Class<?>[]{Project.class},
                        new Object[]{project}
                );
            } catch (Throwable t) {
                innerThreadException.set(t);
            }
        });
        innerThread.start();
        try {
            innerThread.join();
        } catch (InterruptedException ignored) {}
        Throwable innerExceptionValue = innerThreadException.get();
        if(innerExceptionValue != null) {
            ReflectionUtil.throwUnchecked(innerExceptionValue);
        }
    }

}