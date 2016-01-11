// Author: Aidan Fisher

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.Random;

import javax.imageio.ImageIO;

public class Card implements Cloneable, Serializable {
	public static int handWidth = 70;
	public static int width = 110;
	public static int height = 160;

	public transient BufferedImage image = null;
	public String name;
	public Minion minion = null;
	public int mana;

	public boolean enemyFace = false;
	public boolean friendlyFace = false; // Usually for heals. Some warlock cards like flame imp.
	public boolean effectedBySpellDamage = false; // Spells only
	public boolean targeted = false; // This is mainly used for spells, but can be used for battlecry effects.
	public boolean randomTarget = false;
	public boolean minionsOnly = false;
	public boolean areaOfEffect = false;
	public boolean randomlySplit = false; // Like AoE, but different
	public boolean enemiesOnly = false;
	public boolean freeze = false;

	public boolean heroPower = false;

	// For code reasons.
	public boolean targetedBattleCry = false; // A specific type of "targeted" for minions. ("An issue with spare parts")

	public int drawCards = 0; // # of cards this card draws, for battlecry, or just when played for spells

	/** Spell specific */
	public int damage = 0;
	public int extraRandomDamage = 0; // Like crackle or boom bots. Both of them would be 3 as well.
	public int attackBuffThisTurn = 0;
	public int attackBuff = 0;
	public int healthBuff = 0;
	public boolean setToTaunt = false;
	public boolean swapHealth = false;
	public boolean giveTempStealth = false;
	public boolean friendliesOnly = false; // Not implemented yet
	public boolean returnToHand = false;

	public Minion transform = null;

	public Weapon weapon = null;

	// Spell.
	public Card(String name, boolean targeted, int mana) {
		this.name = name;
		this.targeted = targeted;
		this.effectedBySpellDamage = true;
		this.mana = mana;
	}

	// Minion.
	public Card(String name, Minion minion, int mana) {
		this.name = name;
		this.minion = minion;
		this.mana = mana;
	}

	// Weapon.
	public Card(String name, int attack, int durability, int mana) {
		this.name = name;
		this.weapon = new Weapon(attack, durability);
		this.mana = mana;
	}

