package net.grossfabrichackers.faucet;

import net.grossfabrichackers.faucet.gradle.FaucetJavaCompilerFactory;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.jvm.toolchain.internal.JavaCompilerFactory;

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

}
