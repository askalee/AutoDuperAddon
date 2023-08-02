package com.example.addon.hud;

import com.example.addon.Main_Addon;
import com.example.addon.modules.DonkeyRider;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HudExample extends HudElement {
    public static final HudElementInfo<HudExample> INFO = new HudElementInfo<>(Main_Addon.HUD_GROUP, "AutoDuper", "HUD element for AutoDuper 6b6t addon.", HudExample::new);

    public HudExample() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        String firstPos="Starting position: "+ DonkeyRider.getFirstPos().replace("(","").replace(")","");
        String finalPos="Final position: "+ DonkeyRider.getFinalPos().replace("(","").replace(")","");
        int firstPosWidth = mc.textRenderer.getWidth(firstPos);
        int finalPosWidth = mc.textRenderer.getWidth(finalPos);
        int size = Math.max(firstPosWidth, finalPosWidth);
        setSize(size + 5, renderer.textHeight(true)*2+2);
        renderer.text(firstPos, x, y, Color.WHITE, true);
        renderer.text(finalPos, x + 1, y + renderer.textHeight(), Color.WHITE, true);


    }
}
