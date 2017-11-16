import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Vector;
import java.util.Random;
import java.util.Scanner;

public class Peer implements Game {

	static boolean debug = true;

	static int start = 0; // indicate whether game is started or not
	static int playerCount = -1;
	static int mazeSize = -1;
	static int treasureCount = -1;

	static MazeInfo[][] mazeInfo = null; 
	// Store the information of player occupy and treasure in the maze

	static Vector<PlayerInfo> players = new Vector<PlayerInfo>(0, 1); 
	// Store the information of players

	static int pID = -1;
	static boolean backupFlag = false;
	static boolean initialS2Flag = false;
	static Game primary = null;
	static int primaryID = 0;
	static Game backup = null;
	static int backupID = -1;

	static Registry registry = null;
	static Peer obj = new Peer();

	public ReplyPackage join() {
		synchronized (this) {
			ReplyPackage reply = new ReplyPackage();

			if (debug == true) {
				System.out.print("\n");
				System.out.print("[DEBUG_S] - join() - primaryID: " + Integer.toString(primaryID) + "\n");
				System.out.print("[DEBUG_S] - join() - backupID: " + Integer.toString(backupID) + "\n");
				System.out.print("[DEBUG_S] - join() - playerCount: " + Integer.toString(playerCount) + "\n");
			}

			playerCount++;

			//if it is not S1 joining the game
			if (playerCount != pID) {
				if (backupFlag == false) {
					// it means it is the first peer to join the game
					// will set it as the backup server
					backupID = playerCount;
					backupFlag = true;
				}
			}

			PlayerInfo tmpPlayer;

			if (start == 0) { // player can only join the game if the game
								// hasn't started yet
				if (debug == true) {
					System.out.print("\n[DEBUG] - join() - pID: " + pID + "\n");
				}

				int initX, initY; // define the initial position of a player
				do {
					Random random = new Random();// generate random number as
													// initial position
					initX = random.nextInt(mazeSize - 1);
					initY = random.nextInt(mazeSize - 1);
				} while (mazeInfo[initX][initY].occupyFlag != -1);

				players.addElement(new PlayerInfo(playerCount, initX, initY, 0));

				mazeInfo[initX][initY].occupyFlag = 1;
				tmpPlayer = players.lastElement();
			} else {
				tmpPlayer = new PlayerInfo();
			}

			reply.playerInfo = tmpPlayer;
			reply.primaryID = primaryID;
			reply.backupID = backupID;

			if (debug == true) {
				System.out.print("\n");
				System.out.print("[DEBUG_E] - join() - primaryID: " + Integer.toString(primaryID) + "\n");
				System.out.print("[DEBUG_S] - join() - backupID: " + Integer.toString(backupID) + "\n");
				System.out.print("[DEBUG_E] - join() - playerCount: " + Integer.toString(playerCount) + "\n");
			}

			return reply;
		}
	}

