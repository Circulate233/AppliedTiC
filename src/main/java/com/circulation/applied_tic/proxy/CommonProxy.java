package com.circulation.applied_tic.proxy;

import appeng.api.AEApi;
import com.circulation.applied_tic.Config;
import com.circulation.applied_tic.registry.ItemRegistry;
import com.circulation.applied_tic.tools.TiCStorageCell;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import tconstruct.library.TConstructRegistry;

import static com.circulation.applied_tic.registry.ItemRegistry.itemCell;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        ItemRegistry.preInit();
        TConstructRegistry.addItemToDirectory("TiC Storage Cell", itemCell);
    }

    public void init(FMLInitializationEvent event) {
        AEApi.instance()
             .registries()
             .cell()
             .addCellHandler(TiCStorageCell.getCellHandler());
        TConstructRegistry.addToolRecipe(
            itemCell,
            itemCell.getHeadItem(),
            itemCell.getHandleItem()
        );
    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}
