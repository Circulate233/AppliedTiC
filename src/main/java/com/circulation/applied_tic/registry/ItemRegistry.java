package com.circulation.applied_tic.registry;

import com.circulation.applied_tic.AppliedTiC;
import com.circulation.applied_tic.tools.TiCStorageCell;

import cpw.mods.fml.common.registry.GameRegistry;
import tconstruct.library.tools.DynamicToolPart;

public final class ItemRegistry {

    public static TiCStorageCell itemCell;
    public static DynamicToolPart housing;
    public static DynamicToolPart core;

    public static void preInit() {
        GameRegistry.registerItem(itemCell = TiCStorageCell.INSTANCE, "item_cell");
        GameRegistry.registerItem(
            housing = new DynamicToolPart("_me_cell_housing", "housing", AppliedTiC.MODID),
            "me_cell_housing");
        GameRegistry
            .registerItem(core = new DynamicToolPart("_me_cell_core", "core", AppliedTiC.MODID), "me_cell_core");
    }

}