	public ReplyPackage move(int ID, String Direction) {
		synchronized (this) {
			ReplyPackage reply = new ReplyPackage();

			reply.primaryID = primaryID;

			if (debug == true) {
				System.out.print("\n");
				System.out.print("[DEBUG_S] - move() - primaryID: " + Integer.toString(primaryID) + "\n");
				System.out.print("[DEBUG_S] - move() - backupID: " + Integer.toString(backupID) + "\n");
			}

			int dX = 0;
			int dY = 0;
			switch (Direction) { // based on the move direction, set the change
									// of X or Y coordinate

			case "a":
				dX = -1;
				dY = 0;
				break;
			case "d":
				dX = 1;
				dY = 0;
				break;
			case "w":
				dX = 0;
				dY = -1;
				break;
			case "s":
				dX = 0;
				dY = 1;
				break;
			case "n":
				dX=0;
				dY=0;
				break;
			default:
				break;
			}

			int oldX = players.elementAt(ID).directionX; // Extract the current
															// position of the
															// requesting player
			int oldY = players.elementAt(ID).directionY; // Same as above line
			int newX = oldX + dX;
			int newY = oldY + dY;

			if ((newX) >= mazeSize | (newX) < 0 | (newY) >= mazeSize | (newY) < 0) {

				reply.error = 2;// if new location is out of maze

			} else {
				if (mazeInfo[newX][newY].occupyFlag != -1) {
					
					if(Direction.charAt(0) != 'n') {
						reply.error = 1; // if target coordinate is occupied						
					} else {
						reply.error = 0;
					}
					
				} else {

					players.elementAt(ID).directionX = newX; // Update player
																// new location
					players.elementAt(ID).directionY = newY; // Same as above
																// line
					players.elementAt(ID).playerTreasureCount += mazeInfo[newX][newY].mazeTreasure; // add
																									// the
																									// treasure
																									// count
																									// to
																									// the
																									// players
					mazeInfo[oldX][oldY].occupyFlag = -1; // set the old
															// location as
															// unoccupied
					mazeInfo[newX][newY].occupyFlag = 1;// set the new location
														// as occupied;
					mazeInfo[newX][newY].mazeTreasure = 0;// the treasures at
															// the new location
															// are taken away
					reply.error = 0; // move successfully

				}
			}

			int tmpTreasureCount = 0;
			for (int y = 0; y <= mazeSize - 1; y++) {
				for (int x = 0; x <= mazeSize - 1; x++) {
					tmpTreasureCount += mazeInfo[x][y].mazeTreasure;
				}
			}
			if (tmpTreasureCount == 0) {
				reply.error = 3;
				start = 0;
			}

			reply.playerInfo = players.elementAt(ID);
			reply.mazeInfo = mazeInfo.clone();
			reply.mazeSize = mazeSize;

			if (debug == true) {
				System.out.print("\n[DEBUG] - move() - Current maze treasure status:\n");
				for (int y = 0; y <= mazeSize - 1; y++) {
					System.out.print("[DEBUG] - move() - ");
					for (int x = 0; x <= mazeSize - 1; x++) {
						System.out.print(mazeInfo[x][y].mazeTreasure + " ");
					}
					System.out.print("\n");
				}
				System.out.print("\n[DEBUG] - move() - backupID: " + Integer.toString(backupID) + "\n");
			}

			reply.backupID = backupID;
			try {

				backup.update(mazeInfo, players, mazeSize, treasureCount);
				if (debug == true) {
					System.out.print("\n[DEBUG] - move() - Backup updated\n");
				}

			} catch (Exception e) {

				if (initialS2Flag == false && primaryID != ID) {
					System.out.print("\nBackup server down!\n");

					initialS2Flag = true;
					backupID = ID;/// wanghe need to tackle backup server
					reply.backupID = ID;
				}

			}

			if (debug == true) {
				System.out.print("\n");
				System.out.print("[DEBUG_E] - move() - primaryID: " + Integer.toString(primaryID) + "\n");
				System.out.print("[DEBUG_E] - move() - backupID: " + Integer.toString(backupID) + "\n");
			}

			return reply;
		}
	}

	public void update(MazeInfo[][] maze_Info, Vector<PlayerInfo> players_, int maze_Size, int treasure_Count) {
		mazeInfo = maze_Info.clone();
		players = players_;
		mazeSize = maze_Size;
		treasureCount = treasure_Count;
	}

	public ReplyPackage gameStatus(int ID) {
		synchronized (this) {
			ReplyPackage replyPackage = new ReplyPackage();
			replyPackage.playerInfo = players.elementAt(ID);
			replyPackage.mazeInfo = mazeInfo.clone();
			replyPackage.mazeSize = mazeSize;
			replyPackage.players = players;
			return replyPackage;
		}
	}

	public ReplyPackage primaryDown(int ID) {
		synchronized (this) {
			ReplyPackage reply = new ReplyPackage();

			if (debug == true) {
				System.out.print("\n");
				if (backupFlag == true) {
					System.out.print("[DEBUG_S] - primaryDown() - backupFlag: true\n");
				} else {
					System.out.print("[DEBUG_S] - primaryDown() - backupFlag: false\n");
				}
				System.out.print("[DEBUG_E] - primaryDown() - primaryID: " + Integer.toString(primaryID) + "\n");
				System.out.print("[DEBUG_E] - primaryDown() - backupID: " + Integer.toString(backupID) + "\n");
			}

			primaryID = pID;
			try {
				registry = LocateRegistry.getRegistry();
				primary = (Game) registry.lookup("pID_" + Integer.toString(primaryID));
			} catch (Exception e) {
			}

			if (backupFlag == false && ID != backupID) {
				backupFlag = true;
				backupID = ID;
			}

			reply.backupID = backupID;
			reply.primaryID = primaryID;

			if (debug == true) {
				System.out.print("\n");
				if (backupFlag == true) {
					System.out.print("[DEBUG_E] - primaryDown() - backupFlag: true\n");
				} else {
					System.out.print("[DEBUG_E] - primaryDown() - backupFlag: false\n");
				}
				System.out.print("[DEBUG_E] - primaryDown() - primaryID: " + Integer.toString(primaryID) + "\n");
				System.out.print("[DEBUG_E] - primaryDown() - backupID: " + Integer.toString(backupID) + "\n");
			}

			return reply;
		}
	}

