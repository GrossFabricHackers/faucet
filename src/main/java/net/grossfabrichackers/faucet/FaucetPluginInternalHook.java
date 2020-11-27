package net.grossfabrichackers.faucet;

import net.grossfabrichackers.faucet.gradle.FaucetJavaToolchainFactory;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.JavaToolChainFactory;
import org.gradle.internal.jvm.inspection.JvmVersionDetector;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.jvm.toolchain.internal.JavaCompilerFactory;
import org.gradle.process.internal.ExecActionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class FaucetPluginInternalHook {

    public static void apply(Project project) throws Exception {
        project.getLogger().lifecycle(":Applying Faucet");

        ServiceRegistry services = ((ProjectInternal)project).getServices();
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
                new Object[] { JavaToolChainFactory.class }
        );
        Object faucetFJTFHook = ReflectionUtil.newInstance(
                Class.forName("org.gradle.internal.service.ReflectionBasedServiceMethod"),
                new Class<?>[] { Method.class },
                new Object[] { FaucetJavaToolchainFactory.class.getMethod("newFJTFHook", JavaCompilerFactory.class, ExecActionFactory.class, JvmVersionDetector.class) }
        );
        ReflectionUtil.setField(
                factoryMethodService.getClass(),
                factoryMethodService,
                "method",
                faucetFJTFHook
        );

    }

}
