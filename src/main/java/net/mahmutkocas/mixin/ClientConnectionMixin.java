package net.mahmutkocas.mixin;

import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "handlePacket", cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (!(listener instanceof ServerPlayNetworkHandler)) {
            return;
        }
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) listener;

        if (packet instanceof CustomPayloadC2SPacket pack) {
            if (!"minecraft".equals(pack.getChannel().getNamespace())) {
                checkLock(handler.getPlayer().getUuidAsString(), ci);
                if (ci.isCancelled()) {
                    LOGGER.info("Denied packet: " + pack.getChannel() + " for Player: " + handler.getPlayer().getUuidAsString());
                }
            } else {
                LOGGER.debug("Packet: " + pack.getChannel() + " for Player: " + handler.getPlayer().getUuidAsString());
            }
        }
    }


    @Unique
    private static void checkLock(String uuid, CallbackInfo ci) {
        if (!PlayerStateHandler.INSTANCE.isLocked(uuid)) {
            return;
        }
        ci.cancel();
    }
}