	public Card clone() {
		try {
			Card card = (Card) super.clone();
			if (card.minion != null) {
				card.minion = (Minion) minion.clone();
				card.minion.reference = card;
			}
			if (card.transform != null) {
				card.transform = (Minion) transform.clone();
			}
			return card;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public int getCost() {
		int mana = this.mana;
		for (int i = 0; i < Component.board.minions.get(Component.board.turn).size(); i++) {
			if (minion != null) {
				if (Component.board.minions.get(Component.board.turn).get(i).reference.name.equals("Mechwarper")) {
					if (minion.type.contains("Mech")) {
						mana--;
						if (mana < 0) {
							mana = 0;
						}
					}
				}
			}
		}
		return mana;
	}

	public int getSpellDamage() {
		int spellDamage = 0;
		for (int i = 0; i < Component.board.minions.get(Component.board.turn).size(); i++) {
			spellDamage += Component.board.minions.get(Component.board.turn).get(i).spellDamage;
		}
		return spellDamage;
	}

	// Only for spells: (and targeted battlecries)
	public void play(Minion minion) {
		int p2 = Math.abs(Component.board.turn - 1);
		// Spell damage:
		if (effectedBySpellDamage && damage != 0) {
			// Since a spell can only be played once..
			damage += getSpellDamage();
		}
		damage += new Random().nextInt(extraRandomDamage + 1);

		// Go through the booleans..
		if (enemyFace) { // can't be targeted
			// Assume damage, // Coming from deathrattle or battlecry
			if (minion != null) {
				p2 = Math.abs(minion.health - 1);
			}
			// Minion's health has been set to the side it came from
			Component.board.hero[p2].damage(damage, freeze);
		} else if (friendlyFace) {

		} else if (targeted || randomTarget) {
			if (randomTarget) {
				if (enemiesOnly && !minionsOnly) {
					if (minion != null) {
						p2 = Math.abs(minion.health - 1);
					}
					int n = new Random().nextInt(Component.board.minions.get(p2).size() + 1);
					if (n == Component.board.minions.get(p2).size()) {
						minion = Component.board.hero[p2];
					} else {
						minion = Component.board.minions.get(p2).get(n);
					}
				}
			}
			if (transform != null) {
				int index = Component.board.minions.get(0).indexOf(minion);
				if (index != -1) {
					Component.board.minions.get(0).set(index, transform);
				} else {
					index = Component.board.minions.get(1).indexOf(minion);
					if (index != -1) {
						Component.board.minions.get(1).set(index, transform);
					}
				}
				// Buff updates:
				for (int i = 0; i < Component.board.minions.get(index).size(); i++) {
					Component.board.minions.get(index).get(i).updateBuffBonus();
				}
			} else if (damage == 0 && !freeze) {
				// buff
				minion.attackThisTurn += attackBuffThisTurn;
				minion.attack += attackBuffThisTurn;
				minion.attack += attackBuff;
				minion.health += healthBuff;
				minion.maxHealth += healthBuff;
				if (setToTaunt) {
					minion.taunt = true;
				}
				if (swapHealth) {
					int attack = minion.attack;
					minion.attack = minion.health;
					minion.attackThisTurn = 0;
					minion.attackBuff = 0;
					minion.health = attack;
					minion.maxHealth = attack;
					minion.updateBuffBonus();
				}
				if (giveTempStealth) {
					minion.stealth = true;
					minion.loseStealth = true; // If it is not temporary, this should be SET to false;
				}

				if (returnToHand) {
					int side = Component.board.getSideJustMinions(minion);
					Component.board.minions.get(side).remove(minion);
					// Add specific card to hand:
					Component.board.addCard(side, Board.findOriginalCard(minion.reference).clone());
				}
			} else {
				minion.damage(damage, freeze);
			}
		} else if (areaOfEffect) {
			// Always hits their minions
			for (int i = 0; i < Component.board.minions.get(p2).size(); i++) {
				Component.board.minions.get(p2).get(i).damage(damage, freeze); // If died:
			}
			if (!minionsOnly) {
				Component.board.hero[p2].damage(damage, freeze);
			}
			if (!enemiesOnly) {
				for (int i = 0; i < Component.board.minions.get(Component.board.turn).size(); i++) {
					Component.board.minions.get(Component.board.turn).get(i).damage(damage, freeze); // If died:
				}
			}
			if (!minionsOnly && !enemiesOnly) {
				Component.board.hero[Component.board.turn].damage(damage, freeze);
			}

		} else if (randomlySplit) {
			// All randomly split effects CAN hit heroes (heroes are the size option)
			for (int i = 0; i < damage; i++) {
				if (enemiesOnly) {
					int n = new Random().nextInt(Component.board.minions.get(p2).size() + 1);
					if (n == Component.board.minions.get(p2).size()) {
						// Hit their face:
						Component.board.hero[p2].damage(1, false);
					} else {
						// Hit a minion
						Component.board.minions.get(p2).get(n).damage(1, false);
					}
				}
			}
		} else if (transform != null) { // Spell that summons a minion.
			// Needs an int for # spawned.

			// Always spawns to far right.
			Component.board.summonMinion(Component.board.turn, Component.board.minions.size(), transform, false);
		}
		for (int i = 0; i < drawCards; i++) {
			Component.board.drawCard(Component.board.turn);
		}
	}

	public void setImage() {
		if (this.name.equals("Mage")) {
			try {
				image = ImageIO.read(new File("res/mage.png"));
			} catch (Exception e) {
			}
		}
		for (int i = 0; i < Component.cards.size(); i++) {
			if (Component.cards.get(i).name.equals(this.name)) {
				this.image = Component.cards.get(i).image;
				return;
			}
		}
		for (int i = 0; i < Component.spareParts.size(); i++) {
			if (Component.spareParts.get(i).name.equals(this.name)) {
				this.image = Component.spareParts.get(i).image;
				return;
			}
		}
		for (int i = 0; i < Component.unlistedCards.size(); i++) {
			if (Component.unlistedCards.get(i).name.equals(this.name)) {
				this.image = Component.unlistedCards.get(i).image;
				return;
			}
		}
	}

	public void setHeroAbilityImage() {
		if (this.name.equals("Fireblast")) {
			try {
				image = ImageIO.read(new File("res/fireblast.png"));
			} catch (Exception e) {
			}
			return;
		}
	}

	public void render(Graphics2D g, int x, int y) {
		if (image != null) {
			g.drawImage(image, x, y, width, height, null);
		} else {
			setImage();
			// Need to set image.
			/*g.setColor(new Color(180, 180, 180));
			g.fillRect(x, y, width, height);
			g.setColor(new Color(0, 0, 0));
			g.drawRect(x, y, width, height);
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			int width = g.getFontMetrics().stringWidth(minion.attack + " / " + minion.health);
			g.drawString(minion.attack + " / " + minion.health, x + (Card.width - width) / 2, y + height - 10);
			g.setColor(new Color(0, 0, 180));
			g.drawString(String.valueOf(mana), x + Card.width - 20, y + 25);*/
		}
	}
}
