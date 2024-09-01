package com.example;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.netty.util.internal.StringUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ExampleMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

    public IAuthHandler authHandler = new JsonAuthHandler();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("register")
                        .then(argument("password", StringArgumentType.word())
                                .then(argument("repeatPassword", StringArgumentType.word())
                                        .executes(context -> {
                                            LOGGER.info("Hello Fabric world!");
                                            String pass = StringArgumentType.getString(context, "password");
                                            String pass2 = StringArgumentType.getString(context, "repeatPassword");
                                            if (!Objects.equals(pass, pass2) || StringUtil.isNullOrEmpty(pass)) {
                                                context.getSource().sendFeedback(() -> Text.literal("Şifreler uyuşmuyor."), false);
                                                return 1;
                                            }

                                            String user = context.getSource().getName();

                                            if (authHandler.userExists(user)) {
                                                context.getSource().sendFeedback(() -> Text.literal("Zaten kayıt olmuşsunuz."), false);
                                                return 1;
                                            }

                                            boolean isSuccess = authHandler.registerUpdate(user, pass, false);
                                            if (!isSuccess) {
                                                context.getSource().sendFeedback(() -> Text.literal("Kayıt sırasında hata oluştu."), false);
                                                return 1;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal("Kayıt başarılı!"), false);
                                            return 1;
                                        }))
                        )));


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("login")
                        .then(argument("password", StringArgumentType.word())
                        .executes(context -> {
                            String user = context.getSource().getName();
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

                            context.getSource().sendFeedback(() -> Text.literal("Giriş başarılı!"), false);
                            return 1;
                        }))));
    }
}