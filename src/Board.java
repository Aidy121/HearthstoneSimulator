// Author: Aidan Fisher

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Board implements Serializable, Cloneable {
	private static final long serialVersionUID = -2462594028605618459L;

	public static int cardDisplayHeight = Card.height + 20;
	public static int heroDisplayHeight = 30;
	public static int minionDisplayHeight = Card.height + 10;

	public int turn = 0;

	public ArrayList<ArrayList<Minion>> minions = new ArrayList<ArrayList<Minion>>();
	public ArrayList<ArrayList<Card>> cards = new ArrayList<ArrayList<Card>>();
	public ArrayList<ArrayList<Card>> deck = new ArrayList<ArrayList<Card>>();
	public Minion[] hero = new Minion[2];
	public Weapon[] weapon = new Weapon[2];
	public Card[] heroAbility = new Card[2];
	public boolean heroAbilityUsed = false;
	public int[] mana = new int[2];
	public int[] manaLeft = new int[2];
	public int[] fatigueCounter = new int[2];
	public String[] deckName = new String[2];

	public int toBeSummonedLocation;

	public ArrayList<ArrayList<GameAction>> actionsThisGame = new ArrayList<ArrayList<GameAction>>();

	public static int numOcc = 0;

	public Board() {
		// New game:
		for (int p = 0; p < 2; p++) {
			actionsThisGame.add(new ArrayList<GameAction>());

			minions.add(new ArrayList<Minion>());
			cards.add(new ArrayList<Card>());
			deck.add(new ArrayList<Card>());
			/*for (int i = 0; i < 30; i++) {
				deck.get(p).add(Component.cards.get(new Random().nextInt(Component.cards.size() - 1) + 1).clone());
				//deck.get(p).add(new Card(new Minion(new Random().nextInt(5) + 1, new Random().nextInt(5) + 1), new Random().nextInt(4) + 1));
				//deck.get(p).get(i).minion.reference = deck.get(p).get(i);
			}*/

			mana[p] = 0;
			manaLeft[p] = 0;
			fatigueCounter[p] = 0;
			setDeck(p, "Mech Mage");
			shuffle(p);
		}
		cards.get(0).add(Component.cards.get(0));
		drawCard(0);
		drawCard(0);
		drawCard(0);
		drawCard(0);
		drawCard(1);
		drawCard(1);
		drawCard(1);
		// Mulligan phase.

		// Go to first player:
		doTurn(Math.abs(turn - 1));
	}

	public void setDeck(int p, String name) {
		deckName[p] = name;
		if (name.contains("Mage")) {
			Card card = new Card("Mage", new Minion(0, 30), 0);
			card.minion.reference = card;
			hero[p] = card.minion;

			heroAbility[p] = findOriginalCard("Fireblast").clone();

		}
		if (name.equals("Mech Mage")) {
			deck.get(p).add(findOriginalCard("Cogmaster").clone());
			deck.get(p).add(findOriginalCard("Cogmaster").clone());
			deck.get(p).add(findOriginalCard("Clockwork Gnome").clone());
			deck.get(p).add(findOriginalCard("Clockwork Gnome").clone());
			deck.get(p).add(findOriginalCard("Leper Gnome").clone()); // Not part of mech mage (piloted shreddr / loatheb repl.)
			deck.get(p).add(findOriginalCard("Leper Gnome").clone()); // Not part of mech mage (piloted shreddr / loatheb repl.)
			deck.get(p).add(findOriginalCard("Annoy o Tron").clone());
			deck.get(p).add(findOriginalCard("Annoy o Tron").clone());
			deck.get(p).add(findOriginalCard("Mechwarper").clone());
			deck.get(p).add(findOriginalCard("Mechwarper").clone());
			deck.get(p).add(findOriginalCard("Snow Chugger").clone());
			deck.get(p).add(findOriginalCard("Snow Chugger").clone());
			deck.get(p).add(findOriginalCard("Frostbolt").clone());
			deck.get(p).add(findOriginalCard("Frostbolt").clone());
			deck.get(p).add(findOriginalCard("Harvest Golem").clone());
			deck.get(p).add(findOriginalCard("Harvest Golem").clone());
			deck.get(p).add(findOriginalCard("Spider Tank").clone());
			deck.get(p).add(findOriginalCard("Spider Tank").clone());
			deck.get(p).add(findOriginalCard("Tinkertown Technician").clone());
			deck.get(p).add(findOriginalCard("Tinkertown Technician").clone());
			deck.get(p).add(findOriginalCard("Mechanical Yeti").clone());
			deck.get(p).add(findOriginalCard("Mechanical Yeti").clone());
			deck.get(p).add(findOriginalCard("Goblin Blastmage").clone());
			deck.get(p).add(findOriginalCard("Goblin Blastmage").clone());
			deck.get(p).add(findOriginalCard("Fireball").clone());
			deck.get(p).add(findOriginalCard("Fireball").clone());
			deck.get(p).add(findOriginalCard("Azure Drake").clone());
			deck.get(p).add(findOriginalCard("Azure Drake").clone());
			deck.get(p).add(findOriginalCard("Archmage Antonidas").clone());
			deck.get(p).add(findOriginalCard("Dr. Boom").clone());
		}
	}

	public void shuffle(int p) {
		ArrayList<Card> lastDeckList = (ArrayList<Card>) deck.get(p).clone();
		deck.get(p).clear();

		for (int i = 0; i < 30; i++) {
			int n = new Random().nextInt(lastDeckList.size());
			deck.get(p).add(lastDeckList.get(n));
			lastDeckList.remove(n);
		}
	}

	public void doTurn(int p) {
		int l = Math.abs(p - 1);
		// End of turn. All END OF TURN effects happen here:
		// Remove all attack from hero, (counts as unequiping the weapon)
		hero[l].attack = 0;
		for (int i = 0; i < minions.get(turn).size(); i++) {
			minions.get(turn).get(i).endOfTurn();
		}

		mana[p]++;
		if (mana[p] > 10) {
			mana[p] = 10;
		}
		manaLeft[p] = mana[p];
		drawCard(p);
		turn = p;
		for (int i = 0; i < minions.get(turn).size(); i++) {
			minions.get(turn).get(i).startOfTurn();
		}
		// Hero equips weapon:
		hero[p].attacksLeft = 1;
		if (weapon[p] != null) {
			hero[p].attacksLeft = weapon[p].numAttacks;
			weapon[p].equip(hero[p]);
		}
		if (hero[p].frozen) {
			hero[p].frozen = false;
			hero[p].attacksLeft = 0;
		}
		heroAbilityUsed = false;
		// Wait for player to do move.
	}

	public Card getCardObject(int card) {
		if (card == -2) { // Hero ability
			return heroAbility[turn];
		} else {
			return cards.get(turn).get(card);
		}
	}

	public void playWeapon(int card) {
		Card cardObject = getCardObject(card);
		if (manaLeft[turn] >= cardObject.getCost()) {
			// Played minion rules: (Happens BEFORE it gets to the board)
			manaLeft[turn] -= cardObject.getCost();
			if (card != -2) {
				cards.get(turn).remove(card);
			} else {
				heroAbilityUsed = true;
			}
			weapon[turn] = cardObject.weapon;
			// Equip for this turn:
			weapon[turn].equip(hero[turn]);
		}
	}

	public int getSideAll(Minion minion) {
		// This method is assumed to be proper.
		if (hero[0] == minion || minions.get(0).contains(minion)) {
			return 0;
		} else {
			return 1;
		}
	}

	public int getSideJustMinions(Minion minion) {
		if (minions.get(0).contains(minion)) {
			return 0;
		} else if (minions.get(1).contains(minion)) {
			return 1; // Assumed to be proper
		} else {
			//System.out.println("Side Issue");
			return 2;
		}
	}

	public void playMinion(int where, int card) {
		Card cardObject = getCardObject(card);
		if (minions.get(turn).size() < 7 && manaLeft[turn] >= cardObject.getCost()) { // This is the check for playing a minion..
			// Played minion rules: (Happens BEFORE it gets to the board)
			manaLeft[turn] -= cardObject.getCost();
			if (card != -2) {
				cards.get(turn).remove(card);
			} else {
				heroAbilityUsed = true;
			}
			summonMinion(turn, where, cardObject.minion, true);
		}
	}

	public int getApproximateBoardValue(int player) {
		double total = 0;
		for (Minion minion : Component.board.minions.get(player)) {
			total += minion.getValue();
		}
		return (int) (total / 15);
	}

	public void summonMinion(int player, int where, Minion minion, boolean played) {
		if (Component.board.minions.get(player).size() < 7) { // The check for summoning a minion, (it clashes)
			if (where <= Component.board.toBeSummonedLocation && player == Component.board.turn) {
				Component.board.toBeSummonedLocation++;
			}
			for (int i = 0; i < minions.get(player).size(); i++) {
				minions.get(player).get(i).minionSummoned(minion);
			}
			if (played) {
				toBeSummonedLocation = where;
				minion.battleCry();
				where = toBeSummonedLocation;
			}
			minions.get(player).add(where, minion);

			// Buff updates:
			for (int i = 0; i < minions.get(turn).size(); i++) {
				minions.get(turn).get(i).updateBuffBonus();
			}
		}
	}

	public void playSpell(int card, Minion otherMinion) {
		Card cardObject = getCardObject(card);
		if (manaLeft[turn] >= cardObject.getCost()) {
			if (!cardObject.minionsOnly || (otherMinion != hero[0] && otherMinion != hero[1])) {
				manaLeft[turn] -= cardObject.getCost();
				if (card != -2) {
					cards.get(turn).remove(card);
				} else {
					heroAbilityUsed = true;
				}
				// Assume valid target for now
				cardObject.play(otherMinion);

				if (card != -2) { // Hero ability does not count as spell
					// Casted spell rules:
					for (int i = 0; i < minions.get(turn).size(); i++) {
						minions.get(turn).get(i).playerCastedSpell();
					}
				}
			}
		}
	}

	/** Returns true if it needs to "clean up" again. */
	public boolean cleanUp() {
		// Calculate minion deaths:
		boolean cleanUpToBeDoneAgain = false;
		for (int p = 0; p < 2; p++) {
			ArrayList<Minion> clonedMinions = (ArrayList<Minion>) minions.get(p).clone();
			for (int i = 0; i < clonedMinions.size(); i++) {
				if (clonedMinions.get(i).cleanUp()) {
					cleanUpToBeDoneAgain = true;
				}
			}
			if (hero[p].cleanUp()) {
				cleanUpToBeDoneAgain = false; // Heroes, when dead, just end the game.
				break; // No ties.
			}
		}

		// Cleanup should handle the "played minions" stuff as well. (After they have been "summoned")
		return cleanUpToBeDoneAgain;
	}

	public void drawCard(int p) {
		if (deck.get(p).size() != 0) {
			if (cards.get(p).size() < 10) {
				cards.get(p).add(deck.get(p).get(0));
			}
			deck.get(p).remove(0); // Remove the card from the deck, (whether it was drawn or not)
		} else {
			fatigueCounter[p]++;
			// Take fatigue damage.
			hero[p].damage(fatigueCounter[p], false);
		}
	}

	public static Card findOriginalCard(Card card) {
		for (int i = 0; i < Component.cards.size(); i++) {
			if (card.name.equals(Component.cards.get(i).name)) { // It is important that no 2 cards have the same names
				return Component.cards.get(i);
			}
		}
		for (int i = 0; i < Component.unlistedCards.size(); i++) {
			if (card.name.equals(Component.unlistedCards.get(i).name)) { // It is important that no 2 cards have the same names
				return Component.unlistedCards.get(i);
			}
		}
		return null;
	}

	/** This can easily be made more efficient, (hashmap) */
	public static Card findOriginalCard(String card) {
		for (int i = 0; i < Component.unlistedCards.size(); i++) {
			if (card.equals(Component.unlistedCards.get(i).name)) { // It is important that no 2 cards have the same names
				return Component.unlistedCards.get(i);
			}
		}
		for (int i = 0; i < Component.cards.size(); i++) {
			if (card.equals(Component.cards.get(i).name)) { // It is important that no 2 cards have the same names
				return Component.cards.get(i);
			}
		}
		for (int i = 0; i < Component.spareParts.size(); i++) {
			if (card.equals(Component.spareParts.get(i).name)) { // It is important that no 2 cards have the same names
				return Component.spareParts.get(i);
			}
		}
		for (int i = 0; i < Component.heroPowers.size(); i++) {
			if (card.equals(Component.heroPowers.get(i).name)) { // It is important that no 2 cards have the same names
				return Component.heroPowers.get(i);
			}
		}
		return null;
	}

	public void addCard(int p, Card card) {
		if (cards.get(p).size() < 10) {
			cards.get(p).add(card.clone());
		}
	}

	public Object clone() {

		try {
			return super.clone();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void renderClass(Graphics2D g, int p) {
		int height = getHerosHeight(p);
		g.setFont(new Font("Arial", Font.PLAIN, heroDisplayHeight - 5));
		if (p != turn) {
			g.setColor(new Color(0, 0, 0));
		} else {
			g.setColor(new Color(0, 128, 0));
		}
		g.drawString(hero[p].reference.name + ": " + hero[p].attack + " / " + hero[p].health + "  Mana: " + manaLeft[p] + " / " + mana[p], 15, height + heroDisplayHeight - 2);
		if (heroAbility[p].image == null) {
			heroAbility[p].setHeroAbilityImage();
		}
		g.drawImage(heroAbility[p].image, 400, height - heroDisplayHeight / 2, (int) (heroDisplayHeight * 1.35), heroDisplayHeight * 2, null);
	}

	public int getCardsHeight(int p) {
		if (p == 0) {
			return (cardDisplayHeight - Card.height) / 2;
		} else {
			return cardDisplayHeight + heroDisplayHeight * 2 + minionDisplayHeight * 2 + (cardDisplayHeight - Card.height) / 2;
		}
	}

	public int getHerosHeight(int p) {
		if (p == 0) {
			return cardDisplayHeight;
		} else {
			return cardDisplayHeight + heroDisplayHeight + minionDisplayHeight * 2;
		}
	}

	public int getMinionsHeight(int p) {
		if (p == 0) {
			return cardDisplayHeight + heroDisplayHeight + (minionDisplayHeight - Card.height);
		} else {
			return cardDisplayHeight + heroDisplayHeight + minionDisplayHeight;
		}
	}

	public void renderCards(Graphics2D g, int p) {
		int height = getCardsHeight(p);
		for (int i = 0; i < cards.get(p).size(); i++) {
			if (p == turn && i == Listening.cardToBePlayed && Component.mousePos != null) { // Don't bother diplay this card

			} else {
				cards.get(p).get(i).render(g, i * Card.handWidth, height);
			}
		}
		// Rerender the card that's being mouse overed:
		if (Component.mousePos != null) {
			if (p == turn && Listening.cardToBePlayed != -1) {
				if (Listening.cardToBePlayed == -2) {
					heroAbility[p].render(g, Component.mousePos.x - Card.width / 2, Component.mousePos.y - Card.height / 2);
				} else {
					cards.get(p).get(Listening.cardToBePlayed).render(g, Component.mousePos.x - Card.width / 2, Component.mousePos.y - Card.height / 2);
				}
			} else {
				if (Component.mousePos.y > getCardsHeight(p) && Component.mousePos.y < getCardsHeight(p) + Card.height) {
					int n = Component.mousePos.x / Card.handWidth;
					if (n >= 0 && n < cards.get(p).size()) {
						cards.get(p).get(n).render(g, n * Card.handWidth, height);
					}
				}
			}
		}
	}

	public void renderMinions(Graphics2D g, int p) {
		int height = getMinionsHeight(p);
		for (int i = 0; i < minions.get(p).size(); i++) {
			minions.get(p).get(i).render(g, i * Card.width, height);
		}
	}

	public void renderLines(Graphics2D g, int p) {
		int height = getMinionsHeight(p);
		g.setStroke(new BasicStroke(3));
		for (int i = 0; i < minions.get(p).size(); i++) {
			if (p == turn && Listening.minionSelectedLocation == i && Component.mousePos != null) {
				g.setColor(new Color(255, 0, 0));
				g.drawLine(i * Card.width + Card.width / 2, height + Card.height / 2, Component.mousePos.x, Component.mousePos.y);
			}
		}
		if (p == turn && Listening.minionAttacking == hero[turn] && Component.mousePos != null) {
			g.setColor(new Color(255, 0, 0));
			g.drawLine(50, getHerosHeight(p) + heroDisplayHeight / 2, Component.mousePos.x, Component.mousePos.y);
		}
	}

	public void renderMouseOver(Graphics2D g, int p) {
		int height = getMinionsHeight(p);
		for (int i = 0; i < minions.get(p).size(); i++) {
			minions.get(p).get(i).renderMouseOver(g, i * Card.width, height);
		}

	}

	public void render(Graphics2D g) {
		// Display classes:
		renderClass(g, 0);
		renderClass(g, 1);
		renderCards(g, 0);
		renderCards(g, 1);
		renderMinions(g, 0);
		renderMinions(g, 1);
		renderLines(g, 0);
		renderLines(g, 1);
		renderMouseOver(g, 0);
		renderMouseOver(g, 1);

		if (Listening.battlecryBuff != null && Component.mousePos != null) {
			Listening.battlecryBuff.render(g, Component.mousePos.x - Card.width / 2, Component.mousePos.y - Card.height / 2);
		}
	}
}