	public void backupUp() {
		synchronized (this) {
			try {
				registry = LocateRegistry.getRegistry();
				backup = (Game) registry.lookup("pID_" + Integer.toString(backupID));
				if (debug == true) {
					System.out.print("\n[DEBUG] - backupUP() - New setup backup ready\n");
				}
			} catch (Exception e) {
				System.out.print("\nNew setup backup exception: " + e.toString() + "\n");
			}

			if (initialS2Flag == true) {
				initialS2Flag = false;
			}
		}
	}

	public int waitStart() {
		return start;
	}

	public static void main(String args[]) {

		ReplyPackage replyPackage = new ReplyPackage();

		// ===============
		// === Initial ===
		// ===============

		if (args.length == 2) {
			mazeSize = Integer.parseInt(args[0]);
			treasureCount = Integer.parseInt(args[1]);
			pID = 0;
			primaryID = pID;
		}

		// ========================
		// === Bind Primary RMI ===
		// ========================

		if (primaryID == pID) { // If i am the primary server, i need to
								// register my pID as the primary server
			try {
				primary = (Game) UnicastRemoteObject.exportObject(obj, 0);
				registry = LocateRegistry.getRegistry();
				registry.bind("pID_" + Integer.toString(primaryID), primary);

				System.out.print("\nSetup primary ready\n");
			} catch (Exception e) {
				try {
					registry.unbind("pID_" + Integer.toString(primaryID));
					registry.bind("pID_" + Integer.toString(primaryID), primary);
					System.out.print("\nSetup primary ready\n");
				} catch (Exception ee) {
					System.out.print("\nPrimary exception: " + ee.toString() + "\n");
					ee.printStackTrace();
				}
			}
		} else { // If i am not the primary server, i need to search for primary
					// server
			try {
				registry = LocateRegistry.getRegistry();
				primary = (Game) registry.lookup("pID_0"); // primary ID at the
															// start is default
															// 0
				System.out.print("\nConnect to primary OK\n");
			} catch (Exception e) {
				System.out.print("\nConnect to primary exception: " + e.toString() + "\n");
				e.printStackTrace();
			}
		}
		
		try {
			start = primary.waitStart();
			if(start == 1) {
				System.out.print("\nGame has been started!\n");
				return;
			}
		} catch (Exception e) {
			System.out.print("\nGame start check error\n");
			System.out.print("\nGame start check exception: " + e.toString()+"\n");
		}
		
		// primary server is settled

		// ================================
		// === Initial Maze / Join Game ===
		// ================================

		// now is time for peers to join the game and settle the backup server
		if (primaryID == pID) {

			System.out.print("\nInitialize Maze...\n");

			mazeInfo = new MazeInfo[mazeSize][mazeSize];
			for (int a = mazeSize; a > 0; a--)
				for (int b = mazeSize; b > 0; b--)
					mazeInfo[a - 1][b - 1] = new MazeInfo(-1, 0); // initialize
																	// the
																	// treasure
																	// locations,
																	// put the M
																	// treasures
																	// randomly
																	// into the
																	// maze

			for (int i = treasureCount; i > 0; i--) {
				Random random = new Random();
				int a = random.nextInt(mazeSize - 1);
				int b = random.nextInt(mazeSize - 1);
				mazeInfo[a][b].mazeTreasure++;
			}

			if (debug == true) {
				System.out.print("\n[DEBUG] - main() - Treasure location in the Maze:\n");
				for (int b = 0; b <= mazeSize - 1; b++) {
					System.out.print("[DEBUG] - main() - ");
					for (int a = 0; a <= mazeSize - 1; a++) {
						System.out.print(mazeInfo[a][b].mazeTreasure + " ");
					}
					System.out.print("\n");
				}
			}

			try {
				replyPackage = primary.join();
			} catch (Exception e) {
				System.out.print("\nPrimary join exception: " + e.toString() + "\n");
			}
			
			System.out.print("\nMaze initialised!\n");

		} else {

			// if i am a client, i need to join the game, and get the S1 ID and
			// S2 ID
			// the first client to join the game will be the backup server
			try {
				replyPackage = primary.join();
				primaryID = replyPackage.primaryID;
				backupID = replyPackage.backupID;
				pID = replyPackage.playerInfo.pID;
			} catch (Exception e) {
				System.out.print("\nClient join exception: " + e.toString() + "\n");
			}

			if (backupID == pID) {
				// If my pID is equal to backup ID, it means i am the first one
				// to join the game i will need to register myself as backup
				// server
				try {
					backup = (Game) UnicastRemoteObject.exportObject(obj, 0);
					registry = LocateRegistry.getRegistry();

					// bind my pID as backup server
					registry.bind("pID_" + Integer.toString(backupID), backup);
					System.out.print("\nSetup backup ready\n");
				} catch (Exception e) {
					try {
						registry.unbind("pID_" + Integer.toString(backupID));
						registry.bind("pID_" + Integer.toString(backupID), backup);
						System.out.print("\nSetup backup ready\n");
					} catch (Exception ee) {
						System.out.print("\nBackup exception: " + ee.toString());
						ee.printStackTrace();
					}
				}
			} else {
				// If i am not the backup server, i will need to look up for
				// backup server
				try {
					registry = LocateRegistry.getRegistry();
					backup = (Game) registry.lookup("pID_" + Integer.toString(backupID));
					System.out.print("\nConnect to backup OK\n");
				} catch (Exception e) {
					System.out.print("\nConnect to backup exception: " + e.toString() + "\n");
					e.printStackTrace();
				}
			}
		}

		// If i am s1, i need to initialize maze and start count down
		if (primaryID == pID) {

			while (playerCount < 1) { // playerCount = 1 means 2 players in the
										// game
				try {
					System.out.checkError();
				} catch (Exception e) {
					System.out.print(e);
				}
			}

			// Start count down
			for (int a = 7; a > 0; a--) {
				try {
					Thread.sleep(1000); // one second
					System.out.print("\nCountdown:" + a);
				} catch (Exception e) {
				}
				System.out.print("\n");
			}

			try {
				registry = LocateRegistry.getRegistry();
				backup = (Game) registry.lookup("pID_" + Integer.toString(backupID));
				System.out.print("\nConnect to backup OK\n");
			} catch (Exception e) {
				System.out.print("\nConnect to backup exception: " + e.toString());
				e.printStackTrace();
			}

			start = 1; // game has started
			if (debug == true) {
				System.out.print("\n[DEBUG] - main() - start = " + start + "\n");
			}
			
		} else {
			
			System.out.print("\nWaiting for players...\n");
			do {
				try {
					start = primary.waitStart();
				} catch (Exception e) {
					System.out.print("\nStart error\n");
					System.out.print("\nStart error exception: " + e.toString());
				}
			} while (start == 0);
		}

		try {
			backup.update(mazeInfo, players, mazeSize, treasureCount);
		} catch (Exception e) {
		}
		System.out.print("\n\nGame Start!\n\n");

		// ====================
		// === Prompt Input ===
		// ====================

		String dir;

		//draw maze at the start of the game
		try {
			replyPackage = primary.gameStatus(pID);
		} catch (Exception e) {
			System.out.print("\nGame status\n");
			System.out.print("\nGame status: " + e.toString() + "\n");
		}

		Scanner scan = new Scanner(System.in);
		do {

			System.out.print("\nCurrent treasure collected: " + replyPackage.playerInfo.playerTreasureCount + "\n");

			System.out.print("\nMaze:\n"); // Draw the game status
			for (int y = 0; y <= replyPackage.mazeSize - 1; y++) {
				for (int x = 0; x <= replyPackage.mazeSize - 1; x++) {

					if ((x == replyPackage.playerInfo.directionX) && (y == replyPackage.playerInfo.directionY))
						System.out.print("X ");
					else {
						if (replyPackage.mazeInfo[x][y].occupyFlag == 1) {
							System.out.print("Y ");
						} else
							System.out.print(replyPackage.mazeInfo[x][y].mazeTreasure + " ");
					}

				}
				System.out.print("\n");
			}

			System.out.print("\nMake a move: "); // Ask the player to key in
													// commands. Either
													// movements or Exit

			while (1 > 0) {
				try {
					dir = scan.nextLine();
					if ((dir.charAt(0) == 'w') | (dir.charAt(0) == 'a') | (dir.charAt(0) == 's')
							| (dir.charAt(0) == 'd')|(dir.charAt(0) == 'n'))
						break;
				} catch (Exception e) {
					continue;
				}
				System.out.print("Wrong input, please key in a direction(s/n/w/e): ");
			}

			if (dir == "Exit") // if player exit the game
				break;

			try {
				if (debug == true) {
					System.out.print("\n");
					System.out.print("[DEBUG_S] - main() - primaryID: " + Integer.toString(primaryID) + "\n");
					System.out.print("[DEBUG_S] - main() - backupID: " + Integer.toString(backupID) + "\n");
				}

				replyPackage = primary.move(pID, dir); // make move
				primaryID = replyPackage.primaryID;
				backupID = replyPackage.backupID;

				if (debug == true) {
					System.out.print("\n");
					System.out.print("[DEBUG_E] - main() - primaryID: " + Integer.toString(primaryID) + "\n");
					System.out.print("[DEBUG_E] - main() - backupID: " + Integer.toString(backupID) + "\n");
				}

				if (pID == backupID) {

					try {
						backup = (Game) UnicastRemoteObject.exportObject(obj, 0);
						registry = LocateRegistry.getRegistry();
						registry.bind("pID_" + Integer.toString(backupID), backup);
						System.out.print("\nNew setup backup ready\n");
					} catch (Exception ee) {
						try {
							registry.unbind("pID_" + Integer.toString(backupID));
							registry.bind("pID_" + Integer.toString(backupID), backup);
							System.out.print("\nNew setup backup ready\n");
						} catch (Exception eee) {
							System.out.print("\nNew setup back exception: " + ee.toString() + "\n");
							ee.printStackTrace();
						}
					}

					primary.backupUp();

				} else {

					registry = LocateRegistry.getRegistry();
					backup = (Game) registry.lookup("pID_" + Integer.toString(backupID));

				}

			} catch (Exception e) {
				// System.out.print("\nMove error\n");
				// System.out.print("Move error exception: " + e.toString());

				try {

					if (debug == true) {
						System.out.print("\n");
						System.out.print(
								"[DEBUG_S] - main(),primaryDown() - primaryID: " + Integer.toString(primaryID) + "\n");
						System.out.print(
								"[DEBUG_S] - main(),primaryDown() - backupID: " + Integer.toString(backupID) + "\n");
					}

					replyPackage = backup.primaryDown(pID);
					backupID = replyPackage.backupID;
					primaryID = replyPackage.primaryID;

					if (debug == true) {
						System.out.print("\n");
						System.out.print(
								"[DEBUG_E] - main(),primaryDown() - primaryID: " + Integer.toString(primaryID) + "\n");
						System.out.print(
								"[DEBUG_E] - main(),primaryDown() - backupID: " + Integer.toString(backupID) + "\n");
					}

					// i also need to link to the new primary server
					registry = LocateRegistry.getRegistry();
					primary = (Game) registry.lookup("pID_" + Integer.toString(primaryID));
					replyPackage = primary.move(pID, dir); // make move
					
					// if i am the first one to find primary down, i will be the
					// backup
					if (backupID == pID) {
						try {
							backup = (Game) UnicastRemoteObject.exportObject(obj, 0);
							registry = LocateRegistry.getRegistry();
							// bind my pID as backup server
							registry.bind("pID_" + Integer.toString(backupID), backup);
							System.out.print("\nSetup new backup ready\n");
						} catch (Exception ee) {
							try {
								registry.unbind("pID_" + Integer.toString(backupID));
								registry.bind("pID_" + Integer.toString(backupID), backup);
								System.out.print("\nSetup new backup ready\n");
							} catch (Exception eee) {
								System.out.print("\nSetup new backup exception: " + ee.toString() + "\n");
								ee.printStackTrace();
							}
						}
						primary.backupUp();
					} else {
						registry = LocateRegistry.getRegistry();
						backup = (Game) registry.lookup("pID_" + Integer.toString(backupID));
					}

//					replyPackage = primary.move(pID, dir); // make move

				} catch (Exception ee) {
					System.out.print("\nLink to new primary exception: " + ee.toString()+"\n");
				}

			}

			if (replyPackage.error == 1) {
				System.out.print("\nDestination is already occupied by another player!!!\n");
			} else {
				if (replyPackage.error == 2)
					System.out.print("\nHits the wall!!!\n");
				else {
					if (replyPackage.error == 3) {
						try {
							replyPackage = primary.gameStatus(pID);
						} catch (Exception e) {
							System.out.print("\nGame status\n");
							System.out.print("\nGame status: " + e.toString() + "\n");
						}
						
						System.out.print("\nGame Over!!!\n");
						
						int n = 1;
						for(int i = 0;i<= replyPackage.players.size()-1;i++) {
							if(pID != i) {
								if(replyPackage.players.elementAt(i).playerTreasureCount > replyPackage.players.elementAt(pID).playerTreasureCount) {
									n++;
								}
							}
						}
						System.out.print("\nYou rank no."+Integer.toString(n)+" !!!\n");
						
						try {
							registry.unbind("pID_" + Integer.toString(pID));
							UnicastRemoteObject.unexportObject(obj, true);
						} catch (Exception e) {
						}
						break;
					}
				}
			}

		} while (1 > 0);
		
		scan.close();

	} // end of main

} // end of class
