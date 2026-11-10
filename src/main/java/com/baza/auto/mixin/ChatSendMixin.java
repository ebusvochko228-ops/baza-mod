package com.baza.auto.mixin;

import com.baza.auto.BazaModClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ChatSendMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void baza$onSendChatMessage(String message, CallbackInfo ci) {
        if (message.startsWith(".")) {
            BazaModClient.handleCommand(message);
            ci.cancel();
        }
    }
}
