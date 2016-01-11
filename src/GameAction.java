// Author: Aidan Fisher

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class GameAction implements Serializable {

	public Card cardObject = null;
	public int card = -1; // Only for "PLAY_CARD"
	public Minion minion = null; // Only for "MINION_ATTACK." (Includes hero.)
	public int boardPosition = -1; // Where placing the minion on board.
	public Minion target = null; // The target. (Used for targeted spells, targeted battlecries, and minion attacks)
	public boolean wasTargetEnemy;
	public boolean wasTargetHero;
	public int healthOpponentHero;
	public int healthFriendlyHero;
	public int opponentBoardValue; // rounded.
	public int friendlyBoardValue; // rounded.

	public GameAction() {
		/** Define Board State */
		healthOpponentHero = Component.board.hero[Math.abs(Component.board.turn - 1)].health;
		healthFriendlyHero = Component.board.hero[Component.board.turn].health;
		opponentBoardValue = Component.board.getApproximateBoardValue(Math.abs(Component.board.turn - 1)); //  (/15)
		friendlyBoardValue = Component.board.getApproximateBoardValue(Component.board.turn);
	}

	// All playing cards:
	public GameAction(int card, int boardPos) {
		this();
		this.card = card;
		this.cardObject = Component.board.getCardObject(card);
		this.boardPosition = boardPos;
	}

	public GameAction(int card) {
		this();
		this.card = card;
		this.cardObject = Component.board.getCardObject(card);
	}

	public GameAction(int card, Minion target) {
		this();
		this.card = card;
		this.cardObject = Component.board.getCardObject(card);
		this.target = target;
		this.wasTargetHero = Component.board.getSideJustMinions(target) == 2;
		this.wasTargetEnemy = Component.board.getSideAll(target) != Component.board.turn; // As the person who makes the gameaction, it is their turn.
	}

	// Minion attacking.
	public GameAction(Minion attacker, Minion opponent) {
		this();
		this.minion = attacker; // This and
		this.target = opponent; // This are cloned after the gameAction is done.
		this.wasTargetHero = Component.board.getSideJustMinions(target) == 2;
		this.wasTargetEnemy = Component.board.getSideAll(target) != Component.board.turn; // As the person who makes the gameaction, it is their turn.
	}

	public static ArrayList<Double> getAllRatings(ArrayList<GameAction> actions) {
		ArrayList<Double> ratings = new ArrayList<Double>();
		double currentTotal = 0;
		for (int i = 0; i < actions.size(); i++) {
			currentTotal += actions.get(i).getRating();
			ratings.add(currentTotal);
		}
		return ratings;
	}

	public static GameAction getBestAction(ArrayList<GameAction> actions) {
		GameAction bestAction = null;
		double rating = -1;
		for (int i = 0; i < actions.size(); i++) {
			double compareRating = actions.get(i).getRating();
			if (compareRating > rating) {
				bestAction = actions.get(i);
				rating = compareRating;
			}
		}
		//System.out.println(bestAction.getRating());
		return bestAction;
	}

	public static ArrayList<GameAction> getAllActions(int p) {
		ArrayList<GameAction> actions = new ArrayList<GameAction>(100);
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.addAll(Component.board.cards.get(p));
		int heroAbilityIndex = -1;
		if (!Component.board.heroAbilityUsed) {
			heroAbilityIndex = cards.size();
			cards.add(Component.board.heroAbility[p]);
		}
		for (int n = 0; n < cards.size(); n++) {
			if (cards.get(n).getCost() <= Component.board.manaLeft[p]) {
				// Weapons, Non-targeted Spells
				// Targeted Spells
				// Minions. (placement; also druid types) 
				// Minion battlecries!
				if (cards.get(n).minion != null) {
					if (Component.board.minions.get(p).size() < 7) {
						for (int i = 0; i < Component.board.minions.get(p).size() + 1; i++) {
							if (cards.get(n).targetedBattleCry) { // If there is a targeted battlecry.
							} else {
								actions.add(new GameAction(n == heroAbilityIndex ? -2 : n, i));
							}
						}
					}
				} else if (cards.get(n).weapon != null) {
					actions.add(new GameAction(n == heroAbilityIndex ? -2 : n));
				} else {
					// It's a spell:
					if (cards.get(n).targeted) { // Find target
						if (!cards.get(n).minionsOnly) {
							if (!cards.get(n).friendliesOnly) {
								actions.add(new GameAction(n == heroAbilityIndex ? -2 : n, Component.board.hero[Math.abs(p - 1)]));
							}
							if (!cards.get(n).enemiesOnly) {
								actions.add(new GameAction(n == heroAbilityIndex ? -2 : n, Component.board.hero[p]));
							}
						}
						if (!cards.get(n).friendliesOnly) {
							for (Minion target : Component.board.minions.get(Math.abs(p - 1))) {
								if (target.spellTargetable(false)) {
									actions.add(new GameAction(n == heroAbilityIndex ? -2 : n, target));
								}
							}
						}
						if (!cards.get(n).enemiesOnly) {
							for (Minion target : Component.board.minions.get(p)) {
								if (target.spellTargetable(false)) {
									actions.add(new GameAction(n == heroAbilityIndex ? -2 : n, target));
								}
							}
						}
					} else {
						actions.add(new GameAction(n == heroAbilityIndex ? -2 : n));
					}
				}
			}
		}
		ArrayList<Minion> minions = new ArrayList<Minion>();
		minions.addAll(Component.board.minions.get(p));
		minions.add(Component.board.hero[p]);
		ArrayList<Minion> opponentMinions = new ArrayList<Minion>();
		opponentMinions.addAll(Component.board.minions.get(Math.abs(p - 1)));
		opponentMinions.add(Component.board.hero[Math.abs(p - 1)]);
		for (int i = 0; i < opponentMinions.size(); i++) {
			if (!Minion.canAttack(opponentMinions.get(i))) {
				opponentMinions.remove(i);
				i--;
			}
		}
		for (Minion minion : minions) {
			if (minion.canAttack()) {
				for (Minion opponentMinion : opponentMinions) {
					actions.add(new GameAction(minion, opponentMinion));
				}
			}
		}
		return actions;
	}

	public double getRating() {
		getProfile(false); // To get GameSet.lastAccess's value.

		// Based upon last game set

		double rating = 0;
		double total = 0;

		// There are settings within gameSet that are used for this:
		int[] mainAccess = GameSet.lastAccess; // This should be the only thing using access.
		for (int i = 0; i < GameSet.profileChecks.size(); i++) {
			float accessDifference = GameSet.profileDifferences.get(i);
			Profile profile = GameSet.lastGameSet.getProfile(getActualAccess(mainAccess, GameSet.profileChecks.get(i)), false);
			if (profile == null) {
				continue;
			}
			rating += profile.rating * (1 - accessDifference / (double) GameSet.maxDifferenceValue);
			total += profile.totalCases;
			if (total >= GameSet.numRequired) {
				break;
			}
		}
		if (total <= GameSet.numRequired / 4) { // Just leave it to RNG.
			return 1;
		}
		// Rating
		if (rating / total > 0) {
			if (rating / total >= 0.999) {
				return 1000; // Max rating. (to avoid infinity)
			}
			return 1 / (double) (1 - rating / total);
		} else {
			return 1 + rating / total;
		}
	}

	private static int[] getActualAccess(int[] mainAccess, int[] accessModification) {
		int[] actualAccess = Arrays.copyOf(mainAccess, mainAccess.length);
		for (int i = 0; i < accessModification.length; i++) {
			actualAccess[GameSet.priority[i]] = mainAccess[GameSet.priority[i]] + accessModification[i];
		}
		return actualAccess;
	}

	public Profile getProfile(boolean currentGameSet) {
		String name = "";
		if (this.cardObject != null) {
			name = this.cardObject.name;
		}
		if (currentGameSet) {
			return GameSet.currentGameSet.getProfile(name, this.target, this.wasTargetHero, this.wasTargetEnemy, this.healthOpponentHero, this.healthFriendlyHero, this.opponentBoardValue,
					this.friendlyBoardValue, this.minion);
		} else {
			return GameSet.lastGameSet.getProfile(name, this.target, this.wasTargetHero, this.wasTargetEnemy, this.healthOpponentHero, this.healthFriendlyHero, this.opponentBoardValue,
					this.friendlyBoardValue, this.minion);
		}
	}

	public void doAction() {
		if (this.card != -1) {
			Card card;
			if (this.card == -2) { // Hero Ability
				card = Component.board.heroAbility[Component.board.turn];
			} else {
				card = Component.board.cards.get(Component.board.turn).get(this.card);
			}
			if (boardPosition != -1) { // t'is a minion.
				Component.board.playMinion(boardPosition, this.card);
			} else if (card.weapon != null) {
				Component.board.playWeapon(this.card);
			} else {
				if (this.target != null) {
					Minion target = this.target;
					this.target = target.clone(); // This is only necessary if this is going to be used, and it will only be used if the action is run.
					Component.board.playSpell(this.card, target);
				} else {
					Component.board.playSpell(this.card, null);
				}
			}
		} else { // card = -1, minion attacking.
			// Minion attacking:
			Minion target = this.target;
			this.target = target.clone();
			Minion minion = this.minion;
			this.minion = minion.clone();
			minion.attack(target);
		}
		while (Component.board.cleanUp()) {

		}
	}
	// Opponent's cards. (and Hero Power)
	// Opponent's hero. 
	// Opponent's minions.
	// Player's minions
	// Player's hero.
	// Player's cards. (and Hero Power)

}
