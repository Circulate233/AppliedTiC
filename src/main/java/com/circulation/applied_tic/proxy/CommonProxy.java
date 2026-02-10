package com.circulation.applied_tic.proxy;

import static com.circulation.applied_tic.registry.ItemRegistry.itemCell;

import java.util.Collections;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.circulation.applied_tic.Config;
import com.circulation.applied_tic.handler.BuildHandler;
import com.circulation.applied_tic.handler.StorageHandler;
import com.circulation.applied_tic.items.TiCStorageCell;
import com.circulation.applied_tic.part.MEPartMaterial;
import com.circulation.applied_tic.registry.ItemRegistry;

import appeng.api.AEApi;
import appeng.api.features.InscriberProcessType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.crafting.StencilBuilder;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        ItemRegistry.preInit();
        PatternBuilder.instance.addToolPattern(ItemRegistry.pattern);
        StencilBuilder.registerStencil(704, new ItemStack(ItemRegistry.pattern));
        StencilBuilder.registerStencil(705, new ItemStack(ItemRegistry.pattern, 1, 1));
        StencilBuilder.registerStencil(706, new ItemStack(ItemRegistry.pattern, 1, 2));
        TConstructRegistry.addItemToDirectory("TiC Storage Cell", itemCell);
    }

    public void init(FMLInitializationEvent event) {
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(TiCStorageCell.getCellHandler());
        TConstructRegistry
            .addToolRecipe(itemCell, itemCell.getHeadItem(), itemCell.getHandleItem(), itemCell.getAccessoryItem());
        var inscriber = AEApi.instance()
            .registries()
            .inscriber();
        var pattern = new ItemStack(ItemRegistry.pattern, 1, 2);
        for (var i : MEPartMaterial.CellParts.values()) {
            var m = MEPartMaterial.createMaterial(2, i, ItemRegistry.part);
            TConstructRegistry.addCustomMaterial(m);
            inscriber.addRecipe(
                inscriber.builder()
                    .withProcessType(InscriberProcessType.Inscribe)
                    .withInputs(Collections.singletonList(i.getMaterial()))
                    .withOutput(m.craftingItem)
                    .withTopOptional(pattern)
                    .build());
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(StorageHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(BuildHandler.INSTANCE);
    }
}
