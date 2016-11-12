package com.raoulvdberge.refinedstorage.block;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSBlocks;
import com.raoulvdberge.refinedstorage.RSGui;
import com.raoulvdberge.refinedstorage.tile.TileReader;
import mcmultipart.microblock.MicroblockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockReader extends BlockCable {
    public BlockReader() {
        super("reader");
    }

    @Override
    public List<AxisAlignedBB> getNonUnionizedCollisionBoxes(IBlockState state) {
        return RSBlocks.CONSTRUCTOR.getNonUnionizedCollisionBoxes(state);
    }

    @Override
    public boolean onBlockActivatedDefault(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hitCablePart(state, world, pos, hitX, hitY, hitZ)) {
            return false;
        }

        if (!world.isRemote) {
            player.openGui(RS.INSTANCE, RSGui.READER_WRITER, world, pos.getX(), pos.getY(), pos.getZ());

            ((TileReader) world.getTileEntity(pos)).onOpened(player);
        }

        return true;
    }

    @Override
    public boolean canConnectRedstoneDefault(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side, MicroblockContainer partContainer) {
        return side == ((TileReader) world.getTileEntity(pos)).getDirection().getOpposite();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileReader();
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    public EnumPlacementType getPlacementType() {
        return EnumPlacementType.ANY_FACE_PLAYER;
    }
}
