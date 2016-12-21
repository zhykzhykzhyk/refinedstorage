package com.raoulvdberge.refinedstorage.apiimpl.network.node;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBasic;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerChangeListenerNode;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerInterface;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.INetworkNodeHolder;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class NetworkNodeInterface extends NetworkNode implements IComparable {
    private static final String NBT_COMPARE = "Compare";

    private ItemHandlerBasic importItems = new ItemHandlerBasic(9, new ItemHandlerChangeListenerNode(this));

    private ItemHandlerBasic exportSpecimenItems = new ItemHandlerBasic(9, new ItemHandlerChangeListenerNode(this));
    private ItemHandlerBasic exportItems = new ItemHandlerBasic(9, new ItemHandlerChangeListenerNode(this));

    private ItemHandlerInterface items = new ItemHandlerInterface(importItems, exportItems);

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, new ItemHandlerChangeListenerNode(this), ItemUpgrade.TYPE_SPEED, ItemUpgrade.TYPE_STACK, ItemUpgrade.TYPE_CRAFTING);

    private int compare = IComparer.COMPARE_NBT | IComparer.COMPARE_DAMAGE;

    private int currentSlot = 0;

    public NetworkNodeInterface(INetworkNodeHolder holder) {
        super(holder);
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.interfaceUsage + upgrades.getEnergyUsage();
    }

    @Override
    public void update() {
        if (network == null) {
            return;
        }

        if (currentSlot >= importItems.getSlots()) {
            currentSlot = 0;
        }

        ItemStack slot = importItems.getStackInSlot(currentSlot);

        if (slot.isEmpty()) {
            currentSlot++;
        } else if (ticks % upgrades.getSpeed() == 0) {
            int size = Math.min(slot.getCount(), upgrades.getItemInteractCount());

            ItemStack remainder = network.insertItem(slot, size, false);

            if (remainder == null) {
                importItems.extractItemInternal(currentSlot, size, false);
            } else {
                importItems.extractItemInternal(currentSlot, size - remainder.getCount(), false);

                currentSlot++;
            }
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack wanted = exportSpecimenItems.getStackInSlot(i);
            ItemStack got = exportItems.getStackInSlot(i);

            if (wanted.isEmpty()) {
                if (!got.isEmpty()) {
                    exportItems.setStackInSlot(i, RSUtils.getStack(network.insertItem(got, got.getCount(), false)));
                }
            } else {
                int delta = got.isEmpty() ? wanted.getCount() : (wanted.getCount() - got.getCount());

                if (delta > 0) {
                    ItemStack result = network.extractItem(wanted, delta, compare, false);

                    if (result != null) {
                        if (exportItems.getStackInSlot(i).isEmpty()) {
                            exportItems.setStackInSlot(i, result);
                        } else {
                            exportItems.getStackInSlot(i).grow(result.getCount());
                        }
                    } else if (upgrades.hasUpgrade(ItemUpgrade.TYPE_CRAFTING)) {
                        network.scheduleCraftingTask(wanted, delta, compare);
                    }
                } else if (delta < 0) {
                    ItemStack remainder = network.insertItem(got, Math.abs(delta), false);

                    if (remainder == null) {
                        exportItems.extractItem(i, Math.abs(delta), false);
                    } else {
                        exportItems.extractItem(i, Math.abs(delta) - remainder.getCount(), false);
                    }
                }
            }
        }
    }

    @Override
    public int getCompare() {
        return compare;
    }

    @Override
    public void setCompare(int compare) {
        this.compare = compare;

        markDirty();
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(importItems, 0, tag);
        RSUtils.readItems(exportItems, 2, tag);
        RSUtils.readItems(upgrades, 3, tag);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(importItems, 0, tag);
        RSUtils.writeItems(exportItems, 2, tag);
        RSUtils.writeItems(upgrades, 3, tag);

        return tag;
    }

    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        super.writeConfiguration(tag);

        RSUtils.writeItems(exportSpecimenItems, 1, tag);

        tag.setInteger(NBT_COMPARE, compare);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        super.readConfiguration(tag);

        RSUtils.readItems(exportSpecimenItems, 1, tag);

        if (tag.hasKey(NBT_COMPARE)) {
            compare = tag.getInteger(NBT_COMPARE);
        }
    }

    public IItemHandler getImportItems() {
        return importItems;
    }

    public IItemHandler getExportSpecimenItems() {
        return exportSpecimenItems;
    }

    public IItemHandler getExportItems() {
        return exportItems;
    }

    public ItemHandlerInterface getItems() {
        return items;
    }

    public IItemHandler getUpgrades() {
        return upgrades;
    }

    @Override
    public IItemHandler getDrops() {
        return new CombinedInvWrapper(importItems, exportItems, upgrades);
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }
}
