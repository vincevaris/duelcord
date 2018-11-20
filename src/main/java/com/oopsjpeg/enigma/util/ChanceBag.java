package com.oopsjpeg.enigma.util;

public class ChanceBag {
	private float chance = 0;
	private int attempts = 0;
	private float influence = 1;

	public ChanceBag() { }

	public ChanceBag(float chance) {
		this.chance = chance;
	}

	public ChanceBag(float chance, float influence) {
		this.chance = chance;
		this.influence = influence;
	}

	public boolean get() {
		if (attempt() || (chance > 0 && Util.RANDOM.nextFloat() <= chance)) {
			attempts = 0;
			return true;
		}
		return false;
	}

	public boolean attempt() {
		if (chance > 0) {
			attempts++;
			return attempts >= 1 / (chance * influence);
		}
		return false;
	}

	public float getChance() {
		return chance;
	}

	public void setChance(float chance) {
		this.chance = chance;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public float getInfluence() {
		return influence;
	}

	public void setInfluence(float influence) {
		this.influence = influence;
	}

	@Override
	public String toString() {
		return String.valueOf(chance);
	}
}
