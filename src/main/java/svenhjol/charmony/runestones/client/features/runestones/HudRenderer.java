package svenhjol.charmony.runestones.client.features.runestones;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import svenhjol.charmony.core.client.BaseHudRenderer;
import svenhjol.charmony.runestones.common.features.runestones.Helpers;
import svenhjol.charmony.runestones.common.features.runestones.Networking;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneBlockEntity;

import javax.annotation.Nullable;

public class HudRenderer extends BaseHudRenderer {
    private final int nameColor;
    private final int runesColor;
    private final int discoveredColor;

    private BlockPos lastLookedAt = null;
    private MutableComponent runes;
    private MutableComponent name;
    private MutableComponent discovered;
    private MutableComponent activateWith;
    @Nullable private ItemStack sacrifice;

    public HudRenderer() {
        runesColor = 0xbfaf9f;
        nameColor = 0xf8f8ff;
        discoveredColor = 0xafbfcf;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var feature = Runestones.feature();
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        var y = 64;
        var lineHeight = 14;

        if (ticksFade == 0) return;

        var font = minecraft.font;
        var midX = (int)(window.getGuiScaledWidth() / 2.0f);
        var alpha = Math.max(4, Math.min(MAX_FADE_TICKS, ticksFade)) << 24 & 0xff000000;
        var scale = Math.max(0f, Math.min(1.0f, (ticksFade / (float) MAX_FADE_TICKS)));
        var textShadow = feature.hudHasShadowText();

        int nameStringLength;
        int runesStringLength;
        int discoveredStringLength;
        int activateWithStringLength;

        runesStringLength = runes.getString().length() * 5;
        nameStringLength = name.getString().length() * 5;
        discoveredStringLength = discovered.getString().length() * 5;
        activateWithStringLength = activateWith.getString().length() * 5; // reserve space for the item!

        if (feature.hudHasBackground()) {
            guiGraphics.fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0x505050 | alpha);
        }

        if (nameStringLength > 0) {
            y += lineHeight;
            int lx = (int) (midX - (float) (nameStringLength / 2) - 2) + 3;
            guiGraphics.drawString(font, name, lx, y, nameColor | alpha, textShadow);
        }

        if (discoveredStringLength > 0) {
            y += lineHeight;
            int lx = (int) (midX - (float) (discoveredStringLength / 2) - 2);
            guiGraphics.drawString(font, discovered, lx, y, discoveredColor | alpha, textShadow);
        }

        if (runesStringLength > 0) {
            y += lineHeight;
            int lx = (int) (midX - (float) (runesStringLength / 2) - 2);
            guiGraphics.drawString(font, runes, lx, y, runesColor | alpha, textShadow);
        }

        if (activateWithStringLength > 0 && sacrifice != null) {
            y += lineHeight;
            int lx = (int) (midX - (float) (activateWithStringLength / 2) - 2);
            guiGraphics.drawString(font, activateWith, lx, y, nameColor | alpha, textShadow);

            int ix = midX + (activateWithStringLength / 2) - 6;
            int iy = y - 4;
            renderScaledGuiItem(guiGraphics, sacrifice, ix, iy, scale, scale);
        }

        doFadeTicks();
    }

    @Override
    protected boolean isValid(Player player) {
        var level = player.level();
        var feature = Runestones.feature();
        var lookedAt = feature.handlers.lookingAtBlock(player);
        var isCreative = player.getAbilities().instabuild;

        if (level.getBlockEntity(lookedAt) instanceof RunestoneBlockEntity runestone) {
            discovered = Component.empty();

            if (runestone.location == null) {
                return false; // invalid
            }

            if (lastLookedAt == null || lastLookedAt != lookedAt) {
                Networking.C2SPlayerLooking.send(lookedAt);
                lastLookedAt = lookedAt;
            }

            sacrifice = runestone.sacrifice;
            activateWith = Component.translatable("gui.charmony-runestones.runestone.activate_with");

            runes = Component.literal(feature.handlers.runicName(runestone.location))
                .withStyle(feature.registers.runeFont);

            if (runestone.discovered == null && isCreative) {
                name = Component.translatable(Helpers.localeKey(runestone.location));
                discovered = Component.translatable("gui.charmony-runestones.runestone.discovered_by", "Creative mode");
                return true;
            }

            if (runestone.hasBeenDiscovered()) {
                name = Component.translatable(Helpers.localeKey(runestone.location));

                if (runestone.discovered != null) {
                    // Show the "Discovered by" message.
                    discovered = Component.translatable("gui.charmony-runestones.runestone.discovered_by",
                        Component.translatable(runestone.discovered));
                }
            } else {
                name = Component.translatable("gui.charmony-runestones.runestone.unknown");
            }

            return true;
        }

        return false;
    }

    /**
     * Render displayed item with scaling.
     */
    @Override
    public void scaleItem(ItemStack stack, PoseStack poseStack) {
        poseStack.scale(scaleX, scaleY, 1.0f);
        scaleX = scaleY = 1.0f;
    }
}
