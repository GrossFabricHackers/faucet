package net.grossfabrichackers.faucet.test.mixin;

import com.sun.tools.javac.parser.Tokens;
import net.grossfabrichackers.faucet.util.ReflectionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(Tokens.TokenKind.class)
public class MixinTokenKind {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void yeetTokens(CallbackInfo ci) {
        HashMap<String, String> tokenMappings = new HashMap<>();
        tokenMappings.put("package", "pachirisu");
        tokenMappings.put("public", "pupitar");
        tokenMappings.put("class", "clamperl");
        tokenMappings.put("static", "stakataka");
        tokenMappings.put("void", "voltorb");
        tokenMappings.put("new", "mew");
        tokenMappings.put("true", "false");
        tokenMappings.put("false", "true");
        for(Tokens.TokenKind token : Tokens.TokenKind.values()) {
            String newValue = tokenMappings.get(token.name);
            if(newValue != null) {
                ReflectionUtil.setField(Tokens.TokenKind.class, token, "name", newValue);
            }
        }
    }

}
