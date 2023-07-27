package com.example.addon.hud;

import com.example.addon.Main_Addon;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class HudExample extends HudElement {
    public static final HudElementInfo<HudExample> INFO = new HudElementInfo<>(Main_Addon.HUD_GROUP, "AutoDuper", "HUD element for AutoDuper 6b6t addon.", HudExample::new);

    public HudExample() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("AutoDuper", true), renderer.textHeight(true));

        renderer.text("AutoDuper for 6b6t is running", x, y, Color.WHITE, true);
    }
}
