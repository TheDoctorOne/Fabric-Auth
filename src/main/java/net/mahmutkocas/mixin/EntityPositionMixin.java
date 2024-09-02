package net.mahmutkocas.mixin;

import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPositionS2CPacket.class)
public class EntityPositionMixin {

    @Mutable
    @Shadow @Final private double x;
    @Mutable
    @Shadow @Final private double y;
    @Mutable
    @Shadow @Final private double z;
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerScreenHandler.class);

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/Entity;)V")
    public void init(Entity entity, CallbackInfo ci) {
        if(entity instanceof PlayerEntity && isLocked((PlayerEntity) entity, ci)) {
            x = 0;
            y = 255;
            z = 0;
        }
    }


    @Unique
    private boolean isLocked(PlayerEntity player, CallbackInfo ci) {
        return PlayerStateHandler.INSTANCE.isLocked(player);
    }

    @Unique
    private static void sendLoginMessage(PlayerEntity player) {
        player.sendMessage(Text.literal("Giriş yapınız!"), true); // Inform the player
    }

}
