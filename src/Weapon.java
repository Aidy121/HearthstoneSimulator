// Author: Aidan Fisher

import java.io.Serializable;

public class Weapon implements Serializable {

	public int numAttacks = 1;
	public int attack = 0;
	public int durability = 0; // Hero weapons only.

	public Weapon(int attack, int durability) {
		this.attack = attack;
		this.durability = durability;
	}

	public void equip(Minion hero) {
		hero.attack = attack;
	}

	public void use() {
		durability--;
		if (durability == 0) {
			if (this == Component.board.weapon[0]) {
				Component.board.weapon[0] = null;
			} else if (this == Component.board.weapon[1]) {
				Component.board.weapon[1] = null;
			}
		}
	}
}
