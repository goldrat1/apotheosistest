package shadows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import shadows.deadly.DeadlyModule;
import shadows.deadly.cmd.LootCommand;
import shadows.deadly.loot.affix.Affix;
import shadows.ench.EnchModule;
import shadows.garden.GardenModule;
import shadows.placebo.util.RecipeHelper;
import shadows.potion.PotionModule;
import shadows.spawn.SpawnerModule;
import shadows.util.NBTIngredient;
import shadows.util.ParticleMessage;

@Mod(modid = Apotheosis.MODID, name = Apotheosis.MODNAME, version = Apotheosis.VERSION, dependencies = "required-after:placebo@[1.5.1,);after:inspirations;after:forge@[14.23.5.2836,)")
public class Apotheosis {

	public static final String MODID = "apotheosis";
	public static final String MODNAME = "Apotheosis";
	public static final String VERSION = "1.12.4";
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	public static File configDir;
	public static Configuration config;
	public static boolean enableSpawner = true;
	public static boolean enableGarden = true;
	public static boolean enableDeadly = true;
	public static boolean enableEnch = true;
	public static boolean enablePotion = true;
	public static boolean enchTooltips = true;
	public static float localAtkStrength = 1;

	public Apotheosis() {
		Affix.classload();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		configDir = new File(e.getModConfigurationDirectory(), MODID);
		config = new Configuration(new File(configDir, MODID + ".cfg"));

		ApotheosisCore.enableEnch = enableEnch = config.getBoolean("Enable Enchantment Module", "general", true, "If the enchantment module is enabled.");
		if (enableEnch) MinecraftForge.EVENT_BUS.register(new EnchModule());

		ApotheosisCore.enableSpawner = enableSpawner = config.getBoolean("Enable Spawner Module", "general", true, "If the spawner module is enabled.");
		if (enableSpawner) MinecraftForge.EVENT_BUS.register(new SpawnerModule());

		enableGarden = config.getBoolean("Enable Garden Module", "general", true, "If the garden module is loaded.");
		if (enableGarden) MinecraftForge.EVENT_BUS.register(new GardenModule());

		ApotheosisCore.enableDeadly = enableDeadly = config.getBoolean("Enable Deadly Module", "general", true, "If the deadly module is loaded.");
		if (enableDeadly) MinecraftForge.EVENT_BUS.register(new DeadlyModule());

		ApotheosisCore.enablePotion = enablePotion = config.getBoolean("Enable Potion Module", "general", true, "If the potion module is loaded.");
		if (enablePotion) MinecraftForge.EVENT_BUS.register(new PotionModule());

		enchTooltips = config.getBoolean("Enchantment Tooltips", "client", true, "If apotheosis enchantments have tooltips on books.");

		if (config.hasChanged()) config.save();
		MinecraftForge.EVENT_BUS.post(new ApotheosisPreInit(e));
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent e) throws IOException {
		MinecraftForge.EVENT_BUS.post(new ApotheosisInit(e));
		NETWORK.registerMessage(ParticleMessage.Handler.class, ParticleMessage.class, 0, Side.CLIENT);
	}

	@EventHandler
	public void starting(FMLServerStartingEvent e) {
		e.registerServerCommand(new LootCommand());
	}

	@SubscribeEvent
	public void recipes(Register<IRecipe> e) {
		RecipeHelper helper = new RecipeHelper(Apotheosis.MODID, Apotheosis.MODNAME, new ArrayList<>());
		MinecraftForge.EVENT_BUS.post(new ApotheosisRecipeEvent(helper));
		helper.register(e.getRegistry());
	}

	@SubscribeEvent
	public void trackCooldown(AttackEntityEvent e) {
		EntityPlayer p = e.getEntityPlayer();
		Apotheosis.localAtkStrength = p.getCooledAttackStrength(0.5F);
	}

	public static void registerOverrideBlock(IForgeRegistry<Block> reg, Block b, String modid) {
		reg.register(b);
		ForgeRegistries.ITEMS.register(new ItemBlock(b) {
			@Override
			public String getCreatorModId(ItemStack itemStack) {
				return modid;
			}
		}.setRegistryName(b.getRegistryName()));
	}

	public static Ingredient potionIngredient(PotionType type) {
		return new NBTIngredient(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), type));
	}

	public static void fakesplode(Object w, Object p) {
		WorldServer world = (WorldServer) w;
		BlockPos pos = (BlockPos) p;
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
		world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 1D);
	}

	public static class ApotheosisPreInit extends Event {
		public FMLPreInitializationEvent ev;

		private ApotheosisPreInit(FMLPreInitializationEvent ev) {
			this.ev = ev;
		}
	}

	public static class ApotheosisInit extends Event {
		public FMLInitializationEvent ev;

		private ApotheosisInit(FMLInitializationEvent ev) {
			this.ev = ev;
		}
	}

	public static class ApotheosisRecipeEvent extends Event {
		public RecipeHelper helper;

		private ApotheosisRecipeEvent(RecipeHelper helper) {
			this.helper = helper;
		}
	}

}
