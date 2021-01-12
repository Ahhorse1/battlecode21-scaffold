package jcplayer;

import battlecode.common.*;
import java.util.ArrayList;

public strictfp class RobotPlayer {
	static RobotController rc;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	static int turnCount;
	static MapLocation ecLoc;
	static int shortestDistance;

	// Variables that store ID number of the created units for enlightenment centers
	static ArrayList<Integer> createdPoliticiansID;
	static ArrayList<Integer> createdSlanderersID;
	static ArrayList<Integer> createdMuckrakersID;

	// Store ID of the enlightenment center that created you
	static int enlightenmentCenterID;

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	@SuppressWarnings("unused")
	public static void run(RobotController rc) throws GameActionException {

		// This is the RobotController object. You use it to perform actions from this
		// robot,
		// and to get information on its current status.
		RobotPlayer.rc = rc;

		turnCount = 0;

		System.out.println("I'm a " + rc.getType() + " and I just got created!");
		while (true) {
			turnCount += 1;
			// Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
			try {
				// Here, we've separated the controls into a different method for each
				// RobotType.
				// You may rewrite this into your own control structure if you wish.
				// System.out.println("I'm a " + rc.getType() + "! Location " +
				// rc.getLocation());
				switch (rc.getType()) {
				case ENLIGHTENMENT_CENTER:
					runEnlightenmentCenter(rc);
					break;
				case POLITICIAN:
					runPolitician(rc);
					break;
				case SLANDERER:
					runSlanderer(rc);
					break;
				case MUCKRAKER:
					runMuckraker(rc);
					break;
				}

				// Clock.yield() makes the robot wait until the next turn, then it will perform
				// this loop again
				Clock.yield();

			} catch (Exception e) {
				System.out.println(rc.getType() + " Exception");
				e.printStackTrace();
			}
		}
	}

	static void runEnlightenmentCenter(RobotController rc) throws GameActionException {
		int flag = rc.getFlag(rc.getID());
		if (rc.isReady()) {
			if (flag < 4)
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 20)) {
						rc.buildRobot(RobotType.MUCKRAKER, dir, 20);
						System.out.println("Built MUCKRAKER");
						createdMuckrakersID.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
						flag++;
					}
				}
		}
		rc.setFlag(flag);
		// RobotType toBuild = randomSpawnableRobotType();
		/*
		 * int gameMode = 0; if (turnCount < 25) gameMode = 1; else if (turnCount < 100)
		 * gameMode = 2; else if (turnCount < 225) gameMode = 3; else gameMode = 4;
		 * switch (gameMode) { case 1: Stage1(rc); case 2: Stage2(rc); case 3:
		 * Stage3(rc); case 4: Normal(rc);
		 * 
		 * }
		 */

	}

	static void runPolitician(RobotController rc) throws GameActionException {
		Team enemy = rc.getTeam().opponent();
		int actionRadius = rc.getType().actionRadiusSquared;
		RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
		RobotInfo[] neutral = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
		if ((attackable.length != 0 || neutral.length != 0) && rc.canEmpower(actionRadius)) {
			rc.empower(actionRadius);
			return;
		}
		tryMove(randomDirection());
	}

	static void runSlanderer(RobotController rc) throws GameActionException {
		int minRoundInfluence = 20;
		if (turnCount > 225)
			minRoundInfluence = 25;
		if (rc.getInfluence() < 41) {
			tryMove(randomDirection());
		} else {
			RobotInfo[] nearbyRobots = rc.senseNearbyRobots(20, rc.getTeam());
			boolean canDetectHQ = false;
			for (int i = 0; i < nearbyRobots.length; i++) {
				if (nearbyRobots[i].type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
					canDetectHQ = true;
				}
			}
			if (canDetectHQ)
				tryMove(randomDirection());
		}
	}

	static void runMuckraker(RobotController rc) throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		if (rc.getFlag(rc.getID()) == 0) {
			for (Direction dir : directions) {
				if (rc.senseRobotAtLocation(currentLoc.add(dir)) != null) {
					RobotInfo temp = rc.senseRobotAtLocation(currentLoc.add(dir));
					if (temp.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
						ecLoc = temp.location;
						System.out.println("EnlightmentCenter xCoord: " + ecLoc.x);
						System.out.println("EnlightmentCenter yCoord: " + ecLoc.y);
						int flag = rc.getFlag(temp.ID);
						if (flag / 10 == 1) {
							switch (flag % 10) {
							case 0:
								rc.setFlag(1000000);
								break;
							case 1:
								rc.setFlag(2000000);
								break;
							case 2:
								rc.setFlag(3000000);
								break;
							case 3:
								rc.setFlag(4000000);
								break;
							}
						}
					}
				}
			}
		}
		if (rc.isReady()) {
			if (isCornerRunner(rc)) {
				boolean isAtCorner = false;
				int flag = rc.getFlag(rc.getID());
				switch (flag / 1000000) {
				case 1:
					if (rc.canMove(Direction.NORTHWEST)) {
						rc.move(Direction.NORTHWEST);
						return;
					} else if (rc.canMove(Direction.NORTH)) {
						rc.move(Direction.NORTH);
						return;
					} else if (rc.canMove(Direction.WEST)) {
						rc.move(Direction.WEST);
						return;
					} else {
						MapLocation tempLoc = currentLoc;
						tempLoc = tempLoc.add(Direction.NORTH);
						tempLoc = tempLoc.add(Direction.NORTH);
						MapLocation tempLoc2 = currentLoc;
						tempLoc2 = tempLoc2.add(Direction.WEST);
						tempLoc = tempLoc.add(Direction.WEST);
						if (rc.canSenseLocation(tempLoc)) {
							if (rc.canMove(Direction.NORTHEAST)) {
								rc.move(Direction.NORTHEAST);
								return;
							} else if (rc.canMove(Direction.EAST)) {
								rc.move(Direction.EAST);
								return;
							}
						} else if (rc.canSenseLocation(tempLoc2)) {
							if (rc.canMove(Direction.SOUTHWEST)) {
								rc.move(Direction.SOUTHWEST);
								return;
							} else if (rc.canMove(Direction.SOUTH)) {
								rc.move(Direction.SOUTH);
								return;
							}
						} else {
							isAtCorner = true;
							System.out.println("At Northwest Corner");
						}
					}
					break;
				case 2:
					if (rc.canMove(Direction.NORTHEAST)) {
						rc.move(Direction.NORTHEAST);
						return;
					} else if (rc.canMove(Direction.NORTH)) {
						rc.move(Direction.NORTH);
						return;
					} else if (rc.canMove(Direction.EAST)) {
						rc.move(Direction.EAST);
						return;
					} else {
						MapLocation tempLoc = currentLoc;
						tempLoc = tempLoc.add(Direction.NORTH);
						tempLoc = tempLoc.add(Direction.NORTH);
						MapLocation tempLoc2 = currentLoc;
						tempLoc2 = tempLoc2.add(Direction.EAST);
						tempLoc = tempLoc.add(Direction.EAST);
						if (rc.canSenseLocation(tempLoc)) {
							if (rc.canMove(Direction.NORTHWEST)) {
								rc.move(Direction.NORTHWEST);
								return;
							} else if (rc.canMove(Direction.WEST)) {
								rc.move(Direction.WEST);
								return;
							}
						} else if (rc.canSenseLocation(tempLoc2)) {
							if (rc.canMove(Direction.SOUTHEAST)) {
								rc.move(Direction.SOUTHEAST);
								return;
							} else if (rc.canMove(Direction.SOUTH)) {
								rc.move(Direction.SOUTH);
								return;
							}
						} else {
							isAtCorner = true;
							System.out.println("At Northeast Corner");
						}
					}
					break;
				case 3:
					if (rc.canMove(Direction.SOUTHEAST)) {
						rc.move(Direction.SOUTHEAST);
						return;
					} else if (rc.canMove(Direction.SOUTH)) {
						rc.move(Direction.SOUTH);
						return;
					} else if (rc.canMove(Direction.EAST)) {
						rc.move(Direction.EAST);
						return;
					} else {
						MapLocation tempLoc = currentLoc;
						tempLoc = tempLoc.add(Direction.SOUTH);
						tempLoc = tempLoc.add(Direction.SOUTH);
						MapLocation tempLoc2 = currentLoc;
						tempLoc2 = tempLoc2.add(Direction.EAST);
						tempLoc = tempLoc.add(Direction.EAST);
						if (rc.canSenseLocation(tempLoc)) {
							if (rc.canMove(Direction.SOUTHWEST)) {
								rc.move(Direction.SOUTHWEST);
								return;
							} else if (rc.canMove(Direction.WEST)) {
								rc.move(Direction.WEST);
								return;
							}
						} else if (rc.canSenseLocation(tempLoc2)) {
							if (rc.canMove(Direction.NORTHEAST)) {
								rc.move(Direction.NORTHEAST);
								return;
							} else if (rc.canMove(Direction.NORTH)) {
								rc.move(Direction.NORTH);
								return;
							}
						} else {
							isAtCorner = true;
							System.out.println("At Southeast Corner");
						}
					}
					break;
				case 4:
					if (rc.canMove(Direction.SOUTHWEST)) {
						rc.move(Direction.SOUTHWEST);
						return;
					} else if (rc.canMove(Direction.SOUTH)) {
						rc.move(Direction.SOUTH);
						return;
					} else if (rc.canMove(Direction.WEST)) {
						rc.move(Direction.WEST);
						return;
					} else {
						MapLocation tempLoc = currentLoc;
						tempLoc = tempLoc.add(Direction.SOUTH);
						tempLoc = tempLoc.add(Direction.SOUTH);
						MapLocation tempLoc2 = currentLoc;
						tempLoc2 = tempLoc2.add(Direction.WEST);
						tempLoc = tempLoc.add(Direction.WEST);
						if (rc.canSenseLocation(tempLoc)) {
							if (rc.canMove(Direction.SOUTHEAST)) {
								rc.move(Direction.SOUTHEAST);
								return;
							} else if (rc.canMove(Direction.EAST)) {
								rc.move(Direction.EAST);
								return;
							}
						} else if (rc.canSenseLocation(tempLoc2)) {
							if (rc.canMove(Direction.NORTHWEST)) {
								rc.move(Direction.NORTHWEST);
								return;
							} else if (rc.canMove(Direction.NORTH)) {
								rc.move(Direction.NORTH);
								return;
							}
						} else {
							isAtCorner = true;
							System.out.println("At Southwest Corner");
						}
					}
					break;
				}
				if (isAtCorner) {
					flag += 10000000;
					int differenceX = currentLoc.x - ecLoc.x;
					int differenceY = currentLoc.y - ecLoc.y;
					if (differenceX < 0) {
						flag += 100000;
						flag += Math.abs(differenceX) * 1000;
					} else
						flag += Math.abs(differenceX) * 1000;
					if (differenceY < 0) {
						flag += 100;
						flag += Math.abs(differenceY);
					} else
						flag += Math.abs(differenceY);
					rc.setFlag(flag);
				}
			} else {
				Team enemy = rc.getTeam().opponent();
				int actionRadius = rc.getType().actionRadiusSquared;
				for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
					if (robot.type.canBeExposed()) {
						// It's a slanderer... go get them!
						if (rc.canExpose(robot.location)) {
							// System.out.println("e x p o s e d");
							rc.expose(robot.location);
							return;
						}
					}
				}
				tryMove(randomDirection());
			}
		}
		// 0 0 0 0 0 0 0 0
		// First Number is if it's finished or not
		// Second Number is direction
		// Third Number is if it's positive or negative
		// Fourth and Fifth combine to display difference in distance in X
		// Sixth is if it's positive or negative
		// Seventh and Eight combine to display difference in distance in Y

	}

	/**
	 * Returns a random Direction.
	 *
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
	}

	/**
	 * Returns a random spawnable RobotType
	 *
	 * @return a random RobotType
	 */
	static RobotType randomSpawnableRobotType() {
		return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
	}

	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMove(Direction dir) throws GameActionException {
		// System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " +
		// rc.getCooldownTurns() + " "
		// + rc.canMove(dir));
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}

	static void Stage1(RobotController rc) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(40, rc.getTeam());
		int nearbySlanderers = 0;
		int nearbyPoliticians = 0;
		for (int i = 0; i < nearbyRobots.length; i++) {
			if (nearbyRobots[i].type.equals(RobotType.SLANDERER))
				if (nearbyRobots[i].influence == 20)
					nearbySlanderers++;
			if (nearbyRobots[i].type.equals(RobotType.POLITICIAN))
				if (nearbyRobots[i].influence > 40)
					nearbyPoliticians++;
		}
		if (rc.isReady()) {
			if (nearbySlanderers < 2) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.SLANDERER, dir, 20)) {
						rc.buildRobot(RobotType.SLANDERER, dir, 20);
						System.out.println("Built Slanderer");
					}
				}
			} else if (nearbyPoliticians < 1) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 50)) {
						rc.buildRobot(RobotType.POLITICIAN, dir, 50);
						rc.setFlag(2685);
						System.out.println("Built Politician");
					}
				}
			}
			if (turnCount % 2 == 0) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 11)) {
						rc.buildRobot(RobotType.MUCKRAKER, dir, 11);
						System.out.println("Built Muckraker");
					}
				}
			} else {
				if (rc.canBid(5)) {
					rc.bid(5);
					System.out.println("Bidded for Vote");
				}
			}

		}
	}

	static void Stage2(RobotController rc) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(40, rc.getTeam());
		int nearbySlanderers = 0;
		int nearbyPoliticians = 0;
		for (int i = 0; i < nearbyRobots.length; i++) {
			if (nearbyRobots[i].type.equals(RobotType.SLANDERER))
				if (nearbyRobots[i].influence == 20)
					nearbySlanderers++;
			if (nearbyRobots[i].type.equals(RobotType.POLITICIAN))
				if (nearbyRobots[i].influence > 40)
					nearbyPoliticians++;
		}
		if (rc.isReady()) {
			if (nearbySlanderers < 10) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.SLANDERER, dir, 40)) {
						rc.buildRobot(RobotType.SLANDERER, dir, 40);
						System.out.println("Built Slanderer");
					}
				}
			} else if (nearbyPoliticians < 3) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 100)) {
						rc.buildRobot(RobotType.POLITICIAN, dir, 100);
						rc.setFlag(2685);
						System.out.println("Built Politician");
					}
				}
			}
			if (turnCount % 2 == 0) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 11)) {
						rc.buildRobot(RobotType.MUCKRAKER, dir, 11);
						System.out.println("Built Muckraker");
					}
				}
			} else {
				if (rc.canBid(10)) {
					rc.bid(10);
					System.out.println("Bidded for Vote");
				}
			}
		}

	}

	static void Stage3(RobotController rc) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots(40, rc.getTeam());
		int nearbySlanderers = 0;
		int nearbyPoliticians = 0;
		for (int i = 0; i < nearbyRobots.length; i++) {
			if (nearbyRobots[i].type.equals(RobotType.SLANDERER))
				if (nearbyRobots[i].influence == 20)
					nearbySlanderers++;
			if (nearbyRobots[i].type.equals(RobotType.POLITICIAN))
				if (nearbyRobots[i].influence > 40)
					nearbyPoliticians++;
		}
		if (rc.isReady()) {
			if (nearbySlanderers < 20) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.SLANDERER, dir, 40)) {
						rc.buildRobot(RobotType.SLANDERER, dir, 40);
						System.out.println("Built Slanderer");
					}
				}
			} else if (nearbyPoliticians < 5) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 100)) {
						rc.buildRobot(RobotType.POLITICIAN, dir, 100);
						rc.setFlag(2685);
						System.out.println("Built Politician");
					}
				}
			}
			if (turnCount % 2 == 0) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 20)) {
						rc.buildRobot(RobotType.MUCKRAKER, dir, 20);
						System.out.println("Built Muckraker");
					}
				}
			} else {
				if (rc.canBid(25)) {
					rc.bid(25);
					System.out.println("Bidded for Vote");
				}
			}
		}

	}

	static void Normal(RobotController rc) throws GameActionException {
		if (rc.isReady()) {

			if (turnCount % 2 == 0) {
				int bidAmount = 50;
				bidAmount = bidAmount * turnCount / 200;
				if (rc.canBid(bidAmount)) {
					rc.bid(bidAmount);
					System.out.println("Bidded for Vote");
				}
			} else {
				RobotType toBuild = randomSpawnableRobotType();
				int influence = 25;
				if (toBuild.equals(RobotType.POLITICIAN))
					influence = 50;
				for (Direction dir : directions) {
					if (rc.canBuildRobot(toBuild, dir, influence))
						rc.buildRobot(toBuild, dir, influence);
				}
			}
		}
	}

	static boolean isCornerRunner(RobotController rc) throws GameActionException {
		// checks Robot Flag's to see if it is a corner runner
		int flag = rc.getFlag(rc.getID());
		boolean isCornerRunner = false;
		if (flag / 1000000 > 0)
			isCornerRunner = true;
		return isCornerRunner;
	}

	/*
	 * 
	 */
	static int closestCorner(RobotController rc) throws GameActionException {
		int corner = 0;
		int currentDistance = 1000000;
		MapLocation currentLoc = rc.getLocation();
		int ecX = currentLoc.x;
		int ecY = currentLoc.y;
		for (int i = 0; i < createdMuckrakersID.size(); i++) {
			int temp = rc.getFlag(createdMuckrakersID.get(i));
			int x = 0;
			int y = 0;
			int tempCorner = 0;
			int tempDistance = 0;
			if (temp / 10000000 > 1) {
				tempCorner = (temp % 10000000) / 1000000;
				if (temp % 1000000 >= 100000) {
					x = 0 - (temp % 100000) / 1000;
					if (temp % 1000 >= 100)
						y = 0 - (temp % 100);
					else
						y = temp % 100;
				} else
					x = (temp % 100000) / 1000;
				MapLocation cornerML = currentLoc.translate(x, y);
				tempDistance = currentLoc.distanceSquaredTo(cornerML);
				if (tempDistance < currentDistance) {
					currentDistance = tempDistance;
					corner = tempCorner;
				}
			}
		}
		return corner;
	}
}
