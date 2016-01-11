// Author: Aidan Fisher

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Profile implements Comparable<Profile> {

	public int rating = 0;
	public int totalCases = 0;

	public Profile() {
	}

	public void addCase(int rating) {
		this.rating += rating;
		this.totalCases += Math.abs(rating);
	}

	public static Profile consolidate(ArrayList<Profile> profiles) {
		Profile profile = new Profile();
		for (Profile profileItem : profiles) {
			profile.rating += profileItem.rating;
			profile.totalCases += profileItem.totalCases;
		}
		return profile;
	}

	public static ArrayList<Profile> getAllChildren(/*int[] vars, */int pos, Object[] curr, ArrayList<Profile> profiles) {
		//vars = Arrays.copyOf(vars, vars.length + 1);
		for (int i = 0; i < curr.length; i++) {
			//vars[vars.length - 1] = i;
			if (pos == GameSet.lastGameSet.numDimensions - 1) {
				profiles.add((Profile) curr[i]);
				//last.add(vars.clone());
			} else {
				getAllChildren(/*vars, */pos + 1, (Object[]) curr[i], profiles);
			}
		}
		return profiles;
	}

	public int compareTo(Profile o) {
		// TODO Auto-generated method stub
		if ((rating / (double) totalCases) > (o.rating / (double) o.totalCases)) {
			return 1;
		} else if ((rating / (double) totalCases) < (o.rating / (double) o.totalCases)) {
			return -1;
		} else {
			return 0;
		}
	}
}
