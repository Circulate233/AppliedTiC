package com.circulation.applied_tic.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import tconstruct.tools.items.Pattern;

public class TiCPattern extends Pattern {

    private static final String[] patternName = new String[] { "housing", "core", "part" };

    public TiCPattern() {
        super(patternName, getPatternNames(), "patterns");
        setUnlocalizedName("pattern");
    }

    private static String[] getPatternNames() {
        var s = new String[patternName.length];
        for (int i = 0; i < patternName.length; i++) {
            s[i] = "pattern_" + patternName[i];
        }
        return s;
    }

    @Override
    public int getPatternCost(ItemStack pattern) {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubItems(Item b, CreativeTabs tab, List list) {
        for (int i = 0; i < patternName.length; i++) {
            list.add(new ItemStack(b, 1, i));
        }
    }
}
