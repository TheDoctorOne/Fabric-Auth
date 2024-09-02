package net.mahmutkocas.mixin;

import com.mojang.authlib.GameProfile;
import net.mahmutkocas.PlayerStateHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
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

	@Shadow
	public ServerPlayNetworkHandler networkHandler;

	@Shadow
	private int screenHandlerSyncId;

	@Mutable
	@Shadow @Final
	private ScreenHandlerSyncHandler screenHandlerSyncHandler;

	@Inject(at = @At("RETURN"), method = "<init>")
	void init(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
		screenHandlerSyncHandler = new ScreenHandlerSyncHandler() {
			public void updateState(ScreenHandler handler, DefaultedList<ItemStack> stacks, ItemStack cursorStack, int[] properties) {
				var callback = new CallbackInfo("", true);
				checkLock(callback);
				if(callback.isCancelled()) {
					return;
				}

				networkHandler.sendPacket(new InventoryS2CPacket(handler.syncId, handler.nextRevision(), stacks, cursorStack));

				for(int i = 0; i < properties.length; ++i) {
					this.sendPropertyUpdate(handler, i, properties[i]);
				}

			}

			public void updateSlot(ScreenHandler handler, int slot, ItemStack stack) {
				var callback = new CallbackInfo("", true);
				checkLock(callback);
				if(callback.isCancelled()) {
					return;
				}
				networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), slot, stack));
			}

			public void updateCursorStack(ScreenHandler handler, ItemStack stack) {
				var callback = new CallbackInfo("", true);
				checkLock(callback);
				if(callback.isCancelled()) {
					return;
				}
				networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, handler.nextRevision(), -1, stack));
			}

			public void updateProperty(ScreenHandler handler, int property, int value) {
				this.sendPropertyUpdate(handler, property, value);
			}

			private void sendPropertyUpdate(ScreenHandler handler, int property, int value) {
				var callback = new CallbackInfo("", true);
				checkLock(callback);
				if(callback.isCancelled()) {
					return;
				}

				networkHandler.sendPacket(new ScreenHandlerPropertyUpdateS2CPacket(handler.syncId, property, value));
			}
		};
	}


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