package com.example.lupan.memo;

import java.util.Random;


public class Randomizer {

	int[] array;
	int cnt;
	Random rnd;

	public Randomizer(int cnt) {
		array = new int[cnt];
		for (int i = 0; i < cnt; i++) {
			array[i] = i;
		}
		this.cnt = cnt;
		rnd = new Random();
	}

	public Randomizer(int[] arr) {
		array = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			array[i] = arr[i];
		}
		this.cnt = arr.length;
		rnd = new Random();
	}

	public int getRandomValue() {
		assert cnt > 0;
		int pos = rnd.nextInt(cnt);
		int value = array[pos];
		array[pos] = array[--cnt];
		return value;
	}
}
