package com.circulation.applied_tic.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;

import com.circulation.applied_tic.AppliedTiC;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import it.unimi.dsi.fastutil.booleans.BooleanObjectMutablePair;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public final class StorageHandler {

    public static final StorageHandler INSTANCE = new StorageHandler();
    private static final Object2ObjectMap<String, BooleanObjectPair<IItemList<IAEItemStack>>> cellItemsManager = new Object2ObjectOpenHashMap<>();
    private static boolean canSave;
    private static File cellsFlie;

    private StorageHandler() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    private static File getCellsFlie() {
        if (cellsFlie == null) {
            var path = DimensionManager.getWorld(0)
                .getSaveHandler()
                .getWorldDirectory()
                .toPath()
                .resolve(AppliedTiC.MODID)
                .resolve("tic_cells");
            try {
                Files.createDirectories(path);
            } catch (IOException ignored) {

            }
            cellsFlie = path.toFile();
        }

        return cellsFlie;
    }

    public static IItemList<IAEItemStack> loadList(String uuid) {
        var cellItemsPair = cellItemsManager.get(uuid);
        if (cellItemsPair == null) {
            synchronized (cellItemsManager) {
                if ((cellItemsPair = cellItemsManager.get(uuid)) == null) {
                    cellItemsManager.put(
                        uuid,
                        cellItemsPair = BooleanObjectMutablePair.of(
                            false,
                            AEApi.instance()
                                .storage()
                                .createPrimitiveItemList()));
                }
            }
        }
        final var cellItems = cellItemsPair.right();

        if (!saveCell(uuid, cellItemsPair)) {
            synchronized (cellItemsPair) {
                cellItems.resetStatus();
                try {
                    var data = CompressedStreamTools.read(new File(getCellsFlie(), uuid + ".dat"));
                    if (data != null) {
                        var list = data.getTagList("STORES", Constants.NBT.TAG_COMPOUND);

                        for (var i = 0; i < list.tagCount(); i++) {
                            var tag = list.getCompoundTagAt(i);
                            cellItems.addStorage(AEItemStack.loadItemStackFromNBT(tag));
                        }
                    }
                } catch (IOException ignored) {

                }
            }
        }

        return cellItems;
    }

    public static void saveList(String uuid) {
        cellItemsManager.get(uuid)
            .left(true);
        canSave = true;
    }

    private static boolean saveCell(String uuid, BooleanObjectPair<IItemList<IAEItemStack>> pair) {
        synchronized (pair) {
            if (pair.leftBoolean()) {
                pair.left(false);
                NBTTagCompound store = new NBTTagCompound();
                NBTTagList itemNbt = new NBTTagList();
                for (IAEItemStack aeItem : pair.right()) {
                    NBTTagCompound nbt = new NBTTagCompound();
                    aeItem.writeToNBT(nbt);
                    itemNbt.appendTag(nbt);
                }
                store.setTag("STORES", itemNbt);
                try {
                    CompressedStreamTools.safeWrite(store, new File(getCellsFlie(), uuid + ".dat"));
                    return true;
                } catch (IOException ignored) {

                }
            }
            return false;
        }
    }

    public static void saveAllCells() {
        synchronized (cellItemsManager) {
            if (!canSave) return;
            canSave = false;
            for (var entry : cellItemsManager.object2ObjectEntrySet()) {
                saveCell(entry.getKey(), entry.getValue());
            }
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (event.world.isRemote) return;
        saveAllCells();
    }
}
