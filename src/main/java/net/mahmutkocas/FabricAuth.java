package net.mahmutkocas;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.netty.util.internal.StringUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricAuth implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("fabricauth");

    public IAuthHandler authHandler = new JsonAuthHandler();

    private void lockPlayerInputs(ServerPlayerEntity player, Vec3d playerLockedLocation) {
        // Prevent player movement by setting the player's velocity to zero
        player.setPosition(playerLockedLocation);
        player.getAbilities().invulnerable = true;
        player.sendAbilitiesUpdate();
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            sendLoginMessage(player);
            PlayerStateHandler.INSTANCE.add(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            PlayerStateHandler.INSTANCE.remove(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if(PlayerStateHandler.INSTANCE.isLocked(oldPlayer)) {
                PlayerStateHandler.INSTANCE.add(newPlayer);
            }
        });

        registerPlayerLock();


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("register")
                        .then(argument("password", StringArgumentType.word())
                                .then(argument("repeatPassword", StringArgumentType.word())
                                        .executes(context -> {
                                            if(Objects.isNull(context.getSource().getPlayer())) {
                                                context.getSource().sendFeedback(() -> Text.literal("Bu komutu sadece oyuncular kullanabilir."), false);
                                                return 1;
                                            }
                                            String pass = StringArgumentType.getString(context, "password");
                                            String pass2 = StringArgumentType.getString(context, "repeatPassword");
                                            if (!Objects.equals(pass, pass2) || StringUtil.isNullOrEmpty(pass)) {
                                                context.getSource().sendFeedback(() -> Text.literal("Şifreler uyuşmuyor."), false);
                                                return 1;
                                            }

                                            String user = context.getSource().getPlayer().getUuidAsString();

                                            if (authHandler.userExists(user)) {
                                                context.getSource().sendFeedback(() -> Text.literal("Zaten kayıt olmuşsunuz."), false);
                                                return 1;
                                            }

                                            boolean isSuccess = authHandler.registerUpdate(user, pass, false);
                                            if (!isSuccess) {
                                                context.getSource().sendFeedback(() -> Text.literal("Kayıt sırasında hata oluştu."), false);
                                                return 1;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal("Kayıt başarılı! Giriş yapınız."), false);
                                            return 1;
                                        }))
                        )));


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("login")
                        .then(argument("password", StringArgumentType.word())
                        .executes(context -> {
                            if(Objects.isNull(context.getSource().getPlayer())) {
                                context.getSource().sendFeedback(() -> Text.literal("Bu komutu sadece oyuncular kullanabilir."), false);
                                return 1;
                            }

                            String user = context.getSource().getPlayer().getUuidAsString();
                            String pass = StringArgumentType.getString(context, "password");

                            if (!authHandler.userExists(user)) {
                                context.getSource().sendFeedback(() -> Text.literal("Önce kayıt olunuz."), false);
                                return 1;
                            }

                            boolean isSuccess = authHandler.login(user, pass);
                            if (!isSuccess) {
                                context.getSource().sendFeedback(() -> Text.literal("Giriş sırasında hata oluştu, şifreyi kontrol ediniz."), false);
                                return 1;
                            }

                            context.getSource().getPlayer().getAbilities().invulnerable = false;
                            context.getSource().getPlayer().sendAbilitiesUpdate();
                            PlayerStateHandler.INSTANCE.remove(context.getSource().getPlayer());
                            context.getSource().sendFeedback(() -> Text.literal("Giriş başarılı!"), false);
                            return 1;
                        }))));
    }

    private void registerPlayerLock() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if(PlayerStateHandler.INSTANCE.isLocked(player)) {
                    lockPlayerInputs(player, PlayerStateHandler.INSTANCE.getPlayerLockedLocation(player));
                }
            });
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> checkPlayerLock(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> checkPlayerLock(player));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> checkPlayerLock(player));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> checkPlayerLock(player));


        UseItemCallback.EVENT.register((player, world, hand) -> {
            if(PlayerStateHandler.INSTANCE.isLocked(player)) {
                sendLoginMessage(player);
                return TypedActionResult.fail(player.getStackInHand(hand));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

    }

    @NotNull
    private static ActionResult checkPlayerLock(PlayerEntity player) {
        if(PlayerStateHandler.INSTANCE.isLocked(player)) {
            sendLoginMessage(player);
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private static void sendLoginMessage(PlayerEntity player) {
        player.sendMessage(Text.literal("Giriş yapınız!"), true); // Inform the player
    }

}