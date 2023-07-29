package com.example.addon.modules;

import com.example.addon.Main_Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.misc.Spam;
import meteordevelopment.meteorclient.systems.modules.movement.EntitySpeed;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Objects;

public class DonkeyRider extends Module {
    private final Module entitySpeed = new EntitySpeed();
    private final Setting<Double> yaw = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("Yaw")
        .description("Yaw of the player")
        .defaultValue(0.0f)
        .min(-180f)
        .max(180)
        .sliderMin(-180f)
        .sliderMax(180)
        .build()
    );
    private final Setting<Integer> distance = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("Distance")
        .description("Distance for the donkey to travel back and forth")
        .defaultValue(1001)
        .min(500)
        .max(2000)
        .sliderMin(500)
        .sliderMax(2000)
        .build()
    );
    private final Setting<Integer> waitBeforeMoving = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("Delay")
        .description("Wait Before Moving the donkey again after reaching the distance ")
        .defaultValue(1000)
        .min(500)
        .max(10000)
        .sliderMin(500)
        .sliderMax(10000)
        .build()
    );
    private final Setting<Boolean> remount = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("Remount")
        .description("Remounts on the nearby donkey entity")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> RemountTime = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("Remount Delay")
        .description("Delay Before remounting again")
        .defaultValue(1000)
        .min(500)
        .max(10000)
        .sliderMin(500)
        .sliderMax(10000)
        .build()
    );
    Thread runThread;
    private Vec3d firstPos;
    private Vec3d finalPos;
    private Vec3d currentPos;
    private boolean dismounted = false;
    private boolean remounted = false;
    private boolean yawChanged1= false;
    private boolean yawChanged2 = false;

    private double YAW;

    public DonkeyRider() {
        super(Main_Addon.CATEGORY, "DonkeyRider", "Riding the Donkey");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (mc.player != null)
            firstPos = mc.player.getPos();
        if(!entitySpeed.isActive()) entitySpeed.toggle();
        start();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        if(entitySpeed.isActive()) entitySpeed.toggle();
        setPressed(mc.options.forwardKey, false);
        stop();
    }

    private boolean running = true;

    @EventHandler
    @SuppressWarnings("all")
    private void onTick(TickEvent.Post event) {
        if (runThread == null) {
            runThread = new Thread(() -> {
                while (running) {
                    double Yaw = yaw.get();
                    YAW = Yaw;
                    PlayerEntity player = mc.player;
                    if (player != null && player.getPos() != null && isActive()) {
                        if (player.hasVehicle() && player.getVehicle() instanceof DonkeyEntity) {
                            currentPos = player.getPos();
                   //         System.out.println("firstPos: "+firstPos);
                    //        System.out.println("FinalPos: "+finalPos);
                            if (currentPos.distanceTo(firstPos) >= distance.get() && !yawChanged1) {
                                setPressed(mc.options.forwardKey, false);
                                try {
                                    Thread.sleep(waitBeforeMoving.get());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (yaw.get() >= 0) {
                                    yaw.set(yaw.get() - 180);
                                } else {
                                    yaw.set(yaw.get() + 180);
                                }

                                YAW = yaw.get();
                                finalPos = firstPos;
                                firstPos = player.getPos();
                                yaw.set(YAW);
                                yawChanged1 = true;

                            }
                            player.setYaw((float) YAW);
                            setPressed(mc.options.forwardKey, isActive());
                            if (finalPos != null) {
                                double pos = currentPos.distanceTo(finalPos);
                                if (pos <= 5.0f && !dismounted) {
                                    player.dismountVehicle();
                                    ChatUtils.sendPlayerMsg(".dismount");
                                    mc.player.sendMessage(Text.of("Sent dismounted Command"), true);
                                    dismounted = true;
                                    remounted = false;
                                    yawChanged1 = false;
                                    yawChanged2 = false;
                                    System.out.println("Dismounted");
                                    setPressed(mc.options.forwardKey, false);
                                }
                            }
                        } else {
                            setPressed(mc.options.forwardKey,false);
                            if (dismounted && remount.get()) {
                                List<DonkeyEntity> donkeys = player.getWorld().getEntitiesByClass(DonkeyEntity.class, player.getBoundingBox().expand(5), Entity -> !Entity.hasControllingPassenger());
                                if (!donkeys.isEmpty() && !player.hasVehicle() && !remounted && !yawChanged2) {
                                    try {
                                        Thread.sleep((long) (RemountTime.get() * 2));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    //player.startRiding(donkeys.get(0));
                                    mc.interactionManager.interactEntity(player, donkeys.get(0), Hand.MAIN_HAND);
                                    dismounted = false;
                                    finalPos = null;
                                    firstPos = donkeys.get(0).getPos();
                                    if (yaw.get() >= 0) {
                                        yaw.set(yaw.get() - 180);
                                    } else {
                                        yaw.set(yaw.get() + 180);
                                    }
                                    YAW = yaw.get();
                                    yawChanged2 = true;
                                    remounted = true;
                                    yawChanged1 = false;
                                    System.out.println("Remount YAW: " + YAW);
                                }
                            }
                        }
                    }
                }
            });
            runThread.start();
        }
        if(mc.player==null && this.isActive())
            this.toggle();
    }
    public void stop() {
        running = false;
        runThread = null;
    }
    public void start(){
        running=true;
        runThread = null;
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
}
