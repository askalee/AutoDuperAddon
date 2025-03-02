package com.example.addon.hud;

import com.example.addon.Main_Addon;
import com.example.addon.modules.DonkeyRider;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.stat.StatFormatter.DECIMAL_FORMAT;

public class HudExample extends HudElement {
    public HudExample() {
        super(INFO);
    }    public static final HudElementInfo<HudExample> INFO = new HudElementInfo<>(Main_Addon.HUD_GROUP, "AutoDuper", "HUD element for AutoDuper 6b6t addon.", HudExample::new);

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player != null) {
            int size;
            String firstPosText = "Starting position: " + DonkeyRider.getFirstPos().replace("(", "").replace(")", "");
            String finalPosText = "Final position: " + DonkeyRider.getFinalPos().replace("(", "").replace(")", "");
            if (DonkeyRider.getFirstPosVec() == null) {
                size = mc.textRenderer.getWidth(finalPosText);
            } else {
                renderer.text("Distance from starting position: " + DECIMAL_FORMAT.format(mc.player.getPos().distanceTo(DonkeyRider.getFirstPosVec())) + " blocks", x + 1, y + renderer.textHeight() * 3, Color.WHITE, true);
                size = mc.textRenderer.getWidth("Distance from starting position: " + DECIMAL_FORMAT.format(mc.player.getPos().distanceTo(DonkeyRider.getFirstPosVec())) + " blocks");
            }
            setSize(size + 5, renderer.textHeight(true) * 4 + 2);
            renderer.text(firstPosText, x, y + renderer.textHeight(), Color.WHITE, true);
            renderer.text(finalPosText, x + 1, y + renderer.textHeight() * 2, Color.WHITE, true);
            renderer.text(" Imperials On Top!!! ", x + 1, y, Color.WHITE, true);
        }
    }


}
