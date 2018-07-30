package com.oopsjpeg.enigma.util;

import java.util.Random;

public class ChanceBag {
	private final Random random = new Random();
	private float chance = 0;
	private int count = 0;

	public ChanceBag() {}

	public ChanceBag(float chance) {
		setChance(chance);
	}

	public boolean get() {
		if (getChance() > 0 && (random.nextFloat() <= getChance() || count())) {
			setCount(0);
			return true;
		} else return false;
	}

	public boolean count() {
		if (getChance() > 0) {
			setCount(getCount() + 1);
			return getCount() >= Math.ceil(1 / getChance());
		}
		return false;
	}

	public float getChance() {
		return chance;
	}

	public void setChance(float chance) {
		this.chance = Math.max(0, Math.min(1, chance));
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = Math.max(0, count);
	}

	@Override
	public String toString() {
		return String.valueOf(getChance());
	}
}
