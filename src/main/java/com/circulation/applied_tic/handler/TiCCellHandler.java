package com.circulation.applied_tic.handler;

import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import com.circulation.applied_tic.tools.TiCStorageCell;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public abstract class TiCCellHandler implements ICellHandler {

    private final TiCStorageCell storageCell;

    public TiCCellHandler(TiCStorageCell storageCell) {
        this.storageCell = storageCell;
    }

    @Override
    public final boolean isCell(ItemStack is) {
        return is != null && is.getItem() == storageCell;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return null;
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return null;
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return null;
    }

    @Override
    public int getStatusForCell(ItemStack is, IMEInventory handler) {
        return 0;
    }

    @Override
    public double cellIdleDrain(ItemStack is, IMEInventory handler) {
        return 0;
    }
}
