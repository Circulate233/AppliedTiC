package com.circulation.applied_tic.part;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import tconstruct.library.tools.CustomMaterial;

public class MEPartMaterial extends CustomMaterial {

    private static final Int2ObjectMap<MEPartMaterial> map = new Int2ObjectOpenHashMap<>();
    private static final IMaterials materials = AEApi.instance()
        .definitions()
        .materials();
    public static final int startID = 704;

    @Getter
    private final long multiplier;

    private MEPartMaterial(int materialID, int value, ItemStack input, ItemStack craftingItem, long multiplier) {
        super(materialID, value, input, craftingItem, 0x6495EDff);
        this.multiplier = multiplier;
    }

    public static IntSet getAllToolPartMaterialID() {
        return MEPartMaterial.map.keySet();
    }

    public static MEPartMaterial createMaterial(int value, IPartMaterial input, Item craftingItem) {
        final MEPartMaterial part;
        final var materialID = map.size() + startID;
        map.put(
            materialID,
            part = new MEPartMaterial(
                materialID,
                value,
                input.getMaterial(),
                new ItemStack(craftingItem, 1, materialID),
                input.getMultiplier()));
        return part;
    }

    public static MEPartMaterial getMaterial(int materialID) {
        return map.get(materialID);
    }

    public enum CellParts implements IPartMaterial {

        K1(materials.cell1kPart(), 1),
        K4(materials.cell4kPart(), 4),
        K16(materials.cell16kPart(), 16),
        K64(materials.cell64kPart(), 64),
        K256(materials.cell256kPart(), 256),
        K1024(materials.cell1024kPart(), 1024),
        K4096(materials.cell4096kPart(), 4096),
        K16384(materials.cell16384kPart(), 16384),;

        private final ItemStack item;
        @Getter
        private final long multiplier;

        CellParts(IItemDefinition definition, long multiplier) {
            this.item = definition.maybeStack(1)
                .orNull();
            this.multiplier = multiplier;
        }

        @Override
        public ItemStack getMaterial() {
            return item;
        }

    }

    public interface IPartMaterial {

        ItemStack getMaterial();

        long getMultiplier();
    }
}
