package com.circulation.applied_tic.tools;

import static appeng.me.storage.CellInventory.getCell;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.circulation.applied_tic.handler.StorageHandler;
import com.circulation.applied_tic.registry.ItemRegistry;
import com.circulation.applied_tic.utils.TiCCellHandler;
import com.mojang.authlib.GameProfile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.IterationCounter;
import appeng.util.Platform;
import appeng.util.item.ItemList;
import appeng.util.prioitylist.FuzzyPriorityList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import lombok.Getter;
import tconstruct.library.tools.ToolCore;

public class TiCStorageCell extends ToolCore implements IStorageCell {

    public static long BYTES_PER_DURABILITY = 1024L;
    public static final TiCStorageCell INSTANCE = new TiCStorageCell();
    private static final Handler handler = new Handler();
    @Getter(lazy = true)
    private static final FakePlayer fakePlayer = FakePlayerFactory.get(
        DimensionManager.getWorld(0),
        new GameProfile(UUID.fromString("CC1F4976-9C89-4AD4-BFA1-AD167F9B2D4F"), "[AppliedTiCCell]"));

    public TiCStorageCell() {
        super(1000);
    }

    public static TiCCellHandler getCellHandler() {
        return handler;
    }

    @Override
    public void getSubItems(Item id, CreativeTabs tab, List<ItemStack> list) {
        super.getSubItems(id, tab, list);
    }

