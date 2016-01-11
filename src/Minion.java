// Author: Aidan Fisher

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

public class Minion implements Cloneable, Serializable, Comparable<Minion> {

	public Card reference; // The reference card

	public int attacksLeft = 0; // This is set to 1 of every turn

	public int health;
	public int maxHealth;

	public int attack;
	public int attackThisTurn;

	public int healthBuff = 0;
	public int attackBuff = 0;

	public int attackGain = 0; // For stuff like cogmaster

	public int spellDamage = 0; // Defaulted to 0

	// Special Effects:
	public boolean frozen = false;
	public boolean divineShield = false;
	public boolean taunt = false;
	public boolean stealth = false;
	public boolean loseStealth = false; // Lose stealth at start of turn

	public boolean isDead = false;

	public ArrayList<String> type = new ArrayList<String>();

	public Minion(int attack, int health) {
		this.attack = attack;
		this.health = health;
		this.maxHealth = health;
	}

	public Minion clone() {
		try {
			return (Minion) super.clone();
		} catch (Exception e) {
			System.out.println("AH: " + e);
		}
		return null;
	}

	public void playerCastedSpell() {
		// Minions with "Whenever you cast a spell" on them:
		if (reference.name.equals("Mana Wyrm")) {
			this.attack++;
		} else if (reference.name.equals("Wild Pyromancer")) {
			// Cast its spell
			reference.play(null);
		} else if (reference.name.equals("Violet Teacher")) {
			// Summon a minion
			Card card = Board.findOriginalCard("Violet Apprentice").clone();
			// Spawn minion:
			Component.board.summonMinion(Component.board.turn, Component.board.minions.get(Component.board.turn).indexOf(this) + 1, card.minion, false);
		} else if (reference.name.equals("Archmage Antonidas")) {
			Card fireBall = Board.findOriginalCard("Fireball").clone();
			Component.board.addCard(Component.board.turn, fireBall);
		}
	}

	public void minionSummoned(Minion minion) {
		// Minions with "Whenever you play a minion" on them:
		if (reference.name.equals("Knife Juggler")) {
			reference.play(null);
		}
	}

	public void updateBuffBonus() {
		int oldHealthBuff = healthBuff;
		maxHealth -= healthBuff;
		healthBuff = 0;

		attack -= attackBuff;
		attackBuff = 0;
		int side = Component.board.getSideJustMinions(this);
		boolean attackBuffUsed = false;
		for (int i = 0; i < Component.board.minions.get(side).size(); i++) {
			Minion minion = Component.board.minions.get(side).get(i);
			if (!attackBuffUsed && this.reference.name.equals("Cogmaster") && minion.type.contains("Mech")) {
				attackBuff += attackGain;
				attackBuffUsed = true;
			}

			if (minion.reference.name.equals("Stormwind Champion") && this != minion) {
				attackBuff++;
				healthBuff++;
			}
		}
		attack += attackBuff;
		maxHealth += healthBuff;

		if (healthBuff > oldHealthBuff) {
			health += (healthBuff - oldHealthBuff);
		}

		if (health > maxHealth) {
			health = maxHealth;
		}
		if (attack <= 0) {
			attack = 0;
		}
	}

	// This returns "where" incase of summon effects that summon things to the left of the minion, (ie dr. boom)
	public void battleCry() {
		for (int i = 0; i < reference.drawCards; i++) {
			Component.board.drawCard(Component.board.turn);
		}
		if (reference.name.equals("Arcane Golem")) {
			Component.board.mana[Math.abs(Component.board.turn - 1)]++;
		} else if (reference.name.equals("Goblin Blastmage")) {
			boolean mech = false;
			// See if they have a mech:
			for (int i = 0; i < Component.board.minions.get(Component.board.turn).size(); i++) {
				if (Component.board.minions.get(Component.board.turn).get(i).type.contains("Mech")) {
					mech = true;
					break;
				}
			}
			if (mech) {
				reference.play(null);
			}
		} else if (reference.targetedBattleCry) {
			// If there is no minions for it:
			if (reference.minionsOnly) { // Its always possible for the hero, (except for immune)
				if (reference.friendliesOnly && Component.board.minions.get(Component.board.turn).size() == 0) {
					// No battlecry possible
				}
				if (reference.enemiesOnly && Component.board.minions.get(Math.abs(Component.board.turn - 1)).size() == 0) {
					// No battlecry possible
				}
				if (Component.board.minions.get(0).size() == 0 && Component.board.minions.get(1).size() == 0) {
					// No battlecry possible
				}
			}
			Listening.battlecryBuff = reference;
		} else if (reference.name.equals("Tinkertown Technician")) {
			boolean mech = false;
			// See if they have a mech:
			for (int i = 0; i < Component.board.minions.get(Component.board.turn).size(); i++) {
				if (Component.board.minions.get(Component.board.turn).get(i).type.contains("Mech")) {
					mech = true;
					break;
				}
			}
			if (mech) {
				reference.play(this);
				// And draw spare part:
				Card sparePart = Component.spareParts.get(new Random().nextInt(7));
				Component.board.addCard(Component.board.turn, sparePart);
			}
		} else if (reference.name.equals("Dr. Boom")) {
			Card card = Board.findOriginalCard("Boom Bot").clone();
			// Spawn minion:
			if (Component.board.minions.get(Component.board.turn).size() < 6) { // Have to leave room for Dr. Boom
				Component.board.summonMinion(Component.board.turn, Component.board.toBeSummonedLocation, card.minion, false);
				Component.board.toBeSummonedLocation -= 1; // As to put dr. boom in the center.
			}
			card = card.clone();
			if (Component.board.minions.get(Component.board.turn).size() < 6) { // Have to leave room for Dr. Boom
				Component.board.summonMinion(Component.board.turn, Component.board.toBeSummonedLocation, card.minion, false);
			}
		}
	}

