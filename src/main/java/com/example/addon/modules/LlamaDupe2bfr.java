package com.example.addon.modules;

import com.example.addon.Main_Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.concurrent.TimeUnit;

public class LlamaDupe2bfr extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between between each consecutive dupe in ticks (2 ticks ~= 1 second (scaled; so for 15 seconds it should be at 30 ticks)")
        .defaultValue(30)
        .sliderMin(10)
        .sliderMax(200)
        .build()
    );
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Sends debug messages to indicate what is happening or if any error occurred")
        .defaultValue(false)
        .build()
    );
    private int timer = 0;


    public LlamaDupe2bfr() {
        super(Main_Addon.CATEGORY,"LlamaDupe2b2fr","Performs the llama dupe for 2b2fr.org");
    }

    @Override
    public void onActivate() {
        timer = delay.get() * 10;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.interactionManager == null) return;

        if(mc.player.getVehicle() instanceof LlamaEntity){
            mc.player.dismountVehicle();
            ChatUtils.sendPlayerMsg(".dismount");

            if(debug.get()){
               ChatUtils.sendMsg(Text.of("Dismounted"));
            }
        }

        if (timer > 0) {
            timer--;
            return;
        } else {
            timer = delay.get() * 10;
            if(debug.get()){
                ChatUtils.sendMsg(Text.of("Delay in s: " + (timer / 20) ));
            }
        }
        LlamaEntity llamaEntity = getNearestLlamaEntity();

        if(llamaEntity == null){
            ChatUtils.sendMsg(Text.of("Could not find a llama entity within interaction range... Toggling"));
            toggle();
            return;
        }
        ActionResult actionResult =  mc.interactionManager.interactEntity(mc.player,llamaEntity,Hand.MAIN_HAND);

        if(debug.get()){
            ChatUtils.sendMsg(Text.of("Interaction status: " + actionResult.toString()));
        }
    }

    private LlamaEntity getNearestLlamaEntity() {
        return (LlamaEntity)  mc.world.getOtherEntities(mc.player, new Box(mc.player.getBlockPos()).expand(mc.player.getEntityInteractionRange()), entity -> entity instanceof LlamaEntity)
            .stream()
            .min((entity1, entity2) -> Float.compare(entity1.distanceTo(mc.player), entity2.distanceTo(mc.player)))
            .orElse(null);
    }

}
