// Author: Aidan Fisher

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameSet {

	public static GameSet lastGameSet = new GameSet();

	public static int[] lastAccess = null;

	public static int totalSets = 1; // Sets past the first set will be much slower. (But are also much more beneficial)
	public static int currentSet = 0;
	public static int numPerSet = 10000;

	public static double currentWeighting = 0.58;
	public static double increaseToWeighting = 0.02;

	public static int numRequired = 300; // It finds 100; then is done. Data that is more different is more likely to skew the data?
	public static int maxDifferenceValue = 5;

	public static GameSet currentGameSet = new GameSet();

	public java.util.Map<String, Integer> stringMatches = new HashMap<>();
	public ArrayList<String> stringsThatMatch = new ArrayList<String>();

	public int numDimensions = 15;
	public final static int DEFAULT = 0, CARD_NAME = 1, IS_HERO = 2, IS_ENEMY = 3, IS_TARGET_DIVINE_SHIELD = 4, IS_TARGET_TAUNT = 5, TARGET_ATTACK = 6, TARGET_HEALTH = 7, ENEMY_HEALTH = 8,
			FRIENDLY_HEALTH = 9, ENEMY_BOARD_VALUE = 10, FRIENDLY_BOARD_VALUE = 11, ATTACKER_ATTACK = 12, ATTACKER_HEALTH = 13, IS_ATTACKER_DIVINE_SHIELD = 14;
	public Object[][][][][][][][][][][][][][][] profile = new Object[1][0][0][0][0][0][0][0][0][0][0][0][0][0][0]; // Enter access, (for easier programming,) then isHero, isEnemy, isDivineShield, isTaunt, Attack, Health, EnemyHeroHealth, FriendlyHeroHealth, EnemyBoardValue, FriendlyBoardValue, AttackerAttacker, AttackerHealth, AttackerIsDivineShield

	// It absolutely cannot switch between card name, default and isEnemy.
	public static int[] priority = { ENEMY_HEALTH, FRIENDLY_HEALTH, IS_TARGET_TAUNT, ATTACKER_HEALTH, TARGET_ATTACK, FRIENDLY_BOARD_VALUE, ENEMY_BOARD_VALUE, TARGET_HEALTH, ATTACKER_ATTACK,
			IS_ATTACKER_DIVINE_SHIELD, IS_TARGET_DIVINE_SHIELD };
	public static float[] differences = { 0.5f, 0.6f, 0.7f, 1.5f, 1.7f, 1.8f, 1.9f, 2f, 2.2f, 5.5f, 9 }; // Approximations for now.

	public static ArrayList<int[]> profileChecks = new ArrayList<int[]>();
	public static ArrayList<Float> profileDifferences = new ArrayList<Float>();

	public GameSet() {
		stringMatches.put("", stringsThatMatch.size());
		stringsThatMatch.add(""); // Just 'cause.. I want it to be 0.
	}

	public static void setPriorityDifferences() {
		int min = -4;
		int max = 5;
		// Throw 
		int[] checks = new int[differences.length];
		for (int i = 0; i < checks.length; i++) {
			checks[i] = min;
		}
		do {
			float diff = getDifference(checks);
			if (diff < maxDifferenceValue) {
				for (int i = 0; i <= profileChecks.size(); i++) {
					if (i == profileChecks.size()) {
						profileChecks.add(Arrays.copyOf(checks, checks.length));
						profileDifferences.add(diff);
						break;
					}
					if (diff < profileDifferences.get(i)) {
						profileChecks.add(i, Arrays.copyOf(checks, checks.length));
						profileDifferences.add(i, diff);
						break;
					}
				}
			}
		} while (increaseCurrentArrayCount(checks, 0, min, max));
		System.out.println("Finished: " + profileDifferences.size());
	}

	public static void reset() {
		GameSet.currentSet++;
		Component.wins[0] = 0;
		Component.wins[1] = 0;
		currentWeighting += increaseToWeighting;
		lastGameSet = currentGameSet;
		currentGameSet = new GameSet();
	}

	// Gets the profile difference between the values (assuming the other array is 0,0,0..0)
	// Also in skewed order.
	private static float getDifference(int[] array) {
		// Difference to 0:
		float difference = 0;
		for (int i = 0; i < array.length; i++) {
			difference += getDifference(i, array[i]);
		}
		return difference;
	}

	private static float getDifference(int index, int value) {
		// Difference to 0:
		if (value == 0) {
			return 0;
		} else {
			return (float) (differences[index] * Math.pow(2, Math.abs(value) - 1));
		}
	}

	private static boolean increaseCurrentArrayCount(int[] array, int index, int min, int max) {
		if (index == array.length) {
			return false; // Done.
		}
		float total = 0;
		for (int i = array.length - 1; i > index; i--) {
			total += getDifference(i, array[i]);
			if (total >= maxDifferenceValue) {
				// The values below should all be min already
				array[i] = array[i] + 1;
				if (array[i] == max) {
					array[i] = min;
					return increaseCurrentArrayCount(array, i + 1, min, max);
				}
				return true;
			}
		}
		array[index] = array[index] + 1;
		if (array[index] == max) {
			array[index] = min;
			return increaseCurrentArrayCount(array, index + 1, min, max);
		}
		return true;
	}

	public Profile getProfile(String string, Minion target, boolean isHero, boolean isEnemy, int enemyHeroHealth, int friendlyHeroHealth, int enemyBoardValue, int friendlyBoardValue, Minion attacker) {
		Integer stringMatch = stringMatches.get(string);
		if (stringMatch == null) {
			stringMatch = stringsThatMatch.size();
			stringMatches.put(string, stringsThatMatch.size());
			stringsThatMatch.add(string);
		}
		int[] access;
		if (target != null) {
			if (attacker != null) {
				access = new int[] { 0, 0, isHero ? 0 : 1, isEnemy ? 0 : 1, target.divineShield ? 0 : 1, target.taunt ? 0 : 1, target.attack, target.health - 1, (enemyHeroHealth - 1) / 10,
						(friendlyHeroHealth - 1) / 10, enemyBoardValue, friendlyBoardValue, attacker.attack - 1, attacker.health - 1, attacker.divineShield ? 0 : 1 }; // Attack CANNOT be 0 for the attacker
			} else {
				access = new int[] { 0, stringMatch, isHero ? 0 : 1, isEnemy ? 0 : 1, target.divineShield ? 0 : 1, target.taunt ? 0 : 1, target.attack, target.health - 1, (enemyHeroHealth - 1) / 10,
						(friendlyHeroHealth - 1) / 10, enemyBoardValue, friendlyBoardValue, 0, 0, 0 };
			}
		} else {
			access = new int[] { 0, stringMatch, 0, 0, 0, 0, 0, 0, (enemyHeroHealth - 1) / 10, (friendlyHeroHealth - 1) / 10, enemyBoardValue, friendlyBoardValue, 0, 0, 0 };
		}
		lastAccess = access;
		return getProfile(access, true); // Values should be safe.
	}

	// If it is not safe, it will check for < 0 & NOT create new profiles.
	public Profile getProfile(int[] access, boolean safe) {
		if (!safe) {
			for (int i = 0; i < access.length; i++) {
				if (access[i] < 0) {
					return null;
				}
			}
		}
		try {
			Object[][] object = profile;
			for (int i = 1; i < access.length; i++) {
				if (object[access[i - 1]].length <= access[i]) {
					if (!safe) {
						return null;
					}
					Object[] copy = object[access[i - 1]];
					int[] newArraySizes = new int[access.length - i];
					int[] arraySizeInside = Arrays.copyOf(newArraySizes, access.length - i - 1);
					for (int l = 0; l < access.length - i; l++) {
						newArraySizes[l] = 0;
					}
					newArraySizes[0] = access[i] + 1;
					object[access[i - 1]] = (Object[]) Array.newInstance(Object.class, newArraySizes);
					for (int n = 0; n < copy.length; n++) {
						object[access[i - 1]][n] = copy[n];
					}
					for (int n = copy.length; n < object[access[i - 1]].length - 1; n++) {
						if (arraySizeInside.length != 0) {
							arraySizeInside[0] = 0;
							object[access[i - 1]][n] = Array.newInstance(Object.class, arraySizeInside);
						} else {
							object[access[i - 1]][n] = new Profile();
						}
					}
					if (arraySizeInside.length != 0) {
						arraySizeInside[0] = 0;
						object[access[i - 1]][object[access[i - 1]].length - 1] = Array.newInstance(Object.class, arraySizeInside);
					} else {
						object[access[i - 1]][object[access[i - 1]].length - 1] = new Profile();
					}
				}
				if (i == access.length - 1) {
					return (Profile) object[access[i - 1]][access[i]];
				}
				object = (Object[][]) object[access[i - 1]];
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(1 / 0);
		}
		return null;
	}

}