	public void startOfTurn() {
		attacksLeft = 1;
		if (frozen) {
			frozen = false;
			attacksLeft = 0;
		}
		if (loseStealth) {
			loseStealth = false;
			stealth = false;
		}
	}

	public double getValue() { // GET value is based on who's turn it is, (atleast for now)
		if (this.isDead) {
			return 0;
		} else {
			int side = Component.board.getSideJustMinions(this);
			double total = 0;
			if (side != 2 && this.reference.name.equals("Mechwarper")) {
				// Value is added to:
				total += Math.sqrt(Component.board.cards.get(side).size() + 1) * 0.6; // Spare parts make this a bit iffy
			} else if (this.reference.name.equals("Cogmaster")) {
				if (attackBuff >= 2) { // This is a minor AI compliclation with other buffs.
					// Assume it to be buffed, (value goes down)
					total -= 0.8;
				} else {
					// Can be buffed, (value goes up)
					total += 0.8;
				}
			} else if (this.reference.name.equals("Archmage Antonidas")) {
				total += 15;
			} else if (side != 2 && Component.board.deckName[side].contains("Mech") && this.type.contains("Mech")) {
				// Value goes up 'cause mech synergy, (can be specific for each mech deck.
				total += 0.2;
			}
			int attack = this.attack;
			if (attack >= 8) {
				attack = 5; // For evaluation purposes.
			} else if (attack == 7) {
				attack -= 2.1; // 7 health isn't exactly desirable (0.1 increase) (4.9)
			} else if (attack == 6) {
				attack -= 1.2; // 0.4 increase (4.8)
			} else if (attack == 5) {
				attack -= 0.6; // 0.6 increase (4.4)
			} else if (attack == 4) {
				attack -= 0.2; // 0.8 increase (3.8)
			}
			total += (attack + .5) * (this.health + 1.5);
			if (this.divineShield) {
				total += (3 * attack);
			}
			if (this.taunt) {
				total += 0.5 + 0.3 * health;
			}
			return total;
		}
	}

	public void endOfTurn() {
		attack -= attackThisTurn;
		attackThisTurn = 0;
	}

	public boolean canAttack() {
		return attacksLeft >= 1 && !frozen && attack > 0;
	}

	// Can attack has *nothing* to do with the minion that is attacking
	public static boolean canAttack(Minion minion) {
		if (minion.taunt) {
			return true;
		} else if (minion.stealth) {
			return false;
		} else {
			// See if any of the other minions are tuant:
			int n = Component.board.getSideAll(minion);
			for (int i = 0; i < Component.board.minions.get(n).size(); i++) {
				if (Component.board.minions.get(n).get(i).taunt) {
					return false;
				}
			}
			return true;
		}
	}

	public boolean attack(Minion minion) {
		// See if attack can be done:
		if (Minion.canAttack(minion)) {
			this.stealth = false; // Remove stealth (if it is)
			if (Component.board.hero[0] == this) {
				if (Component.board.weapon[0] != null) {
					Component.board.weapon[0].use();
				}
			} else if (Component.board.hero[1] == this) {
				if (Component.board.weapon[1] != null) {
					Component.board.weapon[1].use();
				}
			}
			this.attacksLeft--;
			this.damage(minion.attack, minion.reference.freeze); // This method kills the minion that was queued up to die.. lol.
			minion.damage(this.attack, this.reference.freeze);
			return true;
		} else {
			return false;
		}
	}

	public boolean spellTargetable(boolean spell) {
		return !stealth;
		// boolean stealth is for faerie dragon effect!
	}

	public void damage(int amount, boolean freeze) {
		if (this.divineShield) {
			divineShield = false;
		} else {
			this.health -= amount;
			if (freeze) {
				this.frozen = true;
			}
			// Death check done in cleanup.
		}
	}

	public boolean cleanUp() {
		if (this.health <= 0) {
			die();
			return true;
		}
		return false;
	}

