package shadows.deadly.loot;

import net.minecraft.util.WeightedRandom;

public class AffixModifier extends WeightedRandom.Item {

	/**
	 * The language key for this modifier.
	 */
	protected final String key;

	/**
	 * What this modifier does to the underlying affix.
	 */
	protected final Operation op;

	/**
	 * The value of this modifier.
	 */
	protected final float value;

	protected boolean editName = true;

	public AffixModifier(String key, Operation op, float value, int weight) {
		super(weight);
		this.key = key;
		this.op = op;
		this.value = value;
	}

	public AffixModifier dontEditName() {
		editName = false;
		return this;
	}

	/**
	 * Adjusts the passed level, according to the operation of this modifier.
	 */
	public float editLevel(float level) {
		return op == Operation.ADD ? (level + value) : op == Operation.MULTIPLY ? (level * value) : value;
	}

	/**
	 * Get the translation key of this modifier.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * If this modifier should edit the name of the affix.
	 */
	public boolean editName() {
		return editName;
	}

	public static enum Operation {
		ADD,
		MULTIPLY,
		SET;
	}

}
