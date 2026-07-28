package org.fuzi.fuziutils.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerForcedPoseMixin {

    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void fuziutils$forceStandingPose(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (self.getForcedPose() != null) {
            cir.setReturnValue(false);
        }
    }
}
