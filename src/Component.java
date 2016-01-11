// Author: Aidan Fisher

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.applet.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Component extends JPanel implements Runnable, ActionListener {
	private static final long serialVersionUID = 1L;
	boolean isRunning = false;
	int ticksPerSecond = 60;
	public static Point screenPos = new Point(0, 0);
	public static Point mousePos = new Point(0, 0);
	public Image screen;
	public Image screen2;
	public static Board board;

	public static JButton openButton, nextTurnButton, activateRandomButton, revertLastButton;
	public static JFileChooser fc;

	public static int screenW = 1800;
	public static int screenH = Board.cardDisplayHeight * 2 + Board.heroDisplayHeight * 2 + Board.minionDisplayHeight * 2;

	public static int saveNumber = 0;

	public static ArrayList<Card> cards = new ArrayList<Card>();
	public static ArrayList<Card> spareParts = new ArrayList<Card>();
	public static ArrayList<Card> unlistedCards = new ArrayList<Card>();
	public static ArrayList<Card> heroPowers = new ArrayList<Card>();

	public static int[] wins = new int[2];
	public static int lastWinner = 0;

	public static boolean gameOver = false;

	public static Board lastBoard = null;

	public Component() {
		setPreferredSize(new Dimension(screenW, screenH));
		//addKeyListener(new Listening());
		addMouseListener(new Listening());
		addMouseWheelListener(new Listening());

	}

	public void defineCards() {
		try {
			Card card = new Card("Coin", false, -1); // The giving mana trick
			card.image = ImageIO.read(new File("res/coin.png"));
			cards.add(card);

			card = new Card("Annoy o Tron", new Minion(1, 2), 2);
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.minion.taunt = true;
			card.minion.divineShield = true;
			card.image = ImageIO.read(new File("res/annoy-o-tron.png"));
			cards.add(card);

			card = new Card("Arcane Golem", new Minion(4, 2), 3);
			card.minion.type.add("Battlecry");
			card.minion.reference = card;
			card.minion.attacksLeft = 1; // Charge
			card.image = ImageIO.read(new File("res/arcane-golem.png"));
			cards.add(card);

			card = new Card("Arcane Intellect", false, 3);
			card.drawCards = 2;
			card.image = ImageIO.read(new File("res/arcane-intellect.png"));
			cards.add(card);

			card = new Card("Arcane Missiles", false, 1);
			card.damage = 3;
			card.randomlySplit = true;
			card.enemiesOnly = true;
			card.image = ImageIO.read(new File("res/arcane-missiles.png"));
			cards.add(card);

			card = new Card("Archmage Antonidas", new Minion(5, 7), 7);
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/archmage-antonidas.png"));
			cards.add(card);

			card = new Card("Azure Drake", new Minion(4, 4), 5);
			card.minion.type.add("Battlecry");
			card.minion.reference = card;
			card.minion.spellDamage = 1;
			card.drawCards = 1;
			card.image = ImageIO.read(new File("res/azure-drake.png"));
			cards.add(card);

			card = new Card("Chillwind Yeti", new Minion(4, 5), 4);
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/chillwind-yeti.png"));
			cards.add(card);

			card = new Card("Clockwork Gnome", new Minion(2, 1), 1);
			card.minion.type.add("Deathrattle");
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/clockwork-gnome.png"));
			cards.add(card);

			card = new Card("Cogmaster", new Minion(1, 2), 1);
			card.minion.reference = card;
			card.minion.attackGain = 2;
			card.image = ImageIO.read(new File("res/cogmaster.png"));
			cards.add(card);

			card = new Card("Dark Iron Dwarf", new Minion(4, 4), 4);
			card.minion.type.add("Battlecry");
			card.attackBuffThisTurn = 2;
			card.targeted = true;
			card.minionsOnly = true;
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/dark-iron-dwarf.png"));
			cards.add(card);

			card = new Card("Dr. Boom", new Minion(7, 7), 7);
			card.minion.type.add("Battlecry");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/dr-boom.png"));
			cards.add(card);

			card = new Card("Fiery War Axe", 3, 2, 2);
			card.image = ImageIO.read(new File("res/fiery-war-axe.png"));
			cards.add(card);

			card = new Card("Fireball", true, 4);
			card.damage = 6;
			card.image = ImageIO.read(new File("res/fireball.png"));
			cards.add(card);

			card = new Card("Flamestrike", false, 7);
			card.damage = 4;
			card.areaOfEffect = true;
			card.minionsOnly = true;
			card.enemiesOnly = true;
			card.image = ImageIO.read(new File("res/flamestrike.png"));
			cards.add(card);

			card = new Card("Frostbolt", true, 2);
			card.damage = 3;
			card.freeze = true;
			card.image = ImageIO.read(new File("res/frostbolt.png"));
			cards.add(card);

			card = new Card("Gilblin Stalker", new Minion(2, 3), 2);
			card.minion.reference = card;
			card.minion.stealth = true;
			card.image = ImageIO.read(new File("res/gilblin-stalker.png"));
			cards.add(card);

			card = new Card("Goblin Blastmage", new Minion(5, 4), 4);
			card.minion.type.add("Battlecry");
			card.minion.reference = card;
			card.damage = 4;
			card.randomlySplit = true;
			card.enemiesOnly = true;
			card.effectedBySpellDamage = false;
			card.image = ImageIO.read(new File("res/goblin-blastmage.png"));
			cards.add(card);

			card = new Card("Harvest Golem", new Minion(2, 3), 3);
			card.minion.type.add("Deathrattle");
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/harvest-golem.png"));
			cards.add(card);

			card = new Card("Knife Juggler", new Minion(3, 2), 2);
			card.damage = 1;
			card.randomlySplit = true;
			card.enemiesOnly = true;
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/knife-juggler.png"));
			cards.add(card);

			card = new Card("Leper Gnome", new Minion(2, 1), 1);
			card.minion.type.add("Deathrattle");
			card.minion.reference = card;
			card.damage = 2;
			card.enemyFace = true;
			card.image = ImageIO.read(new File("res/leper-gnome.png"));
			cards.add(card);

			card = new Card("Loot Hoarder", new Minion(2, 1), 2);
			card.minion.type.add("Deathrattle");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/loot-hoarder.png"));
			cards.add(card);

			card = new Card("Mana Wyrm", new Minion(1, 3), 1);
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/mana-wyrm.png"));
			cards.add(card);

			card = new Card("Mark of the Wild", true, 2);
			card.attackBuff = 2;
			card.healthBuff = 2;
			card.minionsOnly = true;
			card.setToTaunt = true;
			card.image = ImageIO.read(new File("res/mark-of-the-wild.png"));
			cards.add(card);

			card = new Card("Mechanical Yeti", new Minion(4, 5), 4);
			card.minion.type.add("Deathrattle");
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/mechanical-yeti.png"));
			cards.add(card);

			card = new Card("Mechwarper", new Minion(2, 3), 2);
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/mechwarper.png"));
			cards.add(card);

			card = new Card("Polymorph", true, 4);
			card.minionsOnly = true;
			card.image = ImageIO.read(new File("res/polymorph.png"));
			Card sheep = new Card("Sheep", new Minion(1, 1), 0);
			sheep.minion.reference = sheep;
			sheep.minion.type.add("Beast");
			sheep.image = ImageIO.read(new File("res/sheep.png"));
			card.transform = sheep.minion;
			cards.add(card);

			card = new Card("Sen'jin ShieldMasta", new Minion(3, 5), 4);
			card.minion.reference = card;
			card.minion.taunt = true;
			card.image = ImageIO.read(new File("res/sen-jin-shieldmasta.png"));
			cards.add(card);

			card = new Card("Sludge Belcher", new Minion(3, 5), 5);
			card.minion.type.add("Deathrattle");
			card.minion.reference = card;
			card.minion.taunt = true;
			card.image = ImageIO.read(new File("res/sludge-belcher.png"));
			cards.add(card);

			card = new Card("Snow Chugger", new Minion(2, 3), 2);
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.freeze = true;
			card.image = ImageIO.read(new File("res/snowchugger.png"));
			cards.add(card);

			card = new Card("Spider Tank", new Minion(3, 4), 3);
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/spider-tank.png"));
			cards.add(card);

			card = new Card("Stormwind Champion", new Minion(6, 6), 7);
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/stormwind-champion.png"));
			cards.add(card);

			card = new Card("Tinkertown Technician", new Minion(3, 3), 3);
			card.minion.type.add("Battlecry");
			card.minion.reference = card;
			card.targeted = true; // So it can target itself.
			card.attackBuff = 1;
			card.healthBuff = 1;
			card.image = ImageIO.read(new File("res/tinkertown-technician.png"));
			cards.add(card);

			card = new Card("Violet Teacher", new Minion(3, 5), 4);
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/violet-teacher.png"));
			cards.add(card);

			card = new Card("Wild Pyromancer", new Minion(3, 2), 2);
			// Spell reference: (Has to be called somehwere) (Will not be effected by spell damage)
			card.damage = 1;
			card.areaOfEffect = true;
			card.minionsOnly = true;
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/wild-pyromancer.png"));
			cards.add(card);

		} catch (Exception e) {
			System.out.println("1");
		}
	}

	public void defineUnlistedCards() {
		try {
			Card card = new Card("Boom Bot", new Minion(1, 1), 1);
			card.minion.type.add("Deathrattle");
			card.minion.type.add("Mech");
			card.damage = 1;
			card.extraRandomDamage = 3;
			card.randomTarget = true;
			card.enemiesOnly = true;
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/boom-bot.png"));
			unlistedCards.add(card);

			card = new Card("Damaged Golem", new Minion(2, 1), 1);
			card.minion.type.add("Mech");
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/damaged-golem.png"));
			unlistedCards.add(card);

			card = new Card("Sheep", new Minion(1, 1), 0); // This has already been listed in "cards" for polymourph's transform.
			card.minion.reference = card;
			card.minion.type.add("Beast");
			card.image = ImageIO.read(new File("res/sheep.png"));
			unlistedCards.add(card);

			card = new Card("Slime", new Minion(1, 2), 1);
			card.minion.reference = card;
			card.minion.taunt = true;
			card.image = ImageIO.read(new File("res/slime.png"));
			unlistedCards.add(card);

			card = new Card("Violet Apprentice", new Minion(1, 1), 0);
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/violet-apprentice.png"));
			unlistedCards.add(card);

			card = new Card("Silver Hand Recruit", new Minion(1, 1), 1); // This has already been listed in "cards" for polymourph's transform.
			card.minion.reference = card;
			card.image = ImageIO.read(new File("res/silver-hand-recruit.png"));
			unlistedCards.add(card);

		} catch (Exception e) {
			System.out.println("3");
		}
	}

	public void defineSpareParts() {
		// All minions only.
		try {
			Card card = new Card("Armor Plating", true, 1);
			card.healthBuff = 1;
			card.minionsOnly = true;
			card.image = ImageIO.read(new File("res/SP-armor-plating.png"));
			spareParts.add(card);

			card = new Card("Emergency Coolant", true, 1);
			card.freeze = true;
			card.minionsOnly = true;
			card.image = ImageIO.read(new File("res/SP-emergency-coolant.png"));
			spareParts.add(card);

			card = new Card("Finicky Cloakfield", true, 1);
			card.giveTempStealth = true;
			card.minionsOnly = true;
			card.friendliesOnly = true;
			card.image = ImageIO.read(new File("res/SP-finicky-cloakfield.png"));
			spareParts.add(card);

			card = new Card("Reversing Switch", true, 1);
			card.swapHealth = true;
			card.minionsOnly = true;
			card.image = ImageIO.read(new File("res/SP-reversing-switch.png"));
			spareParts.add(card);

			card = new Card("Rusty Horn", true, 1);
			card.setToTaunt = true;
			card.minionsOnly = true;
			card.image = ImageIO.read(new File("res/SP-rusty-horn.png"));
			spareParts.add(card);

			card = new Card("Time Rewinder", true, 1);
			card.returnToHand = true;
			card.minionsOnly = true;
			card.friendliesOnly = true;
			card.image = ImageIO.read(new File("res/SP-time-rewinder.png"));
			spareParts.add(card);

			card = new Card("Whirling Blades", true, 1);
			card.attackBuff = 1;
			card.minionsOnly = true;
			card.image = ImageIO.read(new File("res/SP-whirling-blades.png"));
			spareParts.add(card);
		} catch (Exception e) {
			System.out.println("2");
		}
	}

	public void defineHeroPowers() {
		try {
			Card card = new Card("Fireblast", true, 2); // Paladin / Shaman hero ability *need* to be duplicated. (and converted to 1 mana!)
			card.damage = 1;
			card.effectedBySpellDamage = false;
			card.heroPower = true;
			heroPowers.add(card);

			card = new Card("Reinforce", false, 2); // A non-targeted transform spell.
			card.transform = Board.findOriginalCard("Silver Hand Recruit").clone().minion;
			card.heroPower = true;
			heroPowers.add(card);
		} catch (Exception e) {
			System.out.println("8");
		}
	}

	public void start() {

		GameSet.setPriorityDifferences();

		//start stuff
		defineSpareParts();
		defineCards();
		defineUnlistedCards();
		defineHeroPowers();

		board = new Board();

		/*try {
			File file = new File("output.txt");
			FileOutputStream fis = new FileOutputStream(file);
			PrintStream out = new PrintStream(fis);
			System.setOut(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/

		//start loop
		isRunning = true;
		new Thread(this).start();
	}

	public void stop() {
		isRunning = false;
	}

	public static void main(String args[]) {
		Component component = new Component();
		JFrame frame = new JFrame();

		component.setLayout(null);

		fc = new JFileChooser(new File("res"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".hs files", "hs");
		fc.setFileFilter(filter);

		openButton = new JButton("Load File");
		openButton.addActionListener(component);
		component.add(openButton);
		openButton.setBounds(800, 30, 100, 20);

		nextTurnButton = new JButton("Next Turn");
		nextTurnButton.addActionListener(component);
		component.add(nextTurnButton);
		nextTurnButton.setBounds(800, 100, 150, 50);

		activateRandomButton = new JButton("Activate Random");
		activateRandomButton.addActionListener(component);
		component.add(activateRandomButton);
		activateRandomButton.setBounds(800, 400, 150, 50);

		revertLastButton = new JButton("Revert Last");
		revertLastButton.addActionListener(component);
		component.add(revertLastButton);
		revertLastButton.setBounds(800, 600, 150, 50);

		component.getInputMap(1).put(KeyStroke.getKeyStroke("ENTER"), "NextTurn");
		component.getActionMap().put("NextTurn", component.nextTurnAction);

		frame.add(component);
		frame.setTitle("Hearthstone Simulator");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		component.start();
	}

	Action nextTurnAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			Bot bot = new Bot(board, board.turn);
			bot.playTurn();

			/*Listening.cancel();
			board.doTurn(Math.abs(Component.board.turn - 1));
			try {

				FileOutputStream fout = new FileOutputStream("game" + saveNumber + ".hs");
				saveNumber++;
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(Component.board);
				oos.close();
				//System.out.println("Done");

			} catch (Exception ex) {
				ex.printStackTrace();
			}*/
		}
	};

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			System.out.println("Loading");
			int returnVal = fc.showOpenDialog(Component.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String file_name = file.toString();
				if (!file_name.endsWith(".hs")) {
					file_name += ".hs";
					file = new File(file_name);
				}

				System.out.println(file);
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
		} else if (e.getSource() == nextTurnButton) {
			board.doTurn(Math.abs(board.turn - 1));
		} else if (e.getSource() == activateRandomButton) { // For "real" plays
			try {
				FileOutputStream fout = new FileOutputStream("lastSave.hs");
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(Component.board);
				oos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			runRandomAI(2);
		} else if (e.getSource() == revertLastButton) {
			File file = new File("lastSave.hs");
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
	}

	public void runRandomAI(int type) {
		ArrayList<GameAction> actions = GameAction.getAllActions(board.turn);
		ArrayList<Double> ratings = null;
		if (type == 1) {
			ratings = GameAction.getAllRatings(actions);
		}
		if (actions.size() == 0) {
			// Ending the turn is an action!
			board.doTurn(Math.abs(board.turn - 1));
		} else {
			if (type == 0) {
				GameAction action = actions.get(new Random().nextInt(actions.size()));
				action.doAction();
				board.actionsThisGame.get(board.turn).add(action);
			} else if (type == 1) {
				double randomNum = Math.random() * ratings.get(ratings.size() - 1);
				GameAction action = getRandomGameAction(randomNum, actions, ratings);
				action.doAction();
				board.actionsThisGame.get(board.turn).add(action);
			} else if (type == 2) {
				GameAction bestAction = GameAction.getBestAction(actions);
				bestAction.doAction();
				board.actionsThisGame.get(board.turn).add(bestAction);
			}
		}
		if (Component.gameOver) {
			gameOver();
		}
	}

	private static GameAction getRandomGameAction(double number, ArrayList<GameAction> actions, ArrayList<Double> ratings) {
		for (int i = 0; i < ratings.size(); i++) {
			if (number <= ratings.get(i)) {
				return actions.get(i);
			}
		}
		return null;
	}

	public void opponentBoardCalc() {
		int size = board.minions.get(Math.abs(board.turn - 1)).size();
		ArrayList<Minion> opponentsBoard = new ArrayList<Minion>(size);
		for (int i = 0; i < size; i++) {
			opponentsBoard.add((Minion) board.minions.get(Math.abs(board.turn - 1)).get(i).clone());
		}
		Collections.sort(opponentsBoard);
		/*boolean good = false;
		for (int n = 0; n < opponentBoardLists.size(); n++) {
			if (opponentBoardLists.get(n).size() == opponentsBoard.size()) {
				good = true;
				for (int i = 0; i < opponentBoardLists.get(n).size(); i++) {
					if (opponentBoardLists.get(n).get(i).getValue() != opponentsBoard.get(i).getValue()) {
						good = false;
						break;
					}
				}
				if (good) {
					numOccur.set(n, numOccur.get(n) + 1);
					break;
				}
			} else {
				good = false;
			}
		}
		if (!good) {
			opponentBoardLists.add(opponentsBoard);
			numOccur.add(1);
		}*/
	}

	public void gameOver() {
		Component.wins[Component.lastWinner]++;
		for (int p = 0; p < 2; p++) {
			for (GameAction gameAction : board.actionsThisGame.get(p)) {
				if (Component.lastWinner == p) {
					gameAction.getProfile(true).addCase(1); // true as to effect the current gameSet
				} else {
					gameAction.getProfile(true).addCase(-1);
				}
			}
		}
		Component.gameOver = false;
		Component.board = new Board();

		if (wins[0] + wins[1] == GameSet.numPerSet) {
			GameSet.reset();
		}
	}

	public void tick() {

		/*if (Component.board.mana[0] == 5) {
			Component.board.mana[0] = 6;
			try {
				if (new File("gameSave.hs").exists()) {
					FileInputStream fin = new FileInputStream("gameSave.hs");
					ObjectInputStream ois = new ObjectInputStream(fin);
					Component.board = (Board) ois.readObject();
					ois.close();
					// For every card.. image has to be set
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}*/

		// Run listening commands:
		if (Listening.minionAttacking != null && Listening.minionDefending != null) {
			new GameAction(Listening.minionAttacking, Listening.minionDefending).doAction();
			//Listening.minionAttacking.attack(Listening.minionDefending);
			Listening.minionAttacking = null;
			Listening.minionDefending = null;
		}

		/*if (Listening.cardToBePlayed != -1) {
			board.playCard(Listening.cardToBePlayed);
			Listening.cardToBePlayed = -1;
		}*/

		if (Listening.mouseReleasedLastTick && mousePos != null) {
			if (Listening.cardToBePlayed != -1) {
				if (mousePos.y > board.getHerosHeight(0) && mousePos.y < board.getHerosHeight(1) + Board.heroDisplayHeight) {
					// If card is a minion, put it there:
					Card card;
					if (Listening.cardToBePlayed == -2) { // Hero Ability
						card = board.heroAbility[board.turn];
					} else {
						card = board.cards.get(board.turn).get(Listening.cardToBePlayed);
					}
					if (card.minion != null) {
						// Minions are all pretty basic:
						int n = (mousePos.x + Card.width / 2) / Card.width;
						if (n >= 0 && n < 7) {
							if (n > board.minions.get(board.turn).size()) {
								n = board.minions.get(board.turn).size();
							}
							new GameAction(Listening.cardToBePlayed, n).doAction();
							//board.playMinion(n, Listening.cardToBePlayed);
						}
						Listening.cardToBePlayed = -1;
					} else if (card.weapon != null) {
						new GameAction(Listening.cardToBePlayed).doAction();
						//board.playWeapon(Listening.cardToBePlayed);
						Listening.cardToBePlayed = -1;
					} else {
						// It's a spell:
						if (card.targeted) { // Find target
							int y = 2;
							if (mousePos.y > board.getHerosHeight(1)) {
								y = 3;
							} else if (mousePos.y > board.getMinionsHeight(1)) {
								y = 1;
							} else if (mousePos.y > board.getMinionsHeight(0)) {
								y = 0;
							}
							if (y == 0 || y == 1) { // Minion
								int n = mousePos.x / Card.width;
								// If minion exists:
								if (n >= 0 && n < board.minions.get(y).size() && !board.minions.get(y).get(n).stealth) { // can't be stealth either
									//board.playSpell(Listening.cardToBePlayed, board.minions.get(y).get(n)); // Reference Minion (null if not needed)
									new GameAction(Listening.cardToBePlayed, board.minions.get(y).get(n)).doAction();
								}
							} else {
								// Face:
								//board.playSpell(Listening.cardToBePlayed, board.hero[y - 2]);
								new GameAction(Listening.cardToBePlayed, board.hero[y - 2]).doAction();
							}
						} else {
							// Just play spell:
							//board.playSpell(Listening.cardToBePlayed, null);
							new GameAction(Listening.cardToBePlayed).doAction();
						}
						Listening.cardToBePlayed = -1;
					}
				}
			} else if (Listening.battlecryBuff != null) {
				if (Listening.battlecryBuff.targeted && Listening.battlecryBuff.minionsOnly) { // Find target
					int y = 2;
					if (mousePos.y > board.getHerosHeight(1)) {
						y = 3;
					} else if (mousePos.y > board.getMinionsHeight(1)) {
						y = 1;
					} else if (mousePos.y > board.getMinionsHeight(0)) {
						y = 0;
					}
					if (y == 0 || y == 1) { // Minion
						int n = mousePos.x / Card.width;
						// If minion exists:
						if (n >= 0 && n < board.minions.get(y).size() && board.minions.get(y).get(n) != Listening.battlecryBuff.minion) {
							Listening.battlecryBuff.play(board.minions.get(y).get(n));
							Listening.battlecryBuff = null; // HAS to hit a minion
						}
					}
				}
			}
			Listening.mouseReleasedLastTick = false;
		}
	}

	public void render() {
		((VolatileImage) screen).validate(getGraphicsConfiguration());
		Graphics g = screen.getGraphics();
		screenPos = getLocationOnScreen();
		mousePos = getMousePosition();
		//draw:
		g.setColor(new Color(230, 170, 70));
		g.fillRect(0, 0, Card.width * 7 + 10, screenH);
		Graphics2D g2 = (Graphics2D) g;

		g.setColor(new Color(0, 0, 0));
		g.drawString(wins[0] + " / " + wins[1], 720, 350);

		//Listening.currLocation = new Point(MouseInfo.getPointerInfo().getLocation().x - Component.screenPos.x, MouseInfo.getPointerInfo().getLocation().y - Component.screenPos.y);

		for (int i = 0; i < 1000; i++) {
			if (GameSet.currentSet < GameSet.totalSets) {
				if (GameSet.currentSet == 0) {
					runRandomAI(0);
				} else {
					runRandomAI(1);
				}
			}
		}
		render2();
		board.render(g2);

		g = getGraphics();
		g.drawImage(screen, 0, 0, Card.width * 7 + 10, screenH, 0, 0, Card.width * 7 + 10, screenH, null);
		g.dispose();
	}

	public void render2() {
		Graphics g = screen2.getGraphics();
		//draw:
		g.setColor(new Color(230, 170, 140));
		g.fillRect(0, 0, 1000, 200);

		ProfileUI.render(g);

		g = getGraphics();
		g.drawImage(screen2, Card.width * 7 + 10, 180, null);
		g.dispose();
	}

	public void run() {
		screen = createVolatileImage(Card.width * 7 + 10, screenH);
		screen2 = createImage(1000, 190);
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / /*Just in case*/(double) ticksPerSecond;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		while (isRunning) {
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			while (unprocessed >= 1) {
				ticks++;
				tick();
				unprocessed -= 1;
			}
			{
				frames++;
				render();
				if (unprocessed < 1) {
					try {
						Thread.sleep((int) ((1 - unprocessed) * nsPerTick) / 1000000, (int) ((1 - unprocessed) * nsPerTick) % 1000000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 += 1000;
				System.out.println(ticks + " ticks, " + frames + " fps");
				frames = 0;
				ticks = 0;
			}
		}
	}
}
