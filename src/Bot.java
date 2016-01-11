// Author: Aidan Fisher

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Bot {
	public int player;
	public int p2;

	public Bot(Board board, int player) {
		this.player = player;
		this.p2 = Math.abs(player - 1);
	}

	public void playTurn() {
		ArrayList<ArrayList<Card>> cardList = new ArrayList<ArrayList<Card>>();
		// For each card, see if playable. (Including hero ability)
		// *ALWAYS* uses hero ability if has 2 mana for now
		for (int n = 1; n <= Component.board.cards.get(player).size(); n++) {
			int[] currentValue = new int[n];
			for (int i = 0; i < n; i++) {
				currentValue[i] = i;
			}
			setValues(currentValue, Component.board.cards.get(player).size(), cardList);
		}

		// For now, order is not considered. Minions / spells are always played first, for now.
		int bestPlay = -1;
		double stats = 0;
		for (int i = 0; i < cardList.size(); i++) {
			int totalMana = 0;
			double totalStats = 0;
			for (int n = 0; n < cardList.get(i).size(); n++) {
				totalMana += cardList.get(i).get(n).mana;
				if (cardList.get(i).get(n).minion == null) {
					totalStats = -1;
					break;
				} else {
					totalStats += cardList.get(i).get(n).minion.getValue();
				}
			}
			if (totalMana <= Component.board.mana[player] && totalStats > stats) {
				stats = totalStats;
				bestPlay = i;
			}
		}

		// Play the cards.
		if (bestPlay != -1) {
			for (int i = 0; i < cardList.get(bestPlay).size(); i++) {
				//System.out.println(Component.board.cards.get(player).indexOf(cardList.get(bestPlay).get(i)));
				Component.board.playMinion(0, Component.board.cards.get(player).indexOf(cardList.get(bestPlay).get(i)));
			}
		}

		// Evaluate trades:
		for (int e = 0; e < Component.board.minions.get(p2).size(); e++) {
			ArrayList<ArrayList<Integer>> minionList = new ArrayList<ArrayList<Integer>>();
			// For each card, see if playable. (Including hero ability)
			// *ALWAYS* uses hero ability if has 2 mana for now
			for (int n = 1; n <= Component.board.minions.get(player).size(); n++) {
				int[] currentValue = new int[n];
				for (int i = 0; i < n; i++) {
					currentValue[i] = i;
				}
				setMinionValues(currentValue, Component.board.minions.get(player).size(), minionList);
			}
			Minion enemy = Component.board.minions.get(p2).get(e);
			if (Minion.canAttack(enemy)) {
				boolean died;
				if (enemy.taunt) {
					died = attackEnemy(0, e, minionList);
				} else {
					if (Component.board.turn == 0) {
						died = attackEnemy(0.6, e, minionList);
					} else {
						died = attackEnemy(1.1, e, minionList);
					}
				}
				if (died) {
					e--;
				}
			}

		}
		// Hero:
		Minion enemy = Component.board.hero[p2];
		if (Minion.canAttack(enemy)) {
			// Remainder hit:
			for (int i = 0; i < Component.board.minions.get(player).size(); i++) {
				if (Component.board.minions.get(player).get(i).canAttack()) {
					Component.board.minions.get(player).get(i).attack(enemy);
				}
			}
		}

		// Do attacks:
		/*for (int i = 0; i < board.minions.get(player).size(); i++) {
			if (board.minions.get(player).get(i).canAttack()) {
				if (board.minions.get(p2).size() > 0) {
					board.minions.get(player).get(i).attack(board.minions.get(p2).get(0));
				} else {
					board.minions.get(player).get(i).attack(board.hero[p2]);
				}
			}
		}*/
		if (Component.gameOver) {
			Component.wins[Component.lastWinner]++;
			if (Component.wins[0] >= 100 || Component.wins[1] >= 100) {
				return;
			}
			Component.gameOver = false;
			Component.board = new Board();
		}
		Component.board.doTurn(Math.abs(Component.board.turn - 1));
		//Bot bot = new Bot(Component.board, Component.board.turn);
		//bot.playTurn();
	}

	public Minion getMinion(int i) {
		return Component.board.minions.get(Component.board.turn).get(i);
	}

	private boolean attackEnemy(double minTradeValue, int enemyInt, ArrayList<ArrayList<Integer>> minionList) {

		try {

			FileOutputStream fout = new FileOutputStream("temp.hs");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(Component.board);
			oos.close();
			//System.out.println("Done");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		boolean overwritten = false;
		double enemyValue = Component.board.minions.get(Math.abs(Component.board.turn - 1)).get(enemyInt).getValue();
		boolean didDie = false;
		for (int i = 0; i < minionList.size(); i++) {
			// Find enemy minion.
			Minion enemy = Component.board.minions.get(Math.abs(Component.board.turn - 1)).get(enemyInt);
			boolean died = false;
			ArrayList<Minion> minionListSave = new ArrayList<Minion>();
			for (int t = 0; t < minionList.get(i).size(); t++) {
				minionListSave.add(getMinion(minionList.get(i).get(t)));
			}

			// see what
			int totalValue = 0;
			for (int j = 0; j < minionList.get(i).size(); j++) {
				totalValue += getMinion(minionList.get(i).get(j)).getValue();
			}

			int numMinionsUsed = 0;
			// Simulate attack: (one order for now)
			for (int j = 0; j < minionListSave.size(); j++) {
				if (!minionListSave.get(j).isDead) {
					if (enemy.isDead) {
						System.out.println("der" + enemy.reference.name);
					} else {
						minionListSave.get(j).attack(enemy);
						if (enemy.isDead) {
							died = true;
							numMinionsUsed = j + 1;
							break;
						}
					}
				} else {
					System.out.println("IT MATTERED");
				}
			}

			// See how it ended up
			if (numMinionsUsed != minionList.get(i).size() && numMinionsUsed != 0) {
				// This is an anomoly, and *should* have been considered elsewhere, (*CAN BE AN ISSUE, (ex: boom bot))

			} else {
				// Evaluate the trade
				double stats2 = enemyValue - enemy.getValue();
				double remainingStats = 0;
				for (int j = 0; j < minionListSave.size(); j++) {
					remainingStats += minionListSave.get(j).getValue();
				}
				double stats1 = totalValue - remainingStats;

				if (stats1 == 0) {
					// Minor consideration
					stats1 = 1;
				}

				double tradeValue = stats2 / (double) stats1;

				if (tradeValue > minTradeValue) {
					minTradeValue = tradeValue;
					// Save this board state:
					overwritten = true;
					didDie = died;
					try {

						FileOutputStream fout = new FileOutputStream("best.hs");
						ObjectOutputStream oos = new ObjectOutputStream(fout);
						oos.writeObject(Component.board);
						oos.close();
						//System.out.println("Done");

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
			File file = new File("temp.hs");
			if (file.exists()) {
				try {
					FileInputStream fin = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(fin);
					Component.board = (Board) ois.readObject();
					ois.close();
					// For every card.. image has to be set

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		File file = new File("best.hs");
		if (file.exists() && overwritten) {
			try {
				FileInputStream fin = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fin);
				Component.board = (Board) ois.readObject();
				ois.close();
				// For every card.. image has to be set

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return didDie;
	}

	private void setMinionValues(int[] currentValue, int size, ArrayList<ArrayList<Integer>> minionList) {
		minionList.add(new ArrayList<Integer>());
		for (int n = 0; n < currentValue.length; n++) {
			minionList.get(minionList.size() - 1).add(currentValue[n]);
			if (!Component.board.minions.get(player).get(currentValue[n]).canAttack()) {
				minionList.remove(minionList.size() - 1);
				break;
			}
		}
		for (int i = currentValue.length - 1; i >= 0; i--) {
			if (currentValue[i] != i + (size - currentValue.length)) {
				currentValue[i]++;
				setMinionValues(currentValue, size, minionList);
				return;
			}
		}
	}

	private void setValues(int[] currentValue, int size, ArrayList<ArrayList<Card>> cardList) {
		cardList.add(new ArrayList<Card>());
		for (int n = 0; n < currentValue.length; n++) {
			cardList.get(cardList.size() - 1).add(Component.board.cards.get(player).get(currentValue[n]));
		}
		for (int i = currentValue.length - 1; i >= 0; i--) {
			if (currentValue[i] != i + (size - currentValue.length)) {
				currentValue[i]++;
				setValues(currentValue, size, cardList);
				return;
			}
		}
	}
}
