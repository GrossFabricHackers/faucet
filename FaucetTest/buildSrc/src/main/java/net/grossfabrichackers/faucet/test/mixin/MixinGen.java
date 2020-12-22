package net.grossfabrichackers.faucet.test.mixin;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.Code;
import com.sun.tools.javac.jvm.Gen;
import com.sun.tools.javac.jvm.Items;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gen.class)
public class MixinGen {

    @Shadow @Final private Symtab syms;
    @Shadow @Final private Names names;
    @Shadow private Items items;
    @Shadow private Code code;

    @Inject(method = "visitBinary", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectCustomOperatorLogic(JCTree.JCBinary binary, CallbackInfo ci) {
        Type pluralTater = ReflectionUtil.invoke(syms.getClass(), syms, "enterClass", new Class<?>[]{String.class}, new Object[]{"net.grossfabrichackers.faucet.test.PluralTater"});
        Symbol.OperatorSymbol operator = (Symbol.OperatorSymbol)binary.operator;
        if(operator.opcode == 420) {
            code.emitop2(187, makeRef(binary, pluralTater)); // NEW
            code.emitop0(89); // DUP
            callMethod(binary.pos(), pluralTater, names.init, List.nil(), false);
            ReflectionUtil.setField(this.getClass(), this, "result",
                    ReflectionUtil.invoke(items.getClass(), items, "makeStackItem", new Class<?>[]{Type.class}, new Object[]{pluralTater})
            );
            ci.cancel();
        }
    }

    @Shadow
    void callMethod(JCDiagnostic.DiagnosticPosition var1, Type var2, Name var3, List<Type> var4, boolean var5) {}

    @Shadow
    int makeRef(JCDiagnostic.DiagnosticPosition var1, Type var2) {
        return 0;
    }

}
