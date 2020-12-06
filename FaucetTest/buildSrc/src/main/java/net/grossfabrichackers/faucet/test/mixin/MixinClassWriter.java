package net.grossfabrichackers.faucet.test.mixin;

import com.sun.tools.javac.jvm.ClassWriter;
import com.sun.tools.javac.util.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClassWriter.class)
public class MixinClassWriter {

    @Inject(method = "instance", at = @At("HEAD"), remap = false)
    private static void hmm(Context context, CallbackInfo ci) {
        throw new RuntimeException("no u");
    }

}
