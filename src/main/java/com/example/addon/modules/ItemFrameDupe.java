package com.example.addon.modules;

import com.example.addon.Main_Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class ItemFrameDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgAutoScroll = settings.createGroup("Auto Scroll");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgAutoDisable = settings.createGroup("Auto Disable");

    private final Setting<Integer> distance = sgGeneral.add(new IntSetting.Builder()
        .name("distance")
        .description("Max distance to search for placement spots.")
        .defaultValue(3)
        .min(1)
        .sliderMax(6)
        .build()
    );
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate-player")
        .description("Rotate Player when placing.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> rotateItem = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate-item")
        .description("Keep rotating item frame.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Swap back to previous item after placing.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> tpsBasedDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("tps-based-delay")
        .description("Adjust delays based on TPS and ping.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> placeItemFrame = sgPlace.add(new BoolSetting.Builder()
        .name("place-item-frame")
        .description("Place item frames if needed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> placeDelay = sgPlace.add(new IntSetting.Builder()
        .name("place-delay")
        .description("Delay between placement cycles in ticks.")
        .defaultValue(2)
        .min(0)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> maxPlacesPerTick = sgPlace.add(new IntSetting.Builder()
        .name("max-places-per-tick")
        .description("Max item frames to place per tick.")
        .defaultValue(1)
        .min(1)
        .sliderMax(8)
        .build()
    );
    private final Setting<BlockPos> preferredPosition = sgPlace.add(new BlockPosSetting.Builder()
        .name("preferred-position")
        .description("Preferred position for placing item frames.")
        .defaultValue(BlockPos.ORIGIN)
        .build()
    );

    private final Setting<Boolean> multiBreak = sgBreak.add(new BoolSetting.Builder()
        .name("multi-break")
        .description("Break multiple item frames per tick.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> breakDelay = sgBreak.add(new IntSetting.Builder()
        .name("break-delay")
        .description("Delay between breaking item frames in ticks.")
        .defaultValue(2)
        .min(0)
        .sliderMax(60)
        .build()
    );
    private final Setting<Integer> maxBreaksPerTick = sgBreak.add(new IntSetting.Builder()
        .name("max-breaks-per-tick")
        .description("Max item frames to break per tick.")
        .defaultValue(1)
        .min(1)
        .sliderMax(8)
        .build()
    );

    private final Setting<Boolean> autoScroll = sgAutoScroll.add(new BoolSetting.Builder()
        .name("auto-scroll-hotbar")
        .description("Scroll hotbar to dupe items.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> scrollDelay = sgAutoScroll.add(new IntSetting.Builder()
        .name("scroll-delay")
        .description("Delay between hotbar scrolls in ticks.")
        .defaultValue(3)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> renderPlacement = sgRender.add(new BoolSetting.Builder()
        .name("render-placement")
        .description("Render placement positions.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Render style for placement positions.")
        .defaultValue(ShapeMode.Lines)
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Side color of placement render.")
        .defaultValue(new SettingColor(255, 255, 255, 75))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Line color of placement render.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<Boolean> autoDisable = sgAutoDisable.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disable after set dupes or inventory depletion.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> maxDupes = sgAutoDisable.add(new IntSetting.Builder()
        .name("max-dupes")
        .description("Max successful dupes before disabling.")
        .defaultValue(100)
        .min(1)
        .sliderMax(1000)
        .visible(autoDisable::get)
        .build()
    );
    private final Setting<Boolean> disableOnEmpty = sgAutoDisable.add(new BoolSetting.Builder()
        .name("disable-on-empty")
        .description("Disable when no item frames left.")
        .defaultValue(true)
        .visible(autoDisable::get)
        .build()
    );

    private int placeTimer, autoScrollTimer, breakTimer, dupeCount;
    private final List<BlockPos> placementPositions = new ArrayList<>();
    private final PriorityQueue<BlockPos> placementQueue = new PriorityQueue<>((a, b) ->
        Double.compare(mc.player.getBlockPos().getSquaredDistance(a), mc.player.getBlockPos().getSquaredDistance(b)));

    public ItemFrameDupe() {
        super(Main_Addon.CATEGORY, "ItemFrameDupe", "Places and dupes item frames efficiently.");
    }

    @Override
    public void onActivate() {
        placeTimer = autoScrollTimer = breakTimer = dupeCount = 0;
        updatePlacementPositions();
    }

    @Override
    public void onDeactivate() {
        placementPositions.clear();
        placementQueue.clear();
        ChatUtils.infoPrefix("Dupe", "Duped %d items", dupeCount);
        dupeCount = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (autoScroll.get() && autoScrollTimer <= 0) {
            int slot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = (slot + 1) % 9;
            ((IClientPlayerInteractionManager) mc.interactionManager).meteor$syncSelected();
            autoScrollTimer = getDelay(scrollDelay.get());
        } else {
            autoScrollTimer--;
        }

        Box box = new Box(mc.player.getPos().add(-3, -3, -3), mc.player.getPos().add(3, 3, 3));
        List<ItemFrameEntity> itemFrames = mc.world.getEntitiesByClass(ItemFrameEntity.class, box, e -> true);
        int existingFrames = itemFrames.size();

        if (placeItemFrame.get() && placeTimer <= 0 && existingFrames < maxPlacesPerTick.get()) {
            FindItemResult itemResult = InvUtils.findInHotbar(Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME);
            if (!itemResult.found()) {
                if (autoDisable.get() && disableOnEmpty.get()) {
                    ChatUtils.info("No item frames, disabling.");
                    toggle();
                } else {
                    ChatUtils.error("No item frames in hotbar.");
                }
                return;
            }

            int toPlace = maxPlacesPerTick.get() - existingFrames;
            int placed = 0;
            while (!placementQueue.isEmpty() && placed < toPlace) {
                BlockPos pos = placementQueue.poll();
                if (BlockUtils.canPlace(pos, true)) {
                    if (BlockUtils.place(pos, itemResult, rotate.get(), 50, true, false, swapBack.get())) {
                        placed++;
                    }
                }
            }
            placeTimer = getDelay(placeDelay.get());
        } else {
            placeTimer--;
        }

        if (breakTimer <= 0 && !itemFrames.isEmpty()) {
            int breaksThisTick = 0;
            for (ItemFrameEntity frame : itemFrames) {
                if (breaksThisTick >= maxBreaksPerTick.get()) break;
                if (!frame.getBlockPos().isWithinDistance(mc.player.getBlockPos(), mc.player.getEntityInteractionRange())) continue;
                if (mc.player.getMainHandStack().getItem() == Items.ITEM_FRAME || mc.player.getMainHandStack().getItem() == Items.GLOW_ITEM_FRAME) continue;

                mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                if (rotateItem.get() && !frame.getHeldItemStack().isEmpty()) {
                    mc.interactionManager.interactEntity(mc.player, frame, Hand.MAIN_HAND);
                }
                if (!frame.getHeldItemStack().isEmpty() && frame.isAlive() && frame.isAttackable()) {
                    mc.interactionManager.attackEntity(mc.player, frame);
                    breaksThisTick++;
                    dupeCount++;
                    if (autoDisable.get() && dupeCount >= maxDupes.get()) {
                        ChatUtils.info("Reached %d dupes, disabling.", maxDupes.get());
                        toggle();
                        return;
                    }
                }
                if (!multiBreak.get()) break;
            }
            breakTimer = getDelay(breakDelay.get());
        } else {
            breakTimer--;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (renderPlacement.get()) {
            for (BlockPos pos : placementQueue) {
                event.renderer.box(pos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }
        }
    }

    private void updatePlacementPositions() {
        placementPositions.clear();
        placementQueue.clear();

        if (!preferredPosition.get().equals(BlockPos.ORIGIN)) {
            if (BlockUtils.canPlace(preferredPosition.get(), true)) {
                placementPositions.add(preferredPosition.get());
                placementQueue.add(preferredPosition.get());
            } else {
                ChatUtils.error("Cannot place at preferred position.");
            }
        } else {
            Vec3d lookDir = mc.player.getRotationVec(1.0F).normalize();
            List<BlockPos> positions = getForwardSphere(mc.player.getBlockPos(), distance.get(), lookDir);
            for (BlockPos pos : positions) {
                if (BlockUtils.canPlace(pos, true)) {
                    placementPositions.add(pos);
                    placementQueue.add(pos);
                }
            }
        }
    }

    private List<BlockPos> getForwardSphere(BlockPos center, int radius, Vec3d lookDir) {
        List<BlockPos> positions = new ArrayList<>();
        int radSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (center.getSquaredDistance(pos) > radSq) continue;
                    Vec3d toPos = new Vec3d(pos.getX() - center.getX(), pos.getY() - center.getY(), pos.getZ() - center.getZ()).normalize();
                    if (lookDir.dotProduct(toPos) > 0) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private int getDelay(int baseDelay) {
        if (tpsBasedDelay.get()) {
            float tps = TickRate.INSTANCE.getTickRate();
            int ping = PlayerUtils.getPing();
            float adjustedDelay = baseDelay * (20.0f / Math.max(tps, 1.0f)) + (ping / 50.0f);
            return Math.max(1, Math.round(adjustedDelay));
        }
        return baseDelay;
    }
}