	public void die() {
		// Remove minion:
		this.isDead = true;
		int p = 2;
		int loc = Component.board.minions.get(0).indexOf(this);
		if (Component.board.minions.get(0).remove(this)) {
			p = 0;
		} else {
			loc = Component.board.minions.get(1).indexOf(this);
			if (Component.board.minions.get(1).remove(this)) {
				p = 1;
			}
		}
		if (loc < Component.board.toBeSummonedLocation && p == Component.board.turn) {
			Component.board.toBeSummonedLocation--;
		}

		if (p == 2) {
			// Hero has died:
			if (!Component.gameOver) {
				if (this == Component.board.hero[0]) {
					Component.lastWinner = 1;
				} else if (this == Component.board.hero[1]) {
					Component.lastWinner = 0;
				} else {
					System.out.println("Major Issue: " + this.reference.name);
					System.out.println(1 / 0);
				}
				Component.gameOver = true;
			}
			return;
		}

		// Deathrattle:
		if (reference.name.equals("Loot Hoarder")) {
			Component.board.drawCard(p);
		} else if (reference.name.equals("Harvest Golem")) {
			Card card = Board.findOriginalCard("Damaged Golem").clone();
			// Spawn minion:
			Component.board.summonMinion(p, loc, card.minion, false);
		} else if (reference.name.equals("Sludge Belcher")) {
			Card card = Board.findOriginalCard("Slime").clone();
			// Spawn minion:
			Component.board.summonMinion(p, loc, card.minion, false);
		} else if (reference.name.equals("Leper Gnome") || reference.name.equals("Boom Bot")) { // All spell related effects go here
			this.health = p; // Health gets set to which side its on
			reference.play(this);
		} else if (reference.name.equals("Clockwork Gnome")) { // Spare parts
			Card sparePart = Component.spareParts.get(new Random().nextInt(7));
			Component.board.addCard(p, sparePart);
		} else if (reference.name.equals("Mechanical Yeti")) {
			Card sparePart = Component.spareParts.get(new Random().nextInt(7));
			Component.board.addCard(0, sparePart);
			sparePart = Component.spareParts.get(new Random().nextInt(7));
			Component.board.addCard(1, sparePart);
		}

		// Buff updates:
		for (int i = 0; i < Component.board.minions.get(p).size(); i++) {
			Component.board.minions.get(p).get(i).updateBuffBonus();
		}
	}

	public void render(Graphics2D g, int x, int y) {
		if (Listening.minionSelectedLocation != -1 && this == Component.board.minions.get(Component.board.turn).get(Listening.minionSelectedLocation)) {
			g.setColor(new Color(80, 220, 80, 180));
		} else if (Component.mousePos != null && Component.mousePos.x > x && Component.mousePos.y > y && Component.mousePos.x < x + Card.width && Component.mousePos.y < y + Card.height
				&& Listening.minionSelectedLocation != -1) {
			g.setColor(new Color(220, 120, 120, 180));
		} else if (Component.mousePos != null && Component.mousePos.x > x && Component.mousePos.y > y && Component.mousePos.x < x + Card.width && Component.mousePos.y < y + Card.height) {
			g.setColor(new Color(160, 200, 160, 180));
		} else if (attacksLeft == 0 && Component.board.minions.get(Component.board.turn).contains(this)) {
			g.setColor(new Color(150, 150, 150, 180));
		} else {
			g.setColor(new Color(200, 200, 200, 0));
		}

		g.setStroke(new BasicStroke(3));
		if (reference.image == null) {
			reference.setImage();
		}
		g.drawImage(reference.image, x, y, (int) (Card.width), (int) (Card.height), null);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		g.fillRect(x, y, Card.width, Card.height);

		g.setColor(new Color(255, 185, 29));
		g.fillRect(x, y + Card.height - 25, 25, 25);
		g.setColor(new Color(220, 39, 39));
		g.fillRect(x + Card.width - 25, y + Card.height - 25, 25, 25);
		g.setColor(new Color(220, 220, 220));
		g.fillRect(x + 30, y + Card.height - 75, 40, 25);
		g.setColor(new Color(0, 0, 0));
		g.drawString(String.valueOf(attack), x + 5, y + Card.height - 5);
		g.drawString(String.valueOf(health), x + Card.width - 20, y + Card.height - 5);

		g.drawString(String.valueOf(getValue()), x + 30, y + Card.height - 53);

		//int width = g.getFontMetrics().stringWidth(attack + " / " + health);
		//g.drawString(attack + " / " + health, x + (Card.width - width) / 2, y + Card.height - 10);
	}

	public void renderMouseOver(Graphics2D g, int x, int y) {
		if (Component.mousePos != null) {
			if (Component.mousePos.x > x && Component.mousePos.y > y && Component.mousePos.x < x + Card.width && Component.mousePos.y < y + Card.height && reference.image != null) {
				g.drawImage(reference.image, x + Card.width + 30, y - (int) (Card.height * .1), (int) (Card.width * 1.2), (int) (Card.height * 1.2), null);
			}
		}
	}

	public int compareTo(Minion m) {
		if (getValue() > m.getValue()) {
			return 1;
		} else if (getValue() < m.getValue()) {
			return -1;
		} else {
			return 0;
		}
	}
	// Assumed to be less than ~50.

}
