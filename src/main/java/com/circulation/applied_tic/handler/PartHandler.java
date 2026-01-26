package com.circulation.applied_tic.handler;

import com.circulation.applied_tic.part.MEPartMaterial;
import com.circulation.applied_tic.registry.ItemRegistry;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.PartBuilderEvent;
import tconstruct.library.event.ToolCraftEvent;

public final class PartHandler {

    public static final PartHandler INSTANCE = new PartHandler();

    private PartHandler() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onPartBuilder(PartBuilderEvent.NormalPart event) {
        if (event.pattern.getItem() != ItemRegistry.pattern || event.pattern.getItemDamage() != 2) return;
        var e = TConstructRegistry.getCustomMaterial(event.material, MEPartMaterial.class);
        if (e == null) return;
        event.overrideResult(new net.minecraft.item.ItemStack[] { e.craftingItem.copy(), null });
    }

    @SubscribeEvent
    public void onToolCraft(ToolCraftEvent.NormalTool event) {
        if (event.tool == ItemRegistry.itemCell) {
            var m = MEPartMaterial.getMaterial(
                event.toolTag.getCompoundTag("InfiTool")
                    .getInteger("Accessory"));
            if (m == null) return;
            event.toolTag.getCompoundTag("InfiTool")
                .setLong("byteMultiolier", m.getMultiplier());
        }
    }
}
