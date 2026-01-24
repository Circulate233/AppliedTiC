package com.circulation.applied_tic.registry;

import com.circulation.applied_tic.tools.TiCStorageCell;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemRegistry {

    public static TiCStorageCell itemCell;

    public static void preInit() {
        GameRegistry.registerItem(itemCell = TiCStorageCell.INSTANCE, "item_cell");
    }

}
