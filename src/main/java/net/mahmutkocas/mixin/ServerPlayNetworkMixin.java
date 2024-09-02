package net.mahmutkocas.mixin;

import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkMixin {


    @Shadow
    public ServerPlayerEntity player;

    @Shadow @Final
    static Logger LOGGER;

    @Shadow private int requestedTeleportId;

    @Inject(at = @At("HEAD"), method = "requestTeleport(DDDFFLjava/util/Set;)V", cancellable = true)
    public void onChatMessage(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags, CallbackInfo ci) {
        checkLock(ci);
    }
    @Inject(at = @At("HEAD"), method = "onChatMessage", cancellable = true)
    public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        checkLock(ci);
    }

    @Inject(at = @At("HEAD"), method = "onPlayerAction", cancellable = true)
    public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        checkLock(ci);
    }

    @Inject(at = @At("HEAD"), method = "onButtonClick", cancellable = true)
    public void onButtonClick(ButtonClickC2SPacket packet, CallbackInfo ci) {
        checkLock(ci);
    }
    @Inject(at = @At("HEAD"), method = "onPlayerInput", cancellable = true)
    public void onPlayerInput(PlayerInputC2SPacket packet, CallbackInfo ci) {
        checkLock(ci);
    }


    @Unique
    private void checkLock(CallbackInfo ci) {
        if (!PlayerStateHandler.INSTANCE.isLocked(player)) {
            return;
        }
        sendLoginMessage(player);
        ci.cancel();
    }

    @Unique
    private static void sendLoginMessage(PlayerEntity player) {
        player.sendMessage(Text.literal("Giriş yapınız!"), true); // Inform the player
    }
}
