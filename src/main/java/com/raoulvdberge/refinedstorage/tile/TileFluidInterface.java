package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNodeFluidInterface;
import com.raoulvdberge.refinedstorage.tile.config.IComparable;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.RSSerializers;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class TileFluidInterface extends TileNode {
    public static final TileDataParameter<Integer> COMPARE = IComparable.createParameter();

    public static final TileDataParameter<FluidStack> TANK_IN = new TileDataParameter<>(RSSerializers.FLUID_STACK_SERIALIZER, null, new ITileDataProducer<FluidStack, TileFluidInterface>() {
        @Override
        public FluidStack getValue(TileFluidInterface tile) {
            return ((NetworkNodeFluidInterface) tile.getNode()).getTankIn().getFluid();
        }
    });

    public static final TileDataParameter<FluidStack> TANK_OUT = new TileDataParameter<>(RSSerializers.FLUID_STACK_SERIALIZER, null, new ITileDataProducer<FluidStack, TileFluidInterface>() {
        @Override
        public FluidStack getValue(TileFluidInterface tile) {
            return ((NetworkNodeFluidInterface) tile.getNode()).getTankOut().getFluid();
        }
    });

    public TileFluidInterface() {
        dataManager.addWatchedParameter(COMPARE);
        dataManager.addParameter(TANK_IN);
        dataManager.addParameter(TANK_OUT);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(facing == EnumFacing.DOWN ? ((NetworkNodeFluidInterface) getNode()).getTankOut() : ((NetworkNodeFluidInterface) getNode()).getTankIn());
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public INetworkNode createNode() {
        return new NetworkNodeFluidInterface(this);
    }
}
