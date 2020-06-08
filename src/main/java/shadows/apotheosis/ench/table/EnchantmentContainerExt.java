package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.SlotItemHandler;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.util.FloatReferenceHolder;

public class EnchantmentContainerExt extends EnchantmentContainer {

	protected IWorldPosCallable wPos = super.field_217006_g;

	protected FloatReferenceHolder eterna = new FloatReferenceHolder(0F, 0, 40);
	protected FloatReferenceHolder quanta = new FloatReferenceHolder(0F, 0, 10);
	protected FloatReferenceHolder arcana = new FloatReferenceHolder(0F, 0, 10);

	public EnchantmentContainerExt(int id, PlayerInventory inv) {
		super(id, inv, IWorldPosCallable.DUMMY);
		this.inventorySlots.clear();
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return true;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new Slot(this.tableInventory, 1, 35, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(p_75214_1_.getItem());
			}
		});
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 31));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inv, k, 8 + k * 18, 142 + 31));
		}
		this.trackIntArray(eterna.getArray());
		this.trackIntArray(quanta.getArray());
		this.trackIntArray(arcana.getArray());
	}

	public EnchantmentContainerExt(int id, PlayerInventory inv, IWorldPosCallable wPos, EnchantingTableTileEntityExt te) {
		super(id, inv, wPos);
		this.inventorySlots.clear();
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return true;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new SlotItemHandler(te.inv, 0, 35, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(p_75214_1_.getItem());
			}
		});
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 31));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inv, k, 8 + k * 18, 142 + 31));
		}
		this.trackIntArray(eterna.getArray());
		this.trackIntArray(quanta.getArray());
		this.trackIntArray(arcana.getArray());
	}

	@Override
	public boolean enchantItem(PlayerEntity player, int id) {
		int level = enchantLevels[id];
		ItemStack toEnchant = this.tableInventory.getStackInSlot(0);
		ItemStack lapis = this.getSlot(1).getStack();
		int i = id + 1;
		if ((lapis.isEmpty() || lapis.getCount() < i) && !player.abilities.isCreativeMode) return false;

		if (this.enchantLevels[id] <= 0 || toEnchant.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.enchantLevels[id]) && !player.abilities.isCreativeMode) return false;

		this.wPos.consume((world, pos) -> {
			ItemStack enchanted = toEnchant;
			List<EnchantmentData> list = this.getEnchantmentList(toEnchant, id, this.enchantLevels[id], quanta.get(), arcana.get());
			if (!list.isEmpty()) {
				player.onEnchant(toEnchant, i);
				boolean flag = toEnchant.getItem() == Items.BOOK;
				if (flag) {
					enchanted = new ItemStack(Items.ENCHANTED_BOOK);
					this.tableInventory.setInventorySlotContents(0, enchanted);
				}

				for (int j = 0; j < list.size(); ++j) {
					EnchantmentData enchantmentdata = list.get(j);
					if (flag) {
						EnchantedBookItem.addEnchantment(enchanted, enchantmentdata);
					} else {
						enchanted.addEnchantment(enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
					}
				}

				if (!player.abilities.isCreativeMode) {
					lapis.shrink(i);
					if (lapis.isEmpty()) {
						this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
					}
				}

				player.addStat(Stats.ENCHANT_ITEM);
				if (player instanceof ServerPlayerEntity) {
					CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, enchanted, i);
					AdvancementTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, this.tableInventory.getStackInSlot(0), level);

				}

				this.tableInventory.markDirty();
				this.xpSeed.set(player.getXPSeed());
				this.onCraftMatrixChanged(this.tableInventory);
				world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
			}

		});
		return true;

	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		wPos.apply((world, pos) -> {
			if (inventoryIn == this.tableInventory) {
				ItemStack itemstack = inventoryIn.getStackInSlot(0);
				if (itemstack.getCount() == 1 && itemstack.isEnchantable()) {
					this.quanta.set(2.25F);
					float power = getEnchPower();
					this.rand.setSeed(this.xpSeed.get());

					for (int num = 0; num < 3; ++num) {
						this.enchantLevels[num] = RealEnchantmentHelper.calcItemStackEnchantability(this.rand, num, (int) power, itemstack);
						this.enchantClue[num] = -1;
						this.worldClue[num] = -1;

						if (this.enchantLevels[num] < num + 1) {
							this.enchantLevels[num] = 0;
						}
						this.enchantLevels[num] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, num, (int) power, itemstack, enchantLevels[num]);
					}

					for (int j1 = 0; j1 < 3; ++j1) {
						if (this.enchantLevels[j1] > 0) {
							List<EnchantmentData> list = this.getEnchantmentList(itemstack, j1, this.enchantLevels[j1], this.quanta.get(), this.arcana.get());

							if (list != null && !list.isEmpty()) {
								EnchantmentData enchantmentdata = list.get(this.rand.nextInt(list.size()));
								this.enchantClue[j1] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
								this.worldClue[j1] = enchantmentdata.enchantmentLevel;
							}
						}
					}

					this.detectAndSendChanges();
				} else {
					for (int i = 0; i < 3; ++i) {
						this.enchantLevels[i] = 0;
						this.enchantClue[i] = -1;
						this.worldClue[i] = -1;
						this.eterna.set(0);
						this.quanta.set(0);
						this.arcana.set(0);
					}
				}
			}
			return this;
		});
	}

	private List<EnchantmentData> getEnchantmentList(ItemStack stack, int enchantSlot, int level, float quanta, float arcana) {
		this.rand.setSeed(this.xpSeed.get() + enchantSlot);
		List<EnchantmentData> list = RealEnchantmentHelper.buildEnchantmentList(this.rand, stack, level, quanta, arcana, false);
		return list;
	}

	public float getEnchPower() {
		return wPos.apply((world, pos) -> {

			Int2FloatMap powers = new Int2FloatOpenHashMap();

			for (int j = -1; j <= 1; ++j) {
				for (int k = -1; k <= 1; ++k) {
					if ((j != 0 || k != 0) && world.isAirBlock(pos.add(k, 0, j)) && world.isAirBlock(pos.add(k, 1, j))) {
						gatherStats(powers, world, pos.add(k * 2, 0, j * 2));
						gatherStats(powers, world, pos.add(k * 2, 1, j * 2));
						if (k != 0 && j != 0) {
							gatherStats(powers, world, pos.add(k * 2, 0, j));
							gatherStats(powers, world, pos.add(k * 2, 1, j));
							gatherStats(powers, world, pos.add(k, 0, j * 2));
							gatherStats(powers, world, pos.add(k, 1, j * 2));
						}
					}
				}
			}
			float power = 0;
			List<Int2FloatMap.Entry> entries = new ArrayList<>(powers.int2FloatEntrySet());
			Collections.sort(entries, Comparator.comparingInt(Int2FloatMap.Entry::getIntKey));
			for (Int2FloatMap.Entry e : entries) {
				power = Math.min(e.getIntKey(), power + e.getFloatValue());
			}
			this.eterna.set(power);
			return power;
		}).orElse(0F);
	}

	public void gatherStats(Int2FloatMap powers, World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		int max = EnchantmentStatRegistry.getMaxEterna(state, world, pos);
		float power = EnchantmentStatRegistry.getEterna(state, world, pos);
		powers.put(max, powers.getOrDefault(max, 0) + power);
		float quanta = EnchantmentStatRegistry.getQuanta(state, world, pos);
		this.quanta.set(this.quanta.get() + quanta);
		float arcana = EnchantmentStatRegistry.getArcana(state, world, pos);
		this.arcana.set(this.arcana.get() + arcana);
	}

	@Override
	public ContainerType<?> getType() {
		return ApotheosisObjects.ENCHANTING;
	}

	/**
	 * Arcana Tiers, each represents a new rarity set.
	 */
	public static enum Arcana {
		EMPTY(0F, 10, 5, 2, 1),
		LITTLE(1F, 8, 5, 3, 1),
		FEW(2F, 7, 5, 4, 2),
		SOME(3F, 5, 5, 4, 2),
		LESS(4F, 5, 5, 4, 3),
		MEDIUM(5F, 5, 5, 5, 5),
		MORE(6F, 3, 4, 5, 5),
		VALUE(7F, 2, 4, 5, 5),
		EXTRA(8F, 2, 4, 5, 7),
		ALMOST(9F, 1, 3, 5, 8),
		MAX(9.9F, 1, 2, 5, 10);

		final float threshold;
		final int[] rarities;

		Arcana(float threshold, int... rarities) {
			this.threshold = threshold;
			this.rarities = rarities;
		}

		static Arcana[] VALUES = values();

		public int[] getRarities() {
			return this.rarities;
		}

		public static Arcana getForThreshold(float threshold) {
			for (int i = VALUES.length - 1; i >= 0; i--) {
				if (threshold > VALUES[i].threshold) return VALUES[i];
			}
			return EMPTY;
		}

	}
}
