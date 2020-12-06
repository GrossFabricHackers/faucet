package net.grossfabrichackers.faucet.fabric;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.entrypoint.EntrypointTransformer;
import net.fabricmc.loader.game.GameProvider;
import net.fabricmc.loader.metadata.BuiltinModMetadata;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.internal.Factory;
import org.gradle.internal.jvm.Jvm;

import javax.tools.JavaCompiler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class JavacGameProvider implements GameProvider {

    private static final EntrypointTransformer TRANSFORMER = new EntrypointTransformer(it -> ImmutableList.of());
    private final Factory<JavaCompiler> compilerFactory;
    private final Jvm jvm;
    private final JavaCompileSpec compileSpec;
    private WorkResult workResult;

    public JavacGameProvider(Factory<JavaCompiler> compilerFactory, JavaCompileSpec compileSpec, Jvm jvm) {
        this.compilerFactory = compilerFactory;
        this.compileSpec = compileSpec;
        this.jvm = jvm;
        this.workResult = WorkResults.didWork(false);
    }

    @Override
    public String getGameId() {
        return "javac";
    }

    @Override
    public String getGameName() {
        return "Javac";
    }

    @Override
    public String getRawGameVersion() {
        return jvm.getJavaVersion().toString();
    }

    @Override
    public String getNormalizedGameVersion() {
        return "1." + jvm.getJavaVersion().getMajorVersion() + ".0";
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        try {
            File buildSrcMod = new File(System.getProperty("user.dir") + "/buildSrc/build/libs/buildSrc.jar");
            return ImmutableList.of(
                    new BuiltinMod(
                            buildSrcMod.toURI().toURL(),
                            new BuiltinModMetadata.Builder("buildsrc", "1.0.0")
                                    .setEnvironment(ModEnvironment.UNIVERSAL)
                                    .build()
                    )
            );
        } catch (MalformedURLException e) {
            ReflectionUtil.throwUnchecked(e);
            return null;
        }
    }

    @Override
    public String getEntrypoint() {
        return compilerFactory.getClass().getName();
    }

    @Override
    public Path getLaunchDirectory() {
        File faucetDir = new File(System.getProperty("user.dir") + "/buildSrc/faucet");
        if(!faucetDir.exists()) {
            if(!faucetDir.mkdirs()) throw new IllegalStateException("Could not make faucet directory!");
        }
        return faucetDir.toPath();
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean canOpenErrorGui() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public List<Path> getGameContextJars() {
        return ImmutableList.of();
    }

    @Override
    public boolean locateGame(EnvType envType, ClassLoader loader) {
        return true;
    }

    @Override
    public void acceptArguments(String... arguments) {}

    @Override
    public EntrypointTransformer getEntrypointTransformer() {
        return TRANSFORMER;
    }

    @Override
    public void launch(ClassLoader loader) {
        try {
            Consumer<URL> addURL = (url) -> ReflectionUtil.invoke(loader.getClass(), loader, "addURL", new Class[] {URL.class}, new Object[] {url});
            for(URL url : ((URLClassLoader) JavacGameProvider.class.getClassLoader()).getURLs()) {
                addURL.accept(url);
            }
            if(jvm.getToolsJar() != null) addURL.accept(jvm.getToolsJar().toURI().toURL());
            workResult = ReflectionUtil.invokeStatic(
                    loader.loadClass("net.grossfabrichackers.faucet.fabric.JavacLaunchHook"),
                    "launch",
                    new Class<?>[] {JavaCompiler.class, JavaCompileSpec.class},
                    new Object[] {compilerFactory.create(), compileSpec}
            );
        } catch (MalformedURLException | ClassNotFoundException e) {
            ReflectionUtil.throwUnchecked(e);
        }
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }

    public WorkResult getWorkResult() {
        return workResult;
    }

}
