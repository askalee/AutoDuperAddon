package com.example.addon.modules;

import com.example.addon.Main_Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.util.Hand;

import java.util.Collections;
import java.util.List;

public class DonkeyDuper extends Module {
    public DonkeyDuper() {
        super(Main_Addon.CATEGORY, "DonkeyDuper", "Duping using the Donkey");
    }
private boolean isInvOpened=false;
    @Override
    public void onActivate() {
        super.onActivate();
        ClientPlayerEntity player = mc.player;
        new Thread(()->{
        while(!isInvOpened) {
            if (player != null) {
                List<DonkeyEntity> donkeyEntityList = Collections.singletonList(player.getWorld().getClosestEntity(DonkeyEntity.class, TargetPredicate.DEFAULT, player, player.getX(), player.getY(), player.getZ(), player.getBoundingBox().expand(5)));
                System.out.println(donkeyEntityList);
                if (donkeyEntityList.get(0) != null && donkeyEntityList.get(0).hasChest()) {
                    System.out.println("pressing");
                    mc.options.sneakKey.setPressed(true);
                    mc.interactionManager.interactEntity(player,donkeyEntityList.get(0), Hand.MAIN_HAND);
                    isInvOpened=true;
                    mc.options.sneakKey.setPressed(false);
                }
            }
        }
        while (isInvOpened)
        {
            if (player!=null && !(mc.currentScreen instanceof HorseScreen))
            {
                isInvOpened = false;
            }
        }
        }).start();
    }
    @Override
    public void onDeactivate() {
        super.onDeactivate();
        isInvOpened=false;
        mc.options.sneakKey.setPressed(false);
    }
}
