package net.mahmutkocas.mixin;

import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class PlayerScreenMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerScreenHandler.class);


    @Inject(at = @At("HEAD"), method = "onSlotClick", cancellable = true)
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        var res = !isLocked(player, ci);
    }
//    @Inject(at = @At("HEAD"), method = "canUse", cancellable = true)
//    public void canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> ci) {
//        var res = !isLocked(ci);
//        ci.setReturnValue(res);
//
//
//        try {
//            throw new RuntimeException("Ex");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Unique
    private boolean isLocked(PlayerEntity player, CallbackInfo ci) {
        if (!PlayerStateHandler.INSTANCE.isLocked(player)) {
            return false;
        }
        ci.cancel();
        return true;
    }

    @Unique
    private static void sendLoginMessage(PlayerEntity player) {
        player.sendMessage(Text.literal("Giriş yapınız!"), true); // Inform the player
    }

}
