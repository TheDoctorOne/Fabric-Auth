package net.mahmutkocas.mixin;

import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(PlayerScreenHandler.class)
@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Final
    @Shadow
    public PlayerEntity player;
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerScreenHandler.class);

    @Inject(at = @At("HEAD"), method = "addPickBlock", cancellable = true)
    public void addPickBlock(ItemStack stack, CallbackInfo ci) {
        var res = !isLocked(ci);
    }


    @Unique
    private boolean isLocked(CallbackInfo ci) {
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
