package com.circulation.applied_tic.registry;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.circulation.applied_tic.AppliedTiC;
import com.circulation.applied_tic.items.TiCPattern;
import com.circulation.applied_tic.items.TiCStorageCell;
import com.circulation.applied_tic.part.MEPartMaterial;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.library.tools.DynamicToolPart;

public final class ItemRegistry {

    public static TiCStorageCell itemCell;
    public static TiCPattern pattern;
    public static DynamicToolPart housing;
    public static DynamicToolPart core;
    public static DynamicToolPart part;

    public static void preInit() {
        GameRegistry.registerItem(itemCell = TiCStorageCell.INSTANCE, "item_cell");
        GameRegistry.registerItem(pattern = new TiCPattern(), "pattern");
        GameRegistry
            .registerItem(part = new DynamicToolPart("_me_cell_part", "part", AppliedTiC.MODID, MEPartMaterial.class) {
                @SuppressWarnings("unchecked")
                @Override
                @SideOnly(Side.CLIENT)
                public void getSubItems(Item item, CreativeTabs tab, List list) {
                    for (int i : MEPartMaterial.getAllToolPartMaterialID()) {
                        list.add(new ItemStack(item, 1, i));
                    }
                }

                @Override
                public int getMaterialID(ItemStack stack) {
                    return stack.getItemDamage();
                }
            }, "me_cell_part");
        GameRegistry.registerItem(
            housing = new DynamicToolPart("_me_cell_housing", "housing", AppliedTiC.MODID),
            "me_cell_housing");
        GameRegistry
            .registerItem(core = new DynamicToolPart("_me_cell_core", "core", AppliedTiC.MODID), "me_cell_core");
    }

}