    @Override
    public int getPartAmount() {
        return 2;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, Block block, int x, int y, int z,
        EntityLivingBase player) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> lines, boolean advanced) {
        final var inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(stack, null, StorageChannel.ITEMS);

        if (!(inventory instanceof InventoryHandler cellHandler)) {
            return;
        }

        final var cellInventory = cellHandler.inventory;

        lines.add(
            EnumChatFormatting.WHITE + NumberFormat.getInstance(Locale.ENGLISH)
                .format(cellInventory.getUsedBytes())
                + EnumChatFormatting.GRAY
                + " "
                + GuiText.Of.getLocal()
                + " "
                + EnumChatFormatting.DARK_GREEN
                + NumberFormat.getInstance()
                    .format(cellInventory.getBytesLong())
                + " "
                + EnumChatFormatting.GRAY
                + GuiText.BytesUsed.getLocal());

        lines.add(
            EnumChatFormatting.WHITE + NumberFormat.getInstance(Locale.ENGLISH)
                .format(cellInventory.getStoredItemTypes())
                + EnumChatFormatting.GRAY
                + " "
                + GuiText.Of.getLocal()
                + " "
                + EnumChatFormatting.DARK_GREEN
                + NumberFormat.getInstance()
                    .format(cellInventory.getTotalBytes())
                + " "
                + EnumChatFormatting.GRAY
                + GuiText.Types.getLocal());

        if (cellInventory.getTotalItemTypes() == 1 && cellInventory.getStoredItemTypes() != 0) {
            ItemStack itemStack = cellInventory.getAvailableItems(new ItemList(), IterationCounter.fetchNewId())
                .getFirstItem()
                .getItemStack();
            lines.add(GuiText.Contains.getLocal() + ": " + itemStack.getDisplayName());
        }

        if (cellHandler.isPreformatted()) {
            String filter = getOreFilter(stack);
            if (filter.isEmpty()) {
                final String list = (cellHandler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                    : GuiText.Excluded).getLocal();

                if (cellHandler.isFuzzy()) {
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
                } else {
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                }
                if (GuiScreen.isShiftKeyDown()) {
                    int usedFilters = 0;
                    ArrayList<String> filtersTexts = new ArrayList<>();
                    for (int i = 0; i < cellInventory.getConfigInventory()
                        .getSizeInventory(); ++i) {
                        ItemStack s = cellInventory.getConfigInventory()
                            .getStackInSlot(i);
                        if (s != null) {
                            usedFilters++;
                            filtersTexts.add(s.getDisplayName());
                        }
                    }
                    lines.add(
                        GuiText.Filter.getLocal() + " ("
                            + usedFilters
                            + "/"
                            + cellInventory.getConfigInventory()
                                .getSizeInventory()
                            + ")"
                            + ": ");

                    if (!filtersTexts.isEmpty()) {
                        lines.addAll(filtersTexts);
                    }

                }
            } else {
                lines.add(GuiText.PartitionedOre.getLocal() + " : " + filter);
            }

            if (cellHandler.getSticky()) {
                lines.add(GuiText.Sticky.getLocal());
            }
        }
        if (cellInventory.restrictionLong != 0 || cellInventory.restrictionTypes != 0) {
            lines.add(GuiText.Restricted.getLocal());
            if (GuiScreen.isShiftKeyDown()) {
                NumberFormat nf = NumberFormat.getNumberInstance();
                if (cellInventory.restrictionLong != 0)
                    lines.add(GuiText.MaxItems.getLocal() + " " + nf.format(cellInventory.restrictionLong));
                if (cellInventory.restrictionTypes != 0)
                    lines.add(GuiText.MaxTypes.getLocal() + " " + cellInventory.restrictionTypes);
            }
        }
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("uuid")) {
            lines.add(
                EnumChatFormatting.GRAY + "UUID: "
                    + stack.getTagCompound()
                        .getString("uuid"));
        }
    }

    // TiC
    @Override
    public String getIconSuffix(int partType) {
        return switch (partType) {
            case 0 -> "_me_cell_housing";
            case 2 -> "_me_cell_core";
            default -> "";
        };
    }

    @Override
    public String getEffectSuffix() {
        return "_me_cell_effect";
    }

    @Override
    public String getDefaultFolder() {
        return "me_cell";
    }

    @Override
    public Item getHeadItem() {
        return ItemRegistry.housing;
    }

    @Override
    public Item getAccessoryItem() {
        return null;
    }

    @Override
    public Item getHandleItem() {
        return ItemRegistry.core;
    }

    @Override
    public String[] getTraits() {
        return new String[] { "cell" };
    }

    // AE
    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 5);
    }

    @Override
    public IInventory getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        return FuzzyMode.fromItemStack(is);
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is)
            .setString("FuzzyMode", fzMode.name());
    }

    @Override
    public String getOreFilter(ItemStack is) {
        return Platform.openNbtData(is)
            .getString("OreFilter");
    }

    @Override
    public void setOreFilter(ItemStack is, String filter) {
        Platform.openNbtData(is)
            .setString("OreFilter", filter);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return 0;
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        return Platform.openNbtData(cellItem)
            .getCompoundTag("InfiTool")
            .getInteger("TotalDurability") * BYTES_PER_DURABILITY;
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return getBytesPerType(cellItem);
    }

    // TODO:类型占用字节
    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 0;
    }

    // TODO:最大类型？
    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return damageVsEntity;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    // TODO:单元能耗
    @Override
    public double getIdleDrain(@Nullable ItemStack i) {
        return IStorageCell.super.getIdleDrain(i);
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    private final static class Handler extends TiCCellHandler {

        private Handler() {
            super(TiCStorageCell.INSTANCE);
        }

        @Override
        public InventoryHandler getCellInventory(ItemStack is, ISaveProvider host, StorageChannel channel) {
            if (channel == StorageChannel.ITEMS) {
                return new InventoryHandler(new Inventory(is), channel);
            }
            return null;
        }

        @Override
        public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler,
            IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
            Platform.openGUI(player, (TileEntity) chest, chest.getUp(), GuiBridge.GUI_ME);
        }
    }

    private final static class InventoryHandler extends MEInventoryHandler<IAEItemStack>
        implements ICellInventoryHandler {

        private final Inventory inventory;

        public InventoryHandler(Inventory i, StorageChannel channel) {
            super(i, channel);
            this.inventory = i;
        }

        @Override
        public ICellInventory getCellInv() {
            return inventory;
        }

        @Override
        public boolean isPreformatted() {
            return !this.getPartitionList()
                .isEmpty();
        }

        @Override
        public boolean isFuzzy() {
            return this.getPartitionList() instanceof FuzzyPriorityList;
        }

        @Override
        public IncludeExclude getIncludeExcludeMode() {
            return this.getWhitelist();
        }
    }

    @Getter
    private final static class Inventory implements IMEInventory<IAEItemStack>, ICellInventory {

        private static final String ITEM_TYPE_TAG = "it";
        private static final String ITEM_COUNT_TAG = "ic";
        private final ItemStack cellItem;
        private final NBTTagCompound tagCompound;
        private final byte restrictionTypes;
        private final long restrictionLong;
        private IItemList<IAEItemStack> cellItems;
        private long storedItemTypes;
        private long storedItemCount;
        private boolean cardVoidOverflow, cardDistribution;

        private Inventory(ItemStack cellItem) {
            this.cellItem = cellItem;
            this.tagCompound = Platform.openNbtData(cellItem);
            this.storedItemTypes = this.tagCompound.getShort(ITEM_TYPE_TAG);
            this.storedItemCount = this.tagCompound.getLong(ITEM_COUNT_TAG);
            this.restrictionTypes = this.tagCompound.getByte("cellRestrictionTypes");
            this.restrictionLong = this.tagCompound.getLong("cellRestrictionAmount");

            final IInventory upgrades = TiCStorageCell.INSTANCE.getUpgradesInventory(cellItem);
            for (int x = 0; x < upgrades.getSizeInventory(); x++) {
                final ItemStack is = upgrades.getStackInSlot(x);
                if (is != null && is.getItem() instanceof IUpgradeModule) {
                    final Upgrades u = ((IUpgradeModule) is.getItem()).getType(is);
                    if (u != null) {
                        switch (u) {
                            case VOID_OVERFLOW -> cardVoidOverflow = true;
                            case DISTRIBUTION -> cardDistribution = true;
                        }
                    }
                }
            }
        }

        private static boolean isStorageCell(final IAEItemStack itemStack) {
            if (itemStack == null) {
                return false;
            }

            try {
                final Item type = itemStack.getItem();

                if (type instanceof IStorageCell c) {
                    return !c.storableInStorageCell();
                }
            } catch (final Throwable err) {
                return true;
            }

            return false;
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out, int iteration) {
            for (final IAEItemStack i : this.getCellItems()) {
                out.add(i);
            }

            return out;
        }

        // TODO:每多少物品占用的字节数目
        private long getItemByteConsumption() {
            return 8;
        }

        public long getTotalBytes() {
            return TiCStorageCell.INSTANCE.getTotalTypes(cellItem);
        }

        public long getTotalItemTypes() {
            if (restrictionTypes > 0) return restrictionTypes;
            return this.getTotalBytes();
        }

        @Override
        public ItemStack getItemStack() {
            return cellItem;
        }

        @Override
        public double getIdleDrain() {
            return INSTANCE.getIdleDrain(cellItem);
        }

        @Override
        public FuzzyMode getFuzzyMode() {
            return INSTANCE.getFuzzyMode(cellItem);
        }

        @Override
        public IInventory getConfigInventory() {
            return INSTANCE.getConfigInventory(cellItem);
        }

        @Override
        public IInventory getUpgradesInventory() {
            return INSTANCE.getUpgradesInventory(cellItem);
        }

        public int getBytesPerType() {
            return TiCStorageCell.INSTANCE.getBytesPerType(cellItem);
        }

        public long getBytesLong() {
            return TiCStorageCell.INSTANCE.getBytesLong(cellItem);
        }

        public long getRemainingItemsCountDist(IAEItemStack l) {
            long remaining;
            long types = 0;
            for (int i = 0; i < this.getTotalItemTypes(); i++) {
                if (TiCStorageCell.INSTANCE.getConfigInventory(cellItem)
                    .getStackInSlot(i) != null) {
                    types++;
                }
            }
            if (types == 0) types = this.getTotalItemTypes();
            if (l != null) {
                if (restrictionLong > 0) {
                    remaining = Math.min((restrictionLong / types) - l.getStackSize(), getRemainingItemCount());
                } else {
                    remaining = (((getTotalBytes() / types) - getBytesPerType()) * getItemByteConsumption())
                        - l.getStackSize();
                }
            } else {
                if (restrictionLong > 0) {
                    remaining = Math.min(
                        restrictionLong / types,
                        ((this.getTotalBytes() / types) - this.getBytesPerType()) * getItemByteConsumption());
                } else {
                    remaining = ((this.getTotalBytes() / types) - this.getBytesPerType()) * getItemByteConsumption();
                }
            }
            return remaining > 0 ? remaining : 0;
        }

        public int getUnusedItemCount() {
            final long div = this.getStoredItemCount() % getItemByteConsumption();

            if (div == 0) {
                return 0;
            }

            return (int) (getItemByteConsumption() - div);
        }

        @Override
        public int getStatusForCell() {
            return 0;
        }

        @Override
        public String getOreFilter() {
            return "";
        }

        public long getFreeBytes() {
            return this.getTotalBytes() - this.getUsedBytes();
        }

        public long getUsedBytes() {
            final long bytesForItemCount = (this.getStoredItemCount() + this.getUnusedItemCount())
                / getItemByteConsumption();

            return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
        }

        public long getRemainingItemCount() {
            if (restrictionLong > 0) {
                return Math.min(
                    restrictionLong - this.getStoredItemCount(),
                    this.getFreeBytes() * getItemByteConsumption() + this.getUnusedItemCount());
            }
            final long remaining = this.getFreeBytes() * getItemByteConsumption() + this.getUnusedItemCount();

            return remaining > 0 ? remaining : 0;
        }

        private IItemList<IAEItemStack> getCellItems() {
            if (this.cellItems == null) {
                this.loadCellItems();
            }

            return this.cellItems;
        }

        private void loadCellItems() {
            if (this.cellItems == null) {
                if (!tagCompound.hasKey("uuid")) {
                    tagCompound.setString(
                        "uuid",
                        UUID.randomUUID()
                            .toString());
                }
                this.cellItems = StorageHandler.loadList(tagCompound.getString("uuid"));
            }

            if (this.cellItems.size() != storedItemTypes) {
                // fix broken singularity cells
                this.saveChanges();
            }
        }

        private void saveChanges() {

            storedItemCount = 0;

            for (final IAEItemStack v : this.cellItems) {
                storedItemCount += v.getStackSize();
            }

            this.storedItemTypes = (short) this.cellItems.size();

            if (this.cellItems.isEmpty()) {
                this.tagCompound.removeTag(ITEM_TYPE_TAG);
            } else {
                this.tagCompound.setLong(ITEM_TYPE_TAG, this.storedItemTypes);
            }

            if (storedItemCount == 0) {
                this.tagCompound.removeTag(ITEM_COUNT_TAG);
            } else {
                this.tagCompound.setLong(ITEM_COUNT_TAG, storedItemCount);
            }

            if (!tagCompound.hasKey("uuid")) {
                tagCompound.setString(
                    "uuid",
                    UUID.randomUUID()
                        .toString());
            }

            StorageHandler.saveList(tagCompound.getString("uuid"));
        }

        public long getRemainingItemTypes() {
            final long basedOnStorage = this.getBytesPerType() == 0 ? Integer.MAX_VALUE
                : this.getFreeBytes() / this.getBytesPerType();
            final long baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();

            return Math.min(basedOnStorage, baseOnTotal);
        }

        private void updateItemCount(final long delta) {
            this.storedItemCount += delta;
            this.tagCompound.setLong(ITEM_COUNT_TAG, this.storedItemCount);
        }

        private boolean isEmpty(final IMEInventory<IAEItemStack> meInventory) {
            return meInventory.getAvailableItems(
                AEApi.instance()
                    .storage()
                    .createItemList(),
                IterationCounter.fetchNewId())
                .isEmpty();
        }

        public boolean canHoldNewItem() {
            final long bytesFree = this.getFreeBytes();

            return (bytesFree > this.getBytesPerType()
                || (bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0))
                && (restrictionLong <= 0 || restrictionLong > getStoredItemCount())
                && this.getRemainingItemTypes() > 0;
        }

        @Override
        public IAEItemStack injectItems(final IAEItemStack input, final Actionable mode, final BaseActionSource src) {
            if (input == null) {
                return null;
            }

            if (input.getStackSize() == 0) {
                return null;
            }

            if (isStorageCell(input)) {
                final IMEInventory<IAEItemStack> meInventory = getCell(input.getItemStack(), null);

                if (meInventory != null && !this.isEmpty(meInventory)) {
                    return input;
                }
            }

            if (mode == Actionable.MODULATE && input.isCraftable()) {
                input.setCraftable(false);
            }

            final IAEItemStack l = this.getCellItems()
                .findPrecise(input);

            if (l != null) {
                long remainingItemSlots;
                if (cardDistribution) {
                    remainingItemSlots = this.getRemainingItemsCountDist(l);
                } else {
                    remainingItemSlots = this.getRemainingItemCount();
                }

                if (remainingItemSlots <= 0) {
                    if (cardVoidOverflow) {
                        return null;
                    }
                    return input;
                }

                if (input.getStackSize() > remainingItemSlots) {
                    final IAEItemStack r = input.copy();
                    r.setStackSize(r.getStackSize() - remainingItemSlots);

                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(l.getStackSize() + remainingItemSlots);
                        this.updateItemCount(remainingItemSlots);
                        this.addXP(remainingItemSlots);
                        this.saveChanges();
                    }

                    return r;
                } else {
                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(l.getStackSize() + input.getStackSize());
                        this.updateItemCount(input.getStackSize());
                        this.addXP(input.getStackSize());
                        this.saveChanges();
                    }

                    return null;
                }
            }

            if (this.canHoldNewItem()) // room for new type, and for at least one item!
            {
                long remainingItemCount;
                if (cardDistribution) {
                    remainingItemCount = this.getRemainingItemsCountDist(null);
                } else {
                    if (restrictionLong > 0) {
                        remainingItemCount = this.getRemainingItemCount();
                    } else {
                        remainingItemCount = this.getRemainingItemCount()
                            - (long) this.getBytesPerType() * getItemByteConsumption();
                    }
                }

                if (remainingItemCount > 0) {
                    if (input.getStackSize() > remainingItemCount) {
                        final IAEItemStack toReturn = input.copy();
                        toReturn.decStackSize(remainingItemCount);

                        if (mode == Actionable.MODULATE) {
                            final IAEItemStack toWrite = input.copy();
                            toWrite.setStackSize(remainingItemCount);

                            this.cellItems.add(toWrite);
                            this.updateItemCount(toWrite.getStackSize());
                            this.addXP(remainingItemCount);
                            this.saveChanges();
                        }
                        return toReturn;
                    }

                    if (mode == Actionable.MODULATE) {
                        this.updateItemCount(input.getStackSize());
                        this.cellItems.add(input);
                        this.addXP(input.getStackSize());
                        this.saveChanges();
                    }

                    return null;
                }
            }

            return input;
        }

        @Override
        public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode,
            final BaseActionSource src) {
            if (request == null) {
                return null;
            }

            final long size = request.getStackSize();

            IAEItemStack results = null;

            final IAEItemStack l = this.getCellItems()
                .findPrecise(request);

            if (l != null) {
                results = l.copy();

                if (l.getStackSize() <= size) {
                    results.setStackSize(l.getStackSize());

                    if (mode == Actionable.MODULATE) {
                        this.updateItemCount(-l.getStackSize());
                        this.addXP(l.getStackSize());
                        l.setStackSize(0);
                        this.saveChanges();
                    }
                } else {
                    results.setStackSize(size);

                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(l.getStackSize() - size);
                        this.updateItemCount(-size);
                        this.addXP(size);
                        this.saveChanges();
                    }
                }
            }

            return results;
        }

        @Override
        public StorageChannel getChannel() {
            return StorageChannel.ITEMS;
        }

        private long size = 0;

        private void addXP(long stackSize) {
            size += stackSize;
            if (size / 8 > 0) LevelingLogic.addXP(cellItem, getFakePlayer(), size / 8);
            size %= 8;
        }
    }
}
