package net.grossfabrichackers.faucet.test.mixin;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Symtab.class)
public class MixinSymtab {

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void injectCustomOperators(CallbackInfo ci) {
        Type tater = enterClass("net.grossfabrichackers.faucet.test.Tater");
        Type pluralTater = enterClass("net.grossfabrichackers.faucet.test.PluralTater");
        enterBinop("+", tater, tater, pluralTater, 254);
    }

    @Shadow
    private Type enterClass(String clazz) {
        return null;
    }

    @Shadow
    private void enterBinop(String var1, Type var2, Type var3, Type var4, int var5) {}

}
