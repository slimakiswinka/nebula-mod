/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.TitleScreenCredits;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import meteordevelopment.meteorclient.utils.render.prompts.YesNoPrompt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    public TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I", ordinal = 0))
    private void onRenderIdkDude(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Utils.firstTimeTitleScreen) {
            Utils.firstTimeTitleScreen = false;

            if (!MeteorClient.VERSION.isZero() && !MeteorClient.BUILD_NUMBER.isEmpty()) {
                MeteorClient.LOG.info("Checking latest version of Nebula Client");

                MeteorExecutor.execute(() -> {
                    String res = Http.get("https://meteorclient.com/api/stats")
                        .exceptionHandler(e -> MeteorClient.LOG.error("Could not fetch version information."))
                        .sendString();
                    if (res == null) return;

                    JsonElement latestBuild = JsonParser.parseString(res).getAsJsonObject()
                        .getAsJsonObject("builds")
                        .get(MeteorClient.VERSION.toString());

                    if (latestBuild == null)
                        return;

                    if (latestBuild.getAsInt() > Integer.parseInt(MeteorClient.BUILD_NUMBER)) {
                        YesNoPrompt.create()
                            .title("New Update")
                            .message("A new version of Nebula has been released.")
                            .message("Your version: %s", MeteorClient.VERSION + "-" + MeteorClient.BUILD_NUMBER)
                            .message("Latest version: %s", MeteorClient.VERSION + "-" + latestBuild.getAsInt())
                            .message("Do you want to update?")
                            .onYes(() -> Util.getOperatingSystem().open("https://discord.gg/2J795y9QVM"))
                            .onNo(() -> OkPrompt.create()
                                .title("Are you sure?")
                                .message("Using old versions of Nebula is not recommended")
                                .message("and could result in issues.")
                                .id("new-update-no")
                                .onOk(this::close)
                                .show())
                            .id("new-update")
                            .show();
                    }
                });
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.get().titleScreenCredits.get()) TitleScreenCredits.render(context);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (Config.get().titleScreenCredits.get() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (TitleScreenCredits.onClicked(mouseX, mouseY)) info.setReturnValue(true);
        }
    }
}
