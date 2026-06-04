package com.circulation.applied_tic.handler;

import com.circulation.applied_tic.part.MEPartMaterial;
import com.circulation.applied_tic.registry.ItemRegistry;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tconstruct.library.event.ToolCraftEvent;

public final class BuildHandler {

    public static final BuildHandler INSTANCE = new BuildHandler();

    private BuildHandler() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SubscribeEvent
    public void onToolCraft(ToolCraftEvent.NormalTool event) {
        if (event.tool == ItemRegistry.itemCell) {
            var tag = event.toolTag.getCompoundTag("InfiTool");
            var m = MEPartMaterial.getMaterial(tag.getInteger("Accessory"));
            if (m == null) return;
            tag.setLong("byteMultiolier", m.getMultiplier());
            long baseByte = tag.getInteger("TotalDurability");
            tag.setLong("baseByte", baseByte);
            tag.setLong("totalByte", baseByte);
            var a = event.materials[0];
            var b = event.materials[1];
            var baseTypes = process((int) Math.ceil(Math.pow(((a.harvestLevel + 1) * (1 + b.harvestLevel)), 0.75)));
            tag.setInteger("baseTypes", baseTypes);
            tag.setInteger("totalTypes", baseTypes);
        }
    }

    private static int process(int baseType) {
        if (baseType < 2) {
            return baseType * 16;
        } else if (baseType < 8) {
            return baseType * 12;
        } else if (baseType < 16) {
            return baseType * 10;
        } else if (baseType < 32) {
            return baseType * 8;
        } else return baseType * 6;
    }
}
