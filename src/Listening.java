// Author: Aidan Fisher

import java.awt.event.*;
import java.awt.*;

public class Listening implements MouseListener, MouseWheelListener {
	public static boolean mouse1Down = false;
	public static Point pressLocation = new Point(0, 0);
	public static Point currLocation = new Point(0, 0);
	public static Point lastLocation = new Point(0, 0);

	public static int minionSelectedLocation = -1;
	public static Minion minionAttacking = null;
	public static Minion minionDefending = null;
	public static int cardToBePlayed = -1;

	public static Card battlecryBuff = null; // For battlecries. It is actually the minion, but should be treated as a spell

	public static boolean mouseReleasedLastTick = false;

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (Component.mousePos != null) {
			int x = (Component.mousePos.x - Card.width * 7 + 40) / ProfileUI.length;
			if (x >= 1 && x < GameSet.currentGameSet.numDimensions) {
				if (e.getWheelRotation() > 0) {
					ProfileUI.changeAccess(x, ProfileUI.access[x] + 1);
				} else if (e.getWheelRotation() < 0) {
					ProfileUI.changeAccess(x, ProfileUI.access[x] - 1);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public static void cancel() {
		minionSelectedLocation = -1;
		minionAttacking = null;
		minionDefending = null;
		cardToBePlayed = -1;
	}

	public void mousePressed(MouseEvent e) {
		pressLocation = e.getPoint();
		if (e.getButton() == 3) {
			// Cancel all actions:
			cancel();
		} else if (e.getButton() == 1) {
			mouse1Down = true;
			// See if card is there:
			if (Listening.battlecryBuff == null) {
				Board board = Component.board;
				if (pressLocation.y > board.getCardsHeight(board.turn) && pressLocation.y < board.getCardsHeight(board.turn) + Card.height) {
					int n = pressLocation.x / Card.handWidth;
					if (n >= board.cards.get(board.turn).size()) {
						int amount = pressLocation.x - Card.handWidth * (board.cards.get(board.turn).size() - 1);
						if (amount < Card.width) {
							n = board.cards.get(board.turn).size() - 1;
						}
					}
					if (n >= 0 && n < board.cards.get(board.turn).size()) {
						cardToBePlayed = n;
						// card is "being held"
					}
				}

				if (pressLocation.y > board.getMinionsHeight(board.turn) && pressLocation.y < board.getMinionsHeight(board.turn) + Card.height) {
					int n = pressLocation.x / Card.width;
					if (n >= 0 && n < board.minions.get(board.turn).size()) {
						Minion minion = board.minions.get(board.turn).get(n);
						if (minion.canAttack()) {
							Listening.minionSelectedLocation = n;
						}
					}
				}

				if (pressLocation.y > board.getHerosHeight(board.turn) && pressLocation.y < board.getHerosHeight(board.turn) + Board.heroDisplayHeight) {
					// Either hero power or hero attack/weapon.
					if (pressLocation.x > 400 && pressLocation.x < 400 + (int) (Board.heroDisplayHeight * 1.35) && !board.heroAbilityUsed) {
						// Hero ability:
						cardToBePlayed = -2;
					} else {
						if (board.hero[board.turn].canAttack()) {
							Listening.minionAttacking = board.hero[board.turn]; // Reserved for hero
						}
					}
				}
			}

			if (Component.mousePos != null) {
				int x = (Component.mousePos.x - Card.width * 7 + 40) / ProfileUI.length;
				if (x >= 1 && x < GameSet.currentGameSet.numDimensions) {
					ProfileUI.overrideAll[x] = !ProfileUI.overrideAll[x];
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == 3) {
		} else if (e.getButton() == 1) {
			if (Listening.cardToBePlayed != -1 || Listening.battlecryBuff != null) { // This gets priority.
				mouseReleasedLastTick = true;
				// Play card, (if in the playing zone)

			} else if (Listening.minionSelectedLocation != -1 || minionAttacking != null && minionDefending == null) {
				int turn = Math.abs(Component.board.turn - 1);
				// See if it was let down on another minion:
				if (e.getY() > Component.board.getMinionsHeight(turn) && e.getY() < Component.board.getMinionsHeight(turn) + Card.height) {
					int n = e.getX() / Card.width;
					if (n >= 0 && n < Component.board.minions.get(turn).size()) {
						Minion minion = Component.board.minions.get(turn).get(n);

						if (minionAttacking == null) {
							minionAttacking = Component.board.minions.get(Component.board.turn).get(Listening.minionSelectedLocation);
						}
						minionDefending = minion;
						// Attack happens next tick
					}
				} else if (e.getY() > Component.board.getHerosHeight(turn) && e.getY() < Component.board.getHerosHeight(turn) + Board.heroDisplayHeight) {
					// Attack the hero:
					if (minionAttacking == null) {
						minionAttacking = Component.board.minions.get(Component.board.turn).get(Listening.minionSelectedLocation);
					}
					minionDefending = Component.board.hero[turn];
				}
				Listening.minionSelectedLocation = -1;
			}
			mouse1Down = false;
		}
	}
}
