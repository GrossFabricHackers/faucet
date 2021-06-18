package net.grossfabrichackers.faucet.test.mixin;

import com.sun.tools.javac.jvm.Code;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Code.class)
public class MixinCode {

    @Shadow
    private boolean alive;

    @Inject(method = "emitop0(I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectTaterOpcode(int opcode, CallbackInfo ci) {
        if(opcode == 254) {
            this.emitop(opcode);
            if (this.alive) {
                Object state = ReflectionUtil.getField(Code.class, this, "state");
                ReflectionUtil.invoke(state.getClass(), state, "pop", new Class<?>[] {int.class}, new Object[] {1});
                this.postop();
            }
            ci.cancel();
        }
    }

    @Shadow
    private void emitop(int var1) {}

    @Shadow
    void postop() {}

}
