package net.grossfabrichackers.faucet.fabric;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.entrypoint.EntrypointTransformer;
import net.fabricmc.loader.game.GameProvider;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.internal.jvm.Jvm;
import org.gradle.language.base.internal.compile.Compiler;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JavacGameProvider implements GameProvider {

    private static final EntrypointTransformer TRANSFORMER = new EntrypointTransformer(it -> ImmutableList.of());
    private final Compiler<JavaCompileSpec> compiler;
    private final Jvm jvm;
    private final Runnable launchHook;

    public JavacGameProvider(Compiler<JavaCompileSpec> compiler, Jvm jvm, Runnable launchHook) {
        this.compiler = compiler;
        this.jvm = jvm;
        this.launchHook = launchHook;
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
        return Collections.emptyList();
    }

    @Override
    public String getEntrypoint() {
        return compiler.getClass().getName();
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
        launchHook.run();
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }

}
