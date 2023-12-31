package shadows;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import shadows.deadly.asm.DeadlyTransformer;
import shadows.ench.anvil.asm.AnvilTransformer;
import shadows.ench.asm.EnchTransformer;
import shadows.potion.asm.PotionTransformer;
import shadows.spawn.asm.SpawnerTransformer;
import shadows.util.BedTransformer;

public class ApotheosisTransformer implements IClassTransformer {

	public List<IApotheosisTransformer> transformers = new LinkedList<>();

	public ApotheosisTransformer() {
		transformers.add(new AnvilTransformer());
		transformers.add(new EnchTransformer());
		transformers.add(new PotionTransformer());
		transformers.add(new SpawnerTransformer());
		transformers.add(new DeadlyTransformer());
		transformers.add(new BedTransformer());
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		for (IApotheosisTransformer t : transformers)
			if (t.accepts(name, transformedName)) return t.transform(name, transformedName, basicClass);
		return basicClass;
	}

	public static interface IApotheosisTransformer extends IClassTransformer {
		boolean accepts(String name, String transformedName);
	}

}
