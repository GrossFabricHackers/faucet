package net.grossfabrichackers.faucet;

import net.grossfabrichackers.faucet.gradle.FaucetJavaCompilerFactory;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.jvm.toolchain.internal.JavaCompilerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FaucetPluginInternalHook {

    public static void apply(Project project) throws Exception {
        project.getLogger().lifecycle(":Applying Faucet");

        applyPluginDependencies(project);

        // Apply the Faucet java compiler
        ServiceRegistry services = ((ProjectInternal)project.getGradle().getParent().getRootProject()).getServices();
        Object allServices = ReflectionUtil.getField(
                DefaultServiceRegistry.class,
                services,
                "allServices"
        );
        Object factoryMethodService = ReflectionUtil.invoke(
                allServices.getClass(),
                allServices,
                "getService",
                new Class<?>[] { Type.class },
                new Object[] { JavaCompilerFactory.class }
        );
        Object faucetFJTFHook = ReflectionUtil.newInstance(
                Class.forName("org.gradle.internal.service.ReflectionBasedServiceMethod"),
                new Class<?>[] { Method.class },
                new Object[] { ReflectionUtil.getNonoverloadedMethod(FaucetJavaCompilerFactory.class, "createFaucetCompilerFactory") }
        );
        ReflectionUtil.setField(
                factoryMethodService.getClass(),
                factoryMethodService,
                "method",
                faucetFJTFHook
        );
    }

    private static void applyPluginDependencies(Project project) throws Exception {
        Properties transientDeps = new Properties();
        transientDeps.load(FaucetPluginInternalHook.class.getClassLoader().getResourceAsStream("META-INF/faucetTransientDependencies.properties"));

        // we assume if no repositories have been added
        // that we need to add one ourselves
        if(project.getRepositories().size() == 0) {
            project.getRepositories().mavenCentral();
        }

        // add fabric maven
        final URI fabricMaven = new URI(transientDeps.getProperty("fabric_maven"));
        if(project.getRepositories().stream().noneMatch(
                a -> a instanceof MavenArtifactRepository && ((MavenArtifactRepository) a).getUrl().equals(fabricMaven)
        )) {
            project.getRepositories().maven(a -> a.setUrl(fabricMaven));
        }

        // add ourselves as a dependency
        project.getDependencies().add("implementation", project.files((Object[]) ((URLClassLoader) FaucetPluginInternalHook.class.getClassLoader()).getURLs()));

        // add tools.jar for Java 8
        Jvm current = Jvm.current();
        if(current.getJavaVersion().ordinal() <= JavaVersion.VERSION_1_8.ordinal()) {
            if (current.getToolsJar() != null && current.getToolsJar().exists()) {
                project.getDependencies().add("implementation", project.files(current.getToolsJar()));
            } else {
                project.getLogger().warn("Could not find tools.jar, Faucet mixins may not compile.");
            }
        }

        // Setup mixin dependencies
        final String asmVersion = transientDeps.getProperty("asm_version");
        SourceSetContainer sourceSets = ((JavaPluginConvention) project.getConvention().getPlugins().get("java")).getSourceSets();
        Consumer<String> annotationProcessor = d -> sourceSets.stream()
                .map(SourceSet::getAnnotationProcessorConfigurationName)
                .forEach(c -> project.getDependencies().add(c, d));
        Stream.of(
                "org.ow2.asm:asm:",
                "org.ow2.asm:asm-analysis:",
                "org.ow2.asm:asm-commons:",
                "org.ow2.asm:asm-tree:",
                "org.ow2.asm:asm-util:"
        ).map(d -> d.concat(asmVersion)).forEach(annotationProcessor);
        annotationProcessor.accept("com.google.guava:guava:" + transientDeps.getProperty("mixin_guava_version"));
        annotationProcessor.accept("org.apache.logging.log4j:log4j-core:" + transientDeps.getProperty("mixin_log4j_version"));
        annotationProcessor.accept("net.fabricmc:sponge-mixin:" + transientDeps.getProperty("mixin_version"));
        annotationProcessor.accept("net.fabricmc:fabric-mixin-compile-extensions:" + transientDeps.getProperty("mixin_fabric_extensions_version"));
    }

}
