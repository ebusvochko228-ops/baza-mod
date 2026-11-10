package com.baza.auto.mixin;

import com.baza.auto.BazaModClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatSendMixin {
    @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (message.startsWith(".baza")) {
            BazaModClient.handleCommand(message);
            ci.cancel(); // команда не уйдёт на сервер
        }
    }
}
