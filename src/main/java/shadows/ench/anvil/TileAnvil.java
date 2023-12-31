package shadows.ench.anvil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileAnvil extends TileEntity {

	int unbreaking = 0;
	int splitting = 0;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setInteger("ub", unbreaking);
		tag.setInteger("splitting", splitting);
		return super.writeToNBT(tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		unbreaking = tag.getInteger("ub");
		splitting = tag.getInteger("splitting");
	}

	public void setUnbreaking(int level) {
		unbreaking = level;
	}

	public int getUnbreaking() {
		return unbreaking;
	}

	public void setSplitting(int level) {
		splitting = level;
	}

	public int getSplitting() {
		return splitting;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

}
