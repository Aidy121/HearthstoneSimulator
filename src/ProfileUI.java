// Author: Aidan Fisher

import java.awt.*;
import java.util.ArrayList;

public class ProfileUI {

	public static ArrayList<Profile> profiles = new ArrayList<Profile>();
	public static boolean[] overrideAll = new boolean[GameSet.currentGameSet.numDimensions];
	public static int[] access = new int[GameSet.currentGameSet.numDimensions];

	public static int x = 0;
	public static int y = 25;
	public static int lineSize = 30;
	public static int textSize = 20;
	public static int length = 60;
	public static int rows = 1;

	public static void render(Graphics g) {
		try {

			profiles.clear();
			Object[] objectAccessing = GameSet.currentGameSet.profile[0];
			g.setColor(new Color(0, 0, 0));
			getNextProfile(1, objectAccessing, g);
			for (int i = 1; i < access.length; i++) {

			}

			Profile profile = Profile.consolidate(profiles);

			// Draw final stats.
			int i = access.length;
			g.drawRect(x + ((i - 1) / rows) * length + 20, y + (i - 1) % rows * lineSize, length, lineSize);
			g.drawString(profile.rating + " / " + profile.totalCases + " (" + ((int) ((profile.rating / (double) profile.totalCases) * 1000)) / 10.0 + "%)", x + ((i - 1) / rows) * length + 25, y
					+ (i - 1) % rows * lineSize + lineSize - 5);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void getNextProfile(int i, Object[] objectAccessing, Graphics g) throws Exception {
		if (overrideAll[i]) {
			g.drawRect(x + ((i - 1) / rows) * length + 20, y + (i - 1) % rows * lineSize, length, lineSize);
			//String string = getString(access[i];
			g.drawString("All", x + ((i - 1) / rows) * length + 25, y + (i - 1) % rows * lineSize + lineSize - 5);

			if (i < access.length - 1) {
				i++;
				for (int n = 0; n < objectAccessing.length; n++) {
					getNextProfile(i, (Object[]) objectAccessing[n], g);
				}
			} else {
				for (int n = 0; n < objectAccessing.length; n++) {
					profiles.add((Profile) objectAccessing[n]);
				}
			}
		} else {
			if (access[i] >= objectAccessing.length) {
				return;
			}
			g.drawRect(x + ((i - 1) / rows) * length + 20, y + (i - 1) % rows * lineSize, length, lineSize);
			//String string = getString(access[i];
			g.drawString(getString(access[i], i), x + ((i - 1) / rows) * length + 25, y + (i - 1) % rows * lineSize + lineSize - 5);
			if (i < access.length - 1) {
				objectAccessing = (Object[]) objectAccessing[access[i]];
				i++;
				getNextProfile(i, objectAccessing, g);
			} else {
				profiles.add((Profile) objectAccessing[access[i]]);
			}
		}
	}

	// 0 is skipped!

	public static String getString(int value, int access) {
		if (access == 1) {
			return GameSet.currentGameSet.stringsThatMatch.get(value);
		} else if (access == 2) {
			return value == 0 ? "Hero" : "Minion";
		} else if (access == 3) {
			return value == 0 ? "Enemy" : "Friendly";
		} else if (access == 4) {
			return value == 0 ? "DS" : "No DS";
		} else if (access == 5) {
			return value == 0 ? "Taunt" : "No Taunt";
		} else if (access == 6) {
			return "ATT: " + value;
		} else if (access == 7) {
			return "HP: " + (value + 1);
		} else if (access == 8) {
			return "E/HH: " + ((value + 1) * 10 - 9) + "-" + ((value + 1) * 10);
		} else if (access == 9) {
			return "F/HH: " + ((value + 1) * 10 - 9) + "-" + ((value + 1) * 10);
		} else if (access == 10) {
			return "E/BV: " + value;
		} else if (access == 11) {
			return "F/BV: " + value;
		} else if (access == 12) {
			return "A/ATT: " + (value + 1);
		} else if (access == 13) {
			return "A/HP: " + (value + 1);
		} else if (access == 14) {
			return value == 0 ? "A:DS" : "A:No DS";
		}
		return null;
	}

	public static void changeAccess(int accessNum, int newVal) {
		if (newVal >= 0) {
			Object[] objectAccessing = GameSet.currentGameSet.profile[0];
			accessObject(objectAccessing, accessNum, newVal, 1);
			for (int i = 1; i <= accessNum; i++) {

			}
		}
	}

	public static void accessObject(Object[] objectAccessing, int accessNum, int newVal, int i) {
		if (overrideAll[i] && i != accessNum) {
			i++;
			for (int n = 0; n < objectAccessing.length; n++) {
				accessObject((Object[]) objectAccessing[n], accessNum, newVal, i);
			}
			return;
		} else if (access[i] >= objectAccessing.length) { // doesn't work when all-inclusive.
			return;
		}
		if (i == accessNum) {
			if (newVal < objectAccessing.length) {
				access[accessNum] = newVal;
			}
		} else {
			objectAccessing = (Object[]) objectAccessing[access[i]];
			i++;
			accessObject(objectAccessing, accessNum, newVal, i);
		}
	}
}
