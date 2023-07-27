package com.example.addon.modules;

import com.example.addon.Main_Addon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.movement.EntitySpeed;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DonkeyRider extends Module {
    Module EntitySpeed = new EntitySpeed();
    private int DISTANCE = 1000;
    private double PITCH = 0.0f;
    private double YAW = 0.0f;
    private static boolean riding = false;
    private static int distanceTraveled = 0;

    public DonkeyRider() {
        super(Main_Addon.CATEGORY, "DonkeyRider", "Riding the Donkey");
        MeteorClient.EVENT_BUS.subscribe(this);
    }
    private final Setting<Double> Pitch = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("Pitch")
        .description("Pitch of the player")
        .defaultValue(0.0f)
        .min(-90f)
        .max(90)
        .sliderMin(-90f)
        .sliderMax(90)
        .build()
    );
    private final Setting<Double> Yaw = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("Yaw")
        .description("Yaw of the player")
        .defaultValue(0.0f)
        .min(-180f)
        .max(180)
        .sliderMin(-180f)
        .sliderMax(180)
        .build()
    );
    private final Setting<Integer> Distance = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("Distance")
        .description("Distance for the donkey to travel first")
        .defaultValue(1001)
        .min(1000)
        .max(2000)
        .sliderMin(1010)
        .sliderMax(2000)
        .build()
    );
    private final Setting<Integer> WaitBeforeMoving = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("WaitBeforeMoving")
        .description("Wait Before Moving the donkey again")
        .defaultValue(100)
        .min(1)
        .max(1000)
        .sliderMin(1)
        .sliderMax(1000)
        .build()
    );

    @Override
    public void onActivate() {
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        EntitySpeed.onDeactivate();
        setPressed(mc.options.forwardKey, false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        PlayerEntity player = mc.player;
        PITCH = Pitch.get();
        YAW = Yaw.get();
        DISTANCE = Distance.get();

        if (player!=null && isActive()) {
            if (player.hasVehicle() && player.getVehicle() instanceof DonkeyEntity) {

                player.setYaw((float) YAW);
                player.setPitch((float) PITCH);
                setPressed(mc.options.forwardKey,isActive());

                distanceTraveled++;
                if (distanceTraveled >= DISTANCE) {
                    riding = false;
                    setPressed(mc.options.forwardKey,false);
                    for(double i=0; i<WaitBeforeMoving.get(); i++)
                    {}
                    if (Yaw.get() >= 0) {
                        YAW = Yaw.get() - 180;
                    } else {
                        YAW = Yaw.get() + 180;
                    }
                    Yaw.set(YAW);
                    riding=true;
                    distanceTraveled=0;
                }
            } else {
                riding = false;
                setPressed(mc.options.forwardKey,false);
            }
        }
    }
    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
}
