package net.mahmutkocas.mixin;

import com.mojang.authlib.GameProfile;
import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Shadow
	public abstract void closeHandledScreen();

	@Shadow public ServerPlayNetworkHandler networkHandler;

	@Shadow private int screenHandlerSyncId;

	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	protected void tick(CallbackInfo ci) {
		checkLock(ci);

		if(ci.isCancelled()) {

		}
	}

	@Inject(at = @At("HEAD"), method = "dropItem", cancellable = true)
	private void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		checkLock(cir);
		if(cir.isCancelled()) {
			cir.setReturnValue(null);
		}
	}

	@Unique
	private void checkLock(CallbackInfo ci) {
		if (!PlayerStateHandler.INSTANCE.isLocked(getUuidAsString())) {
			return;
		}
		ci.cancel();
	}
}