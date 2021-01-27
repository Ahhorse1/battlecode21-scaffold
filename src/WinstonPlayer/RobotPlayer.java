package WinstonPlayer;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Arrays;

public strictfp class RobotPlayer {
	static RobotController rc;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	static final Direction[][] directionPriorityForCorner = {
			{ Direction.NORTHWEST, Direction.NORTH, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST,
					Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST },
			{ Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHWEST, Direction.SOUTHEAST,
					Direction.WEST, Direction.SOUTH, Direction.SOUTHWEST },
			{ Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHWEST,
					Direction.NORTH, Direction.WEST, Direction.NORTHWEST },
			{ Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.SOUTHEAST, Direction.NORTHWEST,
					Direction.NORTH, Direction.EAST, Direction.NORTHEAST } };
	static final Direction[][] directionPriorityForScouts = {
			{ Direction.NORTHWEST, Direction.NORTH, Direction.WEST, Direction.NORTHEAST, Direction.SOUTHWEST,
					Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST },
			{ Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.EAST, Direction.WEST,
					Direction.SOUTHWEST, Direction.SOUTHEAST, Direction.SOUTH },
			{ Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHWEST, Direction.SOUTHEAST,
					Direction.WEST, Direction.SOUTH, Direction.SOUTHWEST },
			{ Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.NORTH, Direction.SOUTH,
					Direction.NORTHWEST, Direction.SOUTHWEST, Direction.WEST },
			{ Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHWEST,
					Direction.NORTH, Direction.WEST, Direction.NORTHWEST },
			{ Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHEAST, Direction.WEST, Direction.EAST,
					Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH },
			{ Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.SOUTHEAST, Direction.NORTHWEST,
					Direction.NORTH, Direction.EAST, Direction.NORTHEAST },
			{ Direction.WEST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTH, Direction.NORTH,
					Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST } };

	static int turnCount;

	static boolean isCapturedEC = false;

	/**
	 * Variables that store ID and Location of EC that created the robot
	 */
	static MapLocation ecLoc;
	static int enlightenmentCenterID;

	static int lastRoundBid = 1;
	static int lastRoundVotes = 0;
	static int roundsPlateaued = 0;

	static int createdScouts = 0;
	static boolean swarmMuckraker = false;
	static int turnsWaiting;

	static int muckrakerMission;
	static int politicianMission = 0;

	/**
	 * stores schema of the communications and how much to shift each n-1 parameter
	 */
	static int[] schema = { 4, 7, 7, 6 };
	static int[] shift = { schema[1] + schema[2] + schema[3], schema[2] + schema[3], schema[3] };

	static ArrayList<MapLocation> enemyECs = new ArrayList<>();
	static ArrayList<MapLocation> friendlyECs = new ArrayList<>();
	static ArrayList<MapLocation> neutralECs = new ArrayList<>();
	static ArrayList<Integer> neutralECInf = new ArrayList<>();

	static ArrayList<Integer> unitIDs = new ArrayList<>();

	/**
	 * Indicates the mode for building armies Each mode lasts for 15 actions 1 =
	 * muckrakers, 2 = politicians, 3 = muckrakers
	 */
	static int[] modeCount = new int[2];

	/**
	 * Holds units for the first stage
	 */
	static int[][] stageOne = { { 0, 0 }, { 1302, 1072 } };
	static int[][] stageTwo = { { 0, 0, 0, 0, 0 }, { 201, 131, 131, 1302, 1072 } };

	/**
	 * Holds Booleans/int for flipping between different modes
	 */
	static Boolean stageTwoMode = false;
	static Boolean stageFourMode = false;
	static Boolean stageFiveMode = false;
	static Boolean stageSixMode = false;
	static int[] stageSevenModes = { 0, 0 };

	/**
	 * Holds all Influence Breakpoints v1 is for Slanderers v2 is for capturedEC
	 * Slanderers v3 is for Politcians
	 */
	static int[] breakpoints = { 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399, 431, 463, 497, 532, 605,
			643, 683, 724, 766, 810, 855, 902, 949, 999, 1049, 1101, 1155, 1209, 1265, 1322, 1380, 1438, 1498, 1559,
			1619, 1681, 1743, 1805, 1868, 1930, 1993, 2244, 2491, 2734, 3031, 3320, 3604, 3990, Integer.MAX_VALUE };

	static int[] breakpointsv2 = { 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255, 282, 310, 339, 368, 399, 431, 463,
			497, 532, 605, 643, 683, 724, 766, 810, 855, 902, 949, 999, 1049, 1101, 1155, 1209, 1265, 1322, 1380, 1438,
			1498, 1559, 1619, 1681, 1743, 1805, 1868, 1930, 1993, 2244, 2491, 2734, 3031, 3320, 3604, 3990,
			Integer.MAX_VALUE };

	static int[] breakpointsv3 = { 20, 40, 50, 75, 100, 150, 200, Integer.MAX_VALUE };

	static int nECpoliticians = 0;

	/**
	 * protecting: If a unit is protecting a slanderer/EC hasDesintation: If a unit
	 * has a destination atDestination : Unit is at destination latticeDestination :
	 * Lattice structure destination for unit destination: MapLocation containing
	 * the destination
	 */
	static boolean protecting = false;
	static boolean hasDestination = false;
	static boolean atDestination = false;

	static MapLocation latticeDestination;
	static MapLocation destination;

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

		while (true) {
			turnCount += 1;
			// Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
			try {
				// Here, we've separated the controls into a different method for each
				// RobotType.
				// You may rewrite this into your own control structure if you wish.
				switch (rc.getType()) {
				case ENLIGHTENMENT_CENTER:
					if (turnCount == 1 && rc.getInfluence() != 150)
						isCapturedEC = true;
					if (isCapturedEC) {
						runCapturedEnlightenmentCenter();
					} else
						runEnlightenmentCenter();
					break;
				case POLITICIAN:
					runPolitician();
					break;
				case SLANDERER:
					runSlanderer();
					break;
				case MUCKRAKER:
					runMuckraker();
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

	/**
	 * To be run by an EC to scan through UNIT flags, updating information
	 * 
	 * @throws GameActionException
	 */
	static void seeWhatsUp() throws GameActionException {
		boolean needsSupport = false;
		int supportFlag = 0;
		boolean setFlag = false;
		if (unitIDs.size() > 0) {
			int id;
			int[] flag;
			MapLocation loc;
			for (int i = 0; i < unitIDs.size(); i++) {
				id = unitIDs.get(i);
				if (rc.canGetFlag(id)) {
					if (rc.getFlag(id) > 0) {
						flag = decodeFlag(rc.getFlag(id));
						loc = getMapLocation(flag[1], flag[2]);
						switch (flag[0]) {
						case 1:// enemy EC
							if (!enemyECs.contains(loc))
								enemyECs.add(loc);
							friendlyECs.remove(loc);
							if (neutralECs.contains(loc)) {
								int index = neutralECs.indexOf(loc);
								neutralECs.remove(loc);
								neutralECInf.remove(index);
							}
							if (setFlag == false)
								rc.setFlag(encodeFlag(1, loc));
							break;
						case 2: // neutral HQ
							if (!neutralECs.contains(loc)) {
								neutralECs.add(loc);
								int influence = flag[3];
								neutralECInf.add(influence);
							}
							if (setFlag == false)
								rc.setFlag(encodeFlag(2, loc));
							break;
						case 3: // Friendly EC
							if (!friendlyECs.contains(loc)) {
								friendlyECs.add(loc);
								setFlag = true;
								rc.setFlag(encodeFlag(3, loc));
							}
							enemyECs.remove(loc);
							if (neutralECs.contains(loc)) {
								int index = neutralECs.indexOf(loc);
								neutralECInf.remove(index);
							}
							neutralECs.remove(loc);
							break;
						case 12: // Slanderer storm
							needsSupport = true;
							supportFlag = encodeFlag(12, loc);
							break;
						case 15:
							unitIDs.remove(i);
							break;
						}
					}
				} else {
					unitIDs.remove(i);// This means the robot went bye-bye
				}
			}
		}
		System.out.println("RECOGNIZING: " + neutralECs.size() + " Neutral ECs & " + enemyECs.size()
				+ " enemy ones and " + friendlyECs.size() + " friendly ones");
		// Actually set the flag
		if (setFlag == true)
			return;
		if (neutralECs.size() > 0 && turnCount % 2 == 0) {
			rc.setFlag(encodeFlag(2, neutralECs.get(0)));
		} else if (enemyECs.size() > 0 && turnCount % 2 == 1) {
			rc.setFlag(encodeFlag(1, enemyECs.get(0)));
		} else if (needsSupport && turnCount % 3 == 0) {
			rc.setFlag(supportFlag);
		}
	}

	static void runEnlightenmentCenter() throws GameActionException {

		seeWhatsUp();

		if (turnCount <= 20) {
			runStageOne();
		} else if (turnCount <= 100) {
			runStageTwo();
		} else if (turnCount <= 120) {
			runStageThree();
		} else if (turnCount <= 150) {
			runStageFour();
		} else if (turnCount <= 200) {
			runStageFive();
		} else if (turnCount <= 350) {
			runStageSix();
		} else {
			runStageSeven();
		}

		if (turnCount == 1490) {
			rc.setFlag(encodeFlag(14, 0, 0, 0));
		}
	}

	static void runCapturedEnlightenmentCenter() throws GameActionException {
		seeWhatsUp();
		if (turnCount <= 20) {
			if (rc.isReady()) {
				int x = nearestBreakpointv2();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					return;
				}
			}
		}
		runCapturedStage();
	}

	static void runPolitician() throws GameActionException {
		rc.setFlag(0);
		if (turnCount == 1 && rc.getFlag(rc.getID()) == 0)
			firstTurn();
		else if (turnCount == 1)
			convertedPolitician();

		if (politicianMission >= 10 && politicianMission <= 13 && turnCount >= 40) {// Don't worry about lattice after a
																					// couple turns
			politicianMission = 15;
		}
		Team enemy = rc.getTeam().opponent();
		MapLocation currentLoc = rc.getLocation();
		MapLocation loc = currentLoc;// Stores info temporarily
		int sensorRadius = rc.getType().sensorRadiusSquared;
		int[] ecFlag = new int[1];

		// Update info from EC
		if (rc.canGetFlag(enlightenmentCenterID)) {
			ecFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));
			loc = getMapLocation(ecFlag[1], ecFlag[2]);
			switch (ecFlag[0]) {
			case 1:// enemy EC
				if (!enemyECs.contains(loc))
					enemyECs.add(loc);
				friendlyECs.remove(loc);
				neutralECs.remove(loc);
				break;
			case 2: // neutral HQ
				if (!neutralECs.contains(loc))
					neutralECs.add(loc);
				break;
			case 3: // Friendly EC
				if (!friendlyECs.contains(loc))
					friendlyECs.add(loc);
				enemyECs.remove(loc);
				neutralECs.remove(loc);
				break;
			case 14:
				RobotInfo[] nearby = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemy);
				if (nearby.length > 0) {
					rc.empower(rc.getType().actionRadiusSquared);
				}
			}
		} else {
			enlightenmentCenterID = 0;
		}
		// Scan nearby robots, update stuff
		RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius);
		for (RobotInfo robot : nearby) {
			loc = robot.getLocation();
			if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {// Found an EC
				if (robot.getTeam().equals(Team.NEUTRAL) && !neutralECs.contains(loc)) {
					neutralECs.add(loc);
					rc.setFlag(encodeFlag(2, loc, encodeNeutralECInfluence(robot.getInfluence())));
				} else if (robot.getTeam().equals(rc.getTeam()) && !friendlyECs.contains(loc)) {
					friendlyECs.add(loc);
					rc.setFlag(encodeFlag(3, loc));
					neutralECs.remove(loc);
				} else if (robot.getTeam().equals(enemy) && !enemyECs.contains(loc)) {// Found an enemy EC
					enemyECs.add(loc);
					rc.setFlag(encodeFlag(1, loc));
					neutralECs.remove(loc);
					friendlyECs.remove(loc);
				}
			}
		}
		System.out.println("RECOGNIZING: " + neutralECs.size() + " Neutral ECs & " + enemyECs.size()
				+ " enemy ones and " + friendlyECs.size() + " friendly ones");

		if (rc.getFlag(rc.getID()) == 0 && turnCount <= 4) {
			if (rc.getInfluence() % 10 == 0) {
				politicianMission = 10 + (int) (Math.random() * ((13 - 10) + 1));
				rc.setFlag(encodeFlag(0, 0, 0, 10));
			}
		}

		if (rc.isReady()) {
			if (politicianMission >= 10 && politicianMission <= 13) 
				surroundSlanderers();
		} else
			return; // Everything following this is action based

		// Attempt to storm neutral ECs
		if (neutralECs.size() > 0) {
			MapLocation target = neutralECs.get(0);
			int minDistance = currentLoc.distanceSquaredTo(neutralECs.get(0));
			for (MapLocation x : neutralECs) {
				System.out.println("NEUTRAL W/ DISTANCE: " + currentLoc.distanceSquaredTo(x));
				if (currentLoc.distanceSquaredTo(x) < minDistance) {
					minDistance = currentLoc.distanceSquaredTo(x);
					target = x;
				}
			}
			int dist = currentLoc.distanceSquaredTo(target);
			if (dist <= 2 || (dist <= rc.getType().actionRadiusSquared && rc.detectNearbyRobots(dist).length <= 1)) {
				// Empower if we're close enough or if nothing's in the way
				rc.empower(dist);
				return;
			} else if (navigateTo(target)) {
				return;
			} else if (dist <= rc.getType().actionRadiusSquared) {// We may be blocked from attacking
				rc.empower(dist);
				return;
			}
		}

		// Attempt to attack a nearby unit
		RobotInfo[] attackable = rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent());
		int distTo;
		if (attackable.length > 0) {
			for (RobotInfo robot : attackable) {
				distTo = robot.getLocation().distanceSquaredTo(currentLoc);
				if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
					if (distTo <= 2) {
						rc.empower(distTo);
						return;
					}
					if (navigateTo(robot.getLocation()))
						return;
					rc.empower(distTo);
					return;
				}
			}
			for (RobotInfo robot : attackable) {
				distTo = robot.getLocation().distanceSquaredTo(currentLoc);
				if (distTo <= 2 || rc.senseNearbyRobots(distTo, rc.getTeam()).length <= 1) {
					rc.empower(distTo);
					return;
				}
			}
		}

		if (enemyECs.size() > 0) {
			MapLocation targetEC = enemyECs.get(0);
			if (navigateTo(targetEC))
				return;
			else if (rc.canMove(currentLoc.directionTo(targetEC).rotateLeft().rotateLeft())) {
				rc.move(currentLoc.directionTo(targetEC).rotateLeft().rotateLeft());
			} else if (rc.canMove(currentLoc.directionTo(targetEC).rotateRight().rotateRight())) {
				rc.move(currentLoc.directionTo(targetEC).rotateRight().rotateRight());
			}
		}

		// If all else fails, do something
		doRandomMove();

	}

	static void runSlanderer() throws GameActionException {
		if (turnCount == 1) {
			RobotInfo[] nearby = rc.senseNearbyRobots(2, rc.getTeam());
			for (RobotInfo robot : nearby) {
				if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
					enlightenmentCenterID = robot.getID();
					ecLoc = robot.getLocation();
				}
			}
			setDestination();
		}

		if (!hasDestination) {
			setDestination();
		} else if (hasDestination && !atDestination) {
			if (rc.canSenseLocation(latticeDestination)) {
				RobotInfo robot = rc.senseRobotAtLocation(latticeDestination);
				if (robot != null) {
					if (robot.getType().equals(RobotType.POLITICIAN) && decodeFlag(rc.getFlag(robot.ID))[3] == 1) {
						System.out.println("Updating Destination");
						updateDestination();
					}
				}
			}
		}

		if (rc.getLocation().equals(latticeDestination)) {
			atDestination = true;
			rc.setFlag(encodeFlag(0, 0, 0, 1));
		}

		if (hasDestination && decodeFlag(rc.getFlag(rc.getID()))[3] != 1) {
			if (rc.isReady()) {
				MapLocation currentLoc = rc.getLocation();
				if (rc.canMove(currentLoc.directionTo(latticeDestination))) {
					rc.move(currentLoc.directionTo(latticeDestination));
				} else if (rc.canMove(currentLoc.directionTo(latticeDestination).rotateLeft())) {
					rc.move(currentLoc.directionTo(latticeDestination).rotateLeft());
				} else if (rc.canMove(currentLoc.directionTo(latticeDestination).rotateRight())) {
					rc.move(currentLoc.directionTo(latticeDestination).rotateRight());
				} else if (rc.canMove(currentLoc.directionTo(latticeDestination).rotateLeft().rotateLeft())) {
					rc.move(currentLoc.directionTo(latticeDestination).rotateLeft().rotateLeft());
				} else if (rc.canMove(currentLoc.directionTo(latticeDestination).rotateRight().rotateRight())) {
					rc.move(currentLoc.directionTo(latticeDestination).rotateRight().rotateRight());
				}
			}
		}

		// System.out.println("Destination : (" + latticeDestination.x + "," +
		// latticeDestination.y + ")");
		if (rc.isReady() && !atDestination) {
			doRandomMove();
		}
	}

	static void convertedPolitician() throws GameActionException {
		enlightenmentCenterID = rc.getFlag(rc.getID());
		rc.setFlag(0);
	}

	static void runMuckraker() throws GameActionException {
		// Define some constants
		rc.setFlag(0);
		int senseRadius = RobotType.MUCKRAKER.detectionRadiusSquared;
		int actionRadius = RobotType.MUCKRAKER.actionRadiusSquared;
		Team enemy = rc.getTeam().opponent();

		// First turn stuffs
		if (turnCount == 1) {
			firstTurn();// Sets ECFlag
			int[] ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));
			if (ECFlag[3] >= 2 && ECFlag[3] <= 9) {
				System.out.println("I HAVE MISSION: " + ECFlag[3]);
				muckrakerMission = ECFlag[3];// Get our mission; see strategy doc for meaning. It'll be between 2 and 9
			}
		}

		MapLocation targetDestination = rc.getLocation();
		boolean haveDestination = false;

		if (turnCount == 200) {
			swarmMuckraker = true;
			rc.setFlag(encodeFlag(15, 0, 0, 0));
		}

		if (swarmMuckraker) {
			MapLocation currentLoc = rc.getLocation();

			// Update info from EC
			if (rc.canGetFlag(enlightenmentCenterID)) {
				int[] ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));
				MapLocation loc = getMapLocation(ECFlag[1], ECFlag[2]);
				if (ECFlag[0] == 1)
					if (!enemyECs.contains(loc))
						enemyECs.add(loc);
				if (ECFlag[0] == 3) {
					enemyECs.remove(loc);
					if (targetDestination.equals(loc))
						hasDestination = false;
				}
			}

			// Update info
			RobotInfo[] nearby = rc.senseNearbyRobots(senseRadius);
			for (RobotInfo robot : nearby) {
				if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
					MapLocation loc = robot.getLocation();
					if (robot.getTeam().equals(rc.getTeam())) {
						enemyECs.remove(loc);
						if (targetDestination.equals(loc))
							hasDestination = false;
					} else if (robot.getTeam().equals(enemy)) {
						enemyECs.add(loc);
						hasDestination = true;
						destination = loc;
					}
				}
			}

			// Get destination
			if (!hasDestination && enemyECs.size() > 0) {
				targetDestination = enemyECs.get(0);
				hasDestination = true;
			}
			if (rc.isReady()) {
				// Attempt to expose slanderer. Prioritize highest influence
				int highestInfluence = 0;
				int targetID = rc.getID();
				nearby = rc.senseNearbyRobots(actionRadius, enemy);
				for (RobotInfo robot : nearby) {
					if (robot.getType().canBeExposed()) {
						if (rc.senseRobotAtLocation(robot.getLocation()).getInfluence() > highestInfluence) {
							highestInfluence = rc.senseRobotAtLocation(robot.getLocation()).getInfluence();
							targetID = robot.getID();
						}
					}
				}
				if (highestInfluence > 0) {
					rc.expose(targetID);
					return;
				}

				if (hasDestination) {
					// System.out.println("Am Swarming" + targetDestination.x + ", " +
					// targetDestination.y);
					if (navigateTo(targetDestination)) {
						return;
					} else if (rc.canMove(currentLoc.directionTo(targetDestination).rotateLeft().rotateLeft())) {
						rc.move(currentLoc.directionTo(targetDestination).rotateLeft().rotateLeft());
						return;
					} else if (rc.canMove(currentLoc.directionTo(targetDestination).rotateRight().rotateRight())) {
						rc.move(currentLoc.directionTo(targetDestination).rotateRight().rotateRight());
						return;
					}
				}

			}
			Direction anti = antiGroupingMovement();
			if (rc.canMove(anti)) {// Run antigrouping stuff
				rc.move(anti);
				return;
			} else
				doRandomMove();
			return;
		}

		// 1 Collect information from ECflag
		if (rc.canGetFlag(enlightenmentCenterID)) {
			int[] ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));
			MapLocation loc = getMapLocation(ECFlag[1], ECFlag[2]);
			switch (ECFlag[0]) {
			case 1:// enemy EC
				if (!enemyECs.contains(loc))
					enemyECs.add(loc);
				friendlyECs.remove(loc);
				neutralECs.remove(loc);
				break;
			case 2: // neutral HQ
				if (!neutralECs.contains(loc))
					neutralECs.add(loc);
				break;
			case 3: // Friendly EC
				if (!friendlyECs.contains(loc))
					friendlyECs.add(loc);
				enemyECs.remove(loc);
				neutralECs.remove(loc);
				break;
			case 12: // A bro needs help (there's an enemy slanderer storm somewhere)
				targetDestination = loc;
				haveDestination = true;
				break;
			}
		}

		// 2 scan surroundings
		int enemySlandererCnt = 0;
		boolean flagSet = false;// Set this to true if we've already set our flag to something important
		RobotInfo[] nearby = rc.senseNearbyRobots(senseRadius);
		for (RobotInfo robot : nearby) {
			if (robot.getType().canBeExposed()) {
				if (robot.getTeam().equals(enemy) && robot.getType().equals(RobotType.SLANDERER))
					enemySlandererCnt++;
				else if (robot.getTeam().equals(rc.getTeam()) && robot.getType().equals(RobotType.SLANDERER)) {
					int[] flag = decodeFlag(rc.getFlag(robot.getID()));
					if (flag[0] == 12) {
						targetDestination = getMapLocation(flag[1], flag[2]);
						haveDestination = true;
					}
				}
			} else if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				if (robot.getTeam().equals(enemy) && !enemyECs.contains(robot.getLocation())) {
					rc.setFlag(encodeFlag(1, robot.getLocation()));
					flagSet = true;
				} else if (robot.getTeam().equals(Team.NEUTRAL)) {
					// We've found a neutral enlightenment center!!!! Set flag at all costs
					rc.setFlag(encodeFlag(2, robot.getLocation(), encodeNeutralECInfluence(robot.getInfluence())));
					flagSet = true;
				} else if (robot.getTeam().equals(rc.getTeam()) && !friendlyECs.contains(robot.getLocation())) {
					flagSet = true;
					rc.setFlag(encodeFlag(3, robot.getLocation()));
				}
			}
		}
		if (enemySlandererCnt >= 4 && !flagSet) { // notificationCutoff is set at 4, can increase whenever
			// If the number of enemies warrants calling for reinforcements and there's not
			// more important info to send
			rc.setFlag(encodeFlag(12, rc.getLocation()));
		}

		if (muckrakerMission >= 2 && muckrakerMission <= 9)
			if (rc.isReady())
				searchOtherECs();
			else
				return;

		// Now in action phase of muckraker logic
		if (!rc.isReady())
			return;

		// Find a target to expose. Prioritize one with high influence
		int highestInfluence = 0;
		int targetID = rc.getID();
		nearby = rc.senseNearbyRobots(actionRadius, enemy);
		for (RobotInfo robot : nearby) {
			if (robot.getType().canBeExposed()) {
				if (rc.senseRobotAtLocation(robot.getLocation()).getInfluence() > highestInfluence) {
					highestInfluence = rc.senseRobotAtLocation(robot.getLocation()).getInfluence();
					targetID = robot.getID();
				}
			}
		}
		if (highestInfluence > 0) {
			rc.expose(targetID);
			return;
		}

		// Move somewhere based on destination, then antigrouping, then randomly
		Direction anti = antiGroupingMovement();
		if (haveDestination && navigateTo(targetDestination))
			return;
		else if (rc.canMove(anti))// Run antigrouping stuff
			rc.move(anti);
			return;
		else {
			doRandomMove();
		}

	}

	/**
	 * Moves the robot to lower-density areas based on which quadrants are emptier
	 * and passability
	 * 
	 * @return a direction for the robot to move in
	 * @throws GameActionException
	 */
	static Direction antiGroupingMovement() throws GameActionException {
		int selfX = rc.getLocation().x;
		int selfY = rc.getLocation().y;
		int sensorRadius = rc.getType().sensorRadiusSquared;
		int quadrantOne = 0, quadrantTwo = 0, quadrantThree = 0, quadrantFour = 0;
		boolean furtherX;
		boolean furtherY;
		RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius, rc.getTeam());
		for (RobotInfo robot : nearby) {
			MapLocation location = robot.getLocation();
			furtherX = (location.x >= selfX);
			furtherY = (location.y >= selfY);
			if (furtherX && furtherY)
				quadrantOne++; // North east; top left
			else if (furtherX && !furtherY)
				quadrantTwo++; // South east; bottom left
			else if (!furtherX && !furtherY)
				quadrantThree++; // South west; bottom right
			else
				quadrantFour++; // North west; top right
		}
		// Define passabilities of directions
		double north = 0, northeast = 0, east = 0, southeast = 0, south = 0, southwest = 0, west = 0, northwest = 0;// Default
																													// to
																													// entirely
																													// unpassable
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.WEST)))
			west = rc.sensePassability(rc.adjacentLocation((Direction.WEST)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.NORTHWEST)))
			northwest = rc.sensePassability(rc.adjacentLocation((Direction.NORTHWEST)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.NORTH)))
			north = rc.sensePassability(rc.adjacentLocation((Direction.NORTH)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.NORTHEAST)))
			northeast = rc.sensePassability(rc.adjacentLocation((Direction.NORTHEAST)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.EAST)))
			east = rc.sensePassability(rc.adjacentLocation((Direction.EAST)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.SOUTH)))
			south = rc.sensePassability(rc.adjacentLocation((Direction.SOUTH)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.SOUTHWEST)))
			southwest = rc.sensePassability(rc.adjacentLocation((Direction.SOUTHWEST)));
		if (rc.canSenseLocation(rc.adjacentLocation(Direction.SOUTHEAST)))
			southeast = rc.sensePassability(rc.adjacentLocation((Direction.SOUTHEAST)));

		if (quadrantOne == quadrantTwo && quadrantTwo == quadrantThree && quadrantThree == quadrantFour) {
			quadrantOne = (int) ((Math.random() + 1) * 10);
			quadrantTwo = (int) ((Math.random() + 1) * 10);
			quadrantThree = (int) ((Math.random() + 1) * 10);
			quadrantFour = (int) ((Math.random() + 1) * 10);
		}
		if (rc.canSenseLocation(rc.getLocation().translate(2, 2)) && quadrantOne < quadrantTwo
				&& quadrantOne < quadrantThree && quadrantOne < quadrantFour) {
			// Go to quadrant I assuming that it's on the map
			if (northeast >= north && northeast >= east)// Go to most passable areas
				return Direction.NORTHEAST;
			else if (north > east)
				return Direction.NORTH;
			return Direction.EAST;
		}
		if (rc.canSenseLocation(rc.getLocation().translate(2, -2)) && quadrantTwo <= quadrantThree
				&& quadrantTwo <= quadrantFour) {
			// Go to quadrant II assuming it's not walled off
			if (southeast >= east && southeast >= south)
				return Direction.SOUTHEAST;
			else if (south > east)
				return Direction.SOUTH;
			return Direction.EAST;
		}
		if (rc.canSenseLocation(rc.getLocation().translate(-2, -2)) && quadrantThree <= quadrantFour) {
			// Go to quadrant III assuming it's within the map
			if (southwest >= south && southwest >= west)
				return Direction.SOUTHWEST;
			else if (south > west)
				return Direction.SOUTH;
			return Direction.WEST;
		}

		if (rc.canSenseLocation(rc.getLocation().translate(-2, 2))) {
			// Go to quadrant IV if not going to any of the other quadrants
			if (northwest >= north && northwest >= west)
				return Direction.NORTHWEST;
			else if (west > north)
				return Direction.WEST;
			return Direction.NORTH;
		}
		return randomDirection();
	}

// Enlightenment Center Methods Below

	static void runStageOne() throws GameActionException {
		if (turnCount == 1) {
			rc.bid(1);
		}
		if (rc.isReady()) {
			for (int i = 0; i < 2; i++) {
				if (stageOne[0][i] == 0) {
					int toBuildCode = stageOne[1][i];
					RobotType toBuild = decodeBuild(toBuildCode);
					if (canConstruct(toBuild, toBuildCode / 10)) {
						construct(toBuild, toBuildCode / 10);
						stageOne[0][i] = 1;
						return;
					}
				}
			}
			if (createdScouts < 8) {
				createScouts();// Create a scout
			} else if (canConstruct(RobotType.MUCKRAKER, 1)) {
				construct(RobotType.MUCKRAKER, 1);
				int[] flag = decodeFlag(rc.getFlag(rc.getID()));
				rc.setFlag(encodeFlag(flag[0], flag[1], flag[2], 0));
			}
		}
	}

	/**
	 * Uses variable createdScouts See strategy doc for what 0-7 encodes for and
	 * what flag 2-9 encodes
	 */
	public static void createScouts() throws GameActionException {
		for (Direction dir : directionPriorityForScouts[createdScouts]) {
			if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1)) {
				rc.buildRobot(RobotType.MUCKRAKER, dir, 1);
				System.out.println("CREATING SCOUNT N=" + createdScouts);
				int[] flag = decodeFlag(rc.getID());
				rc.setFlag(encodeFlag(flag[0], flag[1], flag[2], createdScouts + 2));// Encode information for scouts
				createdScouts++;
				unitIDs.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());// Important
			}
		}
	}

	/**
	 * Either Builds or Bids depending on buildUnits. Run by EC from 555-1500
	 * 
	 * @throws GameActionException
	 */
	static void runStageTwo() throws GameActionException {
		smartBid();

		if (rc.isReady()) {
			/**
			 * if (makeCornerRunner[0] || makeCornerRunner[1] || makeCornerRunner[2] ||
			 * makeCornerRunner[3]) { runCorner(); }
			 **/

			for (int i = 0; i < 3; i++) {
				if (stageTwo[0][i] == 0) {
					int toBuildCode = stageTwo[1][i];
					RobotType toBuild = decodeBuild(toBuildCode);
					if (canConstruct(toBuild, toBuildCode / 10)) {
						construct(toBuild, toBuildCode / 10);
						stageTwo[0][i] = 1;
						return;
					}
				}
			}
			if (stageTwoMode) {
				if (canConstruct(RobotType.POLITICIAN, 20)) {
					construct(RobotType.POLITICIAN, 20);
					stageTwoMode = false;
					return;
				}
			} else {
				int x = nearestBreakpoint();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageTwoMode = true;
					return;
				}
			}
			if (canConstruct(RobotType.MUCKRAKER, 1))
				construct(RobotType.MUCKRAKER, 1);
		}

	}

	static void runStageThree() throws GameActionException {
		smartBid();

		if (neutralECs.size() > nECpoliticians) {
			int neutralInfluence = (neutralECInf.get(nECpoliticians) % 40 + 1) * 50 + 11;
			if (rc.getInfluence() >= neutralInfluence) {
				if (canConstruct(RobotType.POLITICIAN, neutralInfluence))
					construct(RobotType.POLITICIAN, neutralInfluence);
				nECpoliticians++;
			}
		}
		if (canConstruct(RobotType.MUCKRAKER, 1)) {
			construct(RobotType.MUCKRAKER, 1);
		}
	}

	static void runStageFour() throws GameActionException {
		smartBid();

		if (rc.isReady()) {
			if (stageFourMode) {
				int x = nearestBreakpoint();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageFourMode = false;
				}
			} else {
				int x = nearestBreakpoint();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageFourMode = true;
				}
			}
		}
		if (canConstruct(RobotType.MUCKRAKER, 1))
			construct(RobotType.MUCKRAKER, 1);

		nECpoliticians = 0;
	}

	static void runStageFive() throws GameActionException {
		smartBid();
		/**
		 * if (rc.getInfluence() > 125) { int random = 15 + (int) (Math.random() * 10);
		 * rc.bid(random); } else if (rc.canBid(15)) { int random = 10 +
		 * (int)(Math.random() * 5); rc.bid(random); } else if(rc.canBid(1)){ rc.bid(1);
		 * }
		 **/
		if (neutralECs.size() > nECpoliticians) {
			int neutralInfluence = (neutralECInf.get(nECpoliticians) % 40 + 1) * 50 + 11;
			if (rc.getInfluence() >= neutralInfluence) {
				if (canConstruct(RobotType.POLITICIAN, neutralInfluence))
					construct(RobotType.POLITICIAN, neutralInfluence);
				nECpoliticians++;
			}
		}

		else if (rc.isReady()) {
			if (stageFiveMode) {
				if (canConstruct(RobotType.POLITICIAN, 100)) {
					construct(RobotType.POLITICIAN, 100);
					stageFiveMode = false;
					return;
				}
			} else {
				int x = nearestBreakpoint();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageFiveMode = true;
					return;
				}
			}
			if (canConstruct(RobotType.MUCKRAKER, 1)) {
				construct(RobotType.MUCKRAKER, 1);
			}
		}

	}

	static void runStageSix() throws GameActionException {
		smartBid();
		/**
		 * if (rc.getInfluence() > 250) { int random = 25 + (int)(Math.random() * 25);
		 * rc.bid(random); } else if (rc.canBid(50)) { int random = 5 +
		 * (int)(Math.random() * 5); rc.bid(random); }else if(rc.canBid(1)){ rc.bid(1);
		 * }
		 **/

		if (rc.isReady()) {
			if (stageSixMode) {
				if (canConstruct(RobotType.POLITICIAN, 150)) {
					construct(RobotType.POLITICIAN, 150);
					stageSixMode = false;
					return;
				}
			} else {
				int x = nearestBreakpoint();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageSixMode = false;
					return;
				}
			}
			if (canConstruct(RobotType.MUCKRAKER, 1)) {
				construct(RobotType.MUCKRAKER, 1);
			}
		}

	}

	static void runStageSeven() throws GameActionException {
		smartBid();
		if (rc.isReady()) {
			if (stageSevenModes[0] == 0) {
				int x = nearestBreakpointv3();
				if (canConstruct(RobotType.POLITICIAN, x)) {
					construct(RobotType.POLITICIAN, x);
					stageSevenModes[1] += 1;
				}
				if (stageSevenModes[1] >= 10) {
					stageSevenModes[0] = 1;
					stageSevenModes[1] = 0;
				}
			} else if (stageSevenModes[0] == 1) {
				int x = nearestBreakpoint();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageSevenModes[1] += 1;
				}
				if (stageSevenModes[1] >= 10) {
					stageSevenModes[0] = 2;
					stageSevenModes[1] = 0;
				}
			} else if (stageSevenModes[0] == 2) {
				if (canConstruct(RobotType.MUCKRAKER, 1)) {
					construct(RobotType.MUCKRAKER, 1);
					stageSevenModes[1] += 1;
				}
				if (stageSevenModes[1] >= 25) {
					stageSevenModes[0] = 0;
					stageSevenModes[1] = 0;
				}
			}
		}

	}

	static void smartBid() throws GameActionException {
		int currentVotes = rc.getTeamVotes();
		boolean voteGained = currentVotes > lastRoundVotes;
		if (rc.canBid(lastRoundBid) && voteGained) {
			if (roundsPlateaued == 15) {
				lastRoundBid /= 2;
				roundsPlateaued = 0;
			} else {
				rc.bid(lastRoundBid);
				roundsPlateaued++;
			}
		} else if (turnCount < 400) {
			if (rc.canBid(lastRoundBid + 1)) {
				rc.bid(lastRoundBid + 1);
				lastRoundBid += 1;
			}
		} else {
			if (rc.canBid(lastRoundBid + (int) (0.2 * Math.sqrt(turnCount)))) {
				rc.bid(lastRoundBid + (int) (0.2 * Math.sqrt(turnCount)));
				lastRoundBid += (int) (0.2 * Math.sqrt(turnCount));
			}
		}
		lastRoundVotes = currentVotes;
	}

	static void runCapturedStage() throws GameActionException {
		// smartBid();
		if (turnCount >= 15) {
			if (rc.getInfluence() > 400) {
				int random = 50 + (int) ((Math.random()) * 50); // Between 50 and 100
				rc.bid(random);
			} else if (rc.canBid(10)) {
				int random = 5 + (int) ((Math.random() + .1) * 5); // Between 5 and 10
				rc.bid(random);
			} else if (rc.canBid(1)) {
				rc.bid(1);
			}
		}
		if (rc.isReady()) {
			if (stageSevenModes[0] == 0) {
				int x = nearestBreakpointv3();
				if (canConstruct(RobotType.POLITICIAN, x)) {
					construct(RobotType.POLITICIAN, x);
					stageSevenModes[1] += 1;
				}
				if (stageSevenModes[1] >= 10) {
					stageSevenModes[0] = 1;
					stageSevenModes[1] = 0;
				}
			} else if (stageSevenModes[0] == 1) {
				int x = nearestBreakpointv2();
				if (canConstruct(RobotType.SLANDERER, x)) {
					construct(RobotType.SLANDERER, x);
					stageSevenModes[1] += 1;
				}
				if (stageSevenModes[1] >= 10) {
					stageSevenModes[0] = 2;
					stageSevenModes[1] = 0;
				}
			} else {
				if (canConstruct(RobotType.MUCKRAKER, 1)) {
					construct(RobotType.MUCKRAKER, 1);
					stageSevenModes[1] += 1;
				}
				if (stageSevenModes[1] >= 25) {
					stageSevenModes[0] = 0;
					stageSevenModes[1] = 0;
				}
			}
		}

	}

	static int nearestBreakpoint() throws GameActionException {
		for (int i = 1; i < breakpoints.length; i++) {
			if (breakpoints[i] > rc.getInfluence()) {
				return breakpoints[i - 1];
			}
		}
		return 0;
	}

	static int nearestBreakpointv2() throws GameActionException {
		for (int i = 1; i < breakpointsv2.length; i++) {
			if (breakpointsv2[i] > rc.getInfluence()) {
				return breakpointsv2[i - 1];
			}
		}
		return 0;
	}

	static int nearestBreakpointv3() throws GameActionException {
		for (int i = 1; i < breakpointsv3.length; i++) {
			if (breakpointsv3[i] > rc.getInfluence()) {
				return breakpointsv3[i - 1];
			}
		}
		return 0;
	}

	static RobotType decodeBuild(int x) {
		int y = x % 10;
		if (y == 1) {
			return RobotType.POLITICIAN;
		} else if (y == 2) {
			return RobotType.SLANDERER;
		} else {
			return RobotType.MUCKRAKER;
		}
	}

	/**
	 * Builds a Robot if possible
	 * 
	 * @requires typeToBuild is a valid RobotType, rc.getInfluence >= influence, rc
	 *           is an EC, and there is a location for the Robot to be built
	 * @returns MapLocation where the Robot has been built, unless no Robot is
	 *          built, it returns null.
	 * @ensures Robot is built and MapLocation is the correct location of the Robot.
	 * @throws GameActionException
	 */
	static MapLocation construct(RobotType typeToBuild, int influence) throws GameActionException {
		RobotType toBuild = typeToBuild;

		// Find most passable direction
		double highestPassability = -1;
		double tempPassability;
		Direction bestDir = Direction.CENTER;
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				tempPassability = rc.sensePassability(rc.adjacentLocation(dir));
				if (tempPassability > highestPassability) {
					highestPassability = tempPassability;
					bestDir = dir;
				}
			}
		}
		if (highestPassability > -1) {
			rc.buildRobot(toBuild, bestDir, influence);
			unitIDs.add(rc.senseRobotAtLocation(rc.adjacentLocation(bestDir)).getID());// Store unit ID
			return rc.adjacentLocation(bestDir);
		}
		return null;
	}

	/**
	 * Checks if you can construct a Robot
	 * 
	 * @requires typeToBuild is a valid RobotType, rc.getInfluence >= influence, and
	 *           rc is an EC
	 * @returns if EC can build a Robot
	 * @ensures EC can or cannot build Robot
	 * @throws GameActionException
	 */
	static boolean canConstruct(RobotType typeToBuild, int influence) throws GameActionException {
		RobotType toBuild = typeToBuild;
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				return true;
			}
		}
		return false;
	}

// Flag Encoding/Decoding Methods Below

	/**
	 * Encodes a message given the message type, x-cord, y-cord, and other
	 * information
	 * 
	 * @param type, 0-15, is the number signifying the type of action
	 * @param locationX, 0-127, x-coordinate % 128
	 * @param locationY, 0-127, ycoordinate % 128
	 * @param extrema, 0-63, can encode any other information 0-63
	 * @return returns a encoded flag
	 * @ensures flag is properly coded
	 */
	static int encodeFlag(int type, int locationX, int locationY, int extrema) {
		int[] shift = { schema[1] + schema[2] + schema[3], schema[2] + schema[3], schema[3] };// will store how much to
																								// shift first n-1
																								// params
		int flag = ((type << shift[0]) + (locationX << shift[1]) + (locationY << shift[2]) + extrema);
		if (rc.canSetFlag(flag))
			return flag;
		return 0;
	}

	/**
	 * Encodes a message given the message type, MapLocation, and other information
	 * 
	 * @param type, 0-15, is the number signifying the type of action
	 * @param loc, MapLocation trying to be communicated
	 * @param extrema, 0-63, can encode any other information 0-63
	 * @return returns a encoded flag
	 * @ensures flag is properly coded
	 */
	static int encodeFlag(int type, MapLocation loc, int extrema) {
		return encodeFlag(type, loc.x % 128, loc.y % 128, extrema);
	}

	/**
	 * Encodes a message given the message type, MapLocation, and other information
	 * 
	 * @param type, 0-15, is the number signifying the type of action
	 * @param locationX, 0-127, x-coordinate % 128
	 * @param locationY, 0-127, ycoordinate % 128
	 * @return returns a encoded flag
	 * @ensures flag is properly coded
	 */
	static int encodeFlag(int type, int locationX, int locationY) throws GameActionException {
		int extrema = 0;
		if (rc.canGetFlag(rc.getID()))
			extrema = decodeFlag(rc.getFlag(rc.getID()))[3];
		return encodeFlag(type, locationX, locationY, extrema);
	}

	/**
	 * Encodes a message given the message type, MapLocation
	 *
	 * @param type, 0-15, is the number signifying the type of action
	 * @param loc, MapLocation trying to be communicated
	 * @return returns a encoded flag
	 * @ensures flag is properly coded DOESN'T CHANGE THE extrma flag (good for
	 *          corner runner)
	 */
	static int encodeFlag(int type, MapLocation loc) throws GameActionException {
		int extrema = 0;
		if (rc.canGetFlag(rc.getID()))
			extrema = decodeFlag(rc.getFlag(rc.getID()))[3];
		return encodeFlag(type, loc.x % 128, loc.y % 128, decodeFlag((rc.getFlag(rc.getID())))[3]);

	}

	/**
	 * Decodes flag into an array
	 * 
	 * @requires flag is a valid int
	 * @param flag, 24-bit int
	 * @return Array of size 4
	 * @ensures info is a properly decoded flag
	 */
	static int[] decodeFlag(int flag) {
		int[] info = { flag >> shift[0], (flag >> shift[1]) % (1 << schema[1]), (flag >> shift[2]) % (1 << schema[2]),
				flag % (1 << schema[3]) };
		return info;
	}

	/**
	 * Converts x and y to a MapLocation
	 * 
	 * @requires x and y are valid coordinates % 128
	 * @param x, x difference
	 * @param y, y difference
	 * @return MapLocation
	 * @ensures it is the correct MapLocation
	 */
	static MapLocation getMapLocation(int x, int y) {
		MapLocation loc = rc.getLocation();
		int xDif = x - (loc.x % 128);
		int yDif = y - (loc.y % 128);
		if (xDif < 64 && xDif > -64)
			loc = loc.translate(xDif, 0);
		else if (xDif <= -64)
			loc = loc.translate((128 + xDif) % 64, 0);
		else// larger than 64
			loc = loc.translate((-128 + xDif) % 64, 0);

		if (yDif < 64 && yDif > -64)
			loc = loc.translate(0, yDif);
		else if (yDif <= -64)
			loc = loc.translate(0, (128 + yDif) % 64); // eg, -96 means up 32
		else
			loc = loc.translate(0, (-128 + yDif) % 64);
		// System.out.println("WE ARE AT: " + rc.getLocation().x + "," +
		// rc.getLocation().y + " it's at: " + x + "," + y + " xDif is " + xDif + " yDif
		// is " + yDif + " and the location is " + loc.x + ", " + loc.y);
		return loc;
	}

	static int encodeNeutralECInfluence(int x) {
		int commInf = 39 + x / 50;
		if (x % 50 != 0 || x < 50)
			commInf++;
		return commInf;
	}

// Slanderer Lattice Structure

	/**
	 * Sets a Lattice Destination for the Slanderers
	 * 
	 * @throws GameActionException
	 */
	static void setDestination() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		Team myTeam = rc.getTeam();
		RobotInfo[] nearby = rc.senseNearbyRobots(2, myTeam);
		for (RobotInfo robot : nearby) {
			if (robot.getID() == enlightenmentCenterID) {
				if (rc.canSenseLocation(ecLoc.translate(2, 0))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(2, 0)) == null) {
						latticeDestination = ecLoc.translate(2, 0);
						hasDestination = true;
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(2, 2))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(2, 2)) == null) {
						latticeDestination = ecLoc.translate(2, 2);
						hasDestination = true;
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(2, -2))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(2, -2)) == null) {
						latticeDestination = ecLoc.translate(2, -2);
						hasDestination = true;
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(0, 2))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(0, 2)) == null) {
						latticeDestination = ecLoc.translate(0, 2);
						hasDestination = true;
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(0, -2))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(0, -2)) == null) {
						latticeDestination = ecLoc.translate(0, -2);
						hasDestination = true;
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(-2, 0))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(2, 0)) == null) {
						latticeDestination = ecLoc.translate(2, 0);
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(-2, 2))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(2, 2)) == null) {
						latticeDestination = ecLoc.translate(2, 2);
						hasDestination = true;
						return;
					}
				}

				if (rc.canSenseLocation(ecLoc.translate(-2, -2))) {
					if (rc.senseRobotAtLocation(ecLoc.translate(2, -2)) == null) {
						latticeDestination = ecLoc.translate(2, -2);
						hasDestination = true;
						return;
					}
				}
			}
		}

		RobotInfo[] nearby2 = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam);

		for (RobotInfo robot : nearby2) {
			if (robot.getType().equals(RobotType.POLITICIAN) && decodeFlag(rc.getFlag(rc.getID()))[3] == 1) {
				MapLocation centerPoint = robot.getLocation();
				if (rc.canSenseLocation(centerPoint.translate(1, 1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(1, 1)) == null) {
						if (centerPoint.translate(1, 1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(1, 1);
							hasDestination = true;
							return;
						}
					}
				}

				if (rc.canSenseLocation(centerPoint.translate(1, -1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(1, -1)) == null) {
						if (centerPoint.translate(1, -1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(1, -1);
							hasDestination = true;
							return;
						}
					}
				}

				if (rc.canSenseLocation(centerPoint.translate(-1, 1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(-1, 1)) == null) {
						if (centerPoint.translate(-1, 1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(-1, 1);
							hasDestination = true;
							return;
						}
					}
				}

				if (rc.canSenseLocation(centerPoint.translate(-1, -1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(-1, -1)) == null) {
						if (centerPoint.translate(-1, -1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(-1, -1);
							hasDestination = true;
							return;
						}
					}
				}
			}
		}

	}

	/**
	 * Updates the Lattice for the Slanderers
	 * 
	 * @throws GameActionException
	 */
	static void updateDestination() throws GameActionException {
		Team myTeam = rc.getTeam();
		RobotInfo[] nearby2 = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam);
		// System.out.println("Nearby Friendly Robots::" + nearby2.length);
		for (RobotInfo robot : nearby2) {
			System.out.println("RobotType ::" + robot.getType());
			System.out.println("RobotFlag ::" + rc.getFlag(robot.ID));
			if (robot.getType().equals(RobotType.POLITICIAN) && decodeFlag(rc.getFlag(rc.getID()))[3] == 1) {
				MapLocation centerPoint = robot.getLocation();
				// System.out.println("Slanderer in Lattice Structure at : (" + centerPoint.x +
				// "," + centerPoint.y + ")");
				if (rc.canSenseLocation(centerPoint.translate(1, 1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(1, 1)) == null) {
						if (centerPoint.translate(1, 1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(1, 1);
							hasDestination = true;
							return;
						}
					}
				}

				if (rc.canSenseLocation(centerPoint.translate(1, -1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(1, -1)) == null) {
						if (centerPoint.translate(1, -1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(1, -1);
							hasDestination = true;
							return;
						}
					}
				}

				if (rc.canSenseLocation(centerPoint.translate(-1, 1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(-1, 1)) == null) {
						if (centerPoint.translate(-1, 1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(-1, 1);
							hasDestination = true;
							return;
						}
					}
				}

				if (rc.canSenseLocation(centerPoint.translate(-1, -1))) {
					if (rc.senseRobotAtLocation(centerPoint.translate(-1, -1)) == null) {
						if (centerPoint.translate(-1, -1).distanceSquaredTo(ecLoc) > 2) {
							latticeDestination = centerPoint.translate(-1, -1);
							hasDestination = true;
							return;
						}
					}
				}
			}
		}
	}

// Politcian Protection Methods

	/**
	 * Moves Politicians to Surround Slanderers
	 * 
	 * @throws GameActionException
	 */
	static void surroundSlanderers() throws GameActionException {
		Team myTeam = rc.getTeam();
		Team enemy = rc.getTeam().opponent();
		MapLocation currentLoc = rc.getLocation();
		RobotInfo[] nearby = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam);
		int closestSlanderer = 10000;
		MapLocation slanderer = null;
		Direction directionToSlanderer = null;
		for (RobotInfo robot : nearby) {
			if (robot.getType().equals(RobotType.POLITICIAN) && decodeFlag(rc.getFlag(robot.getID()))[3] == 1) {
				if (robot.getLocation().distanceSquaredTo(currentLoc) < closestSlanderer) {
					closestSlanderer = robot.getLocation().distanceSquaredTo(currentLoc);
					slanderer = robot.getLocation();
					directionToSlanderer = currentLoc.directionTo(slanderer);
				}
			}
		}
		
		if (turnCount >= 40)
			politicianMission = 15;
		
	
		if (rc.isReady()) {
			RobotInfo[] attackable = rc.senseNearbyRobots(1, enemy);
			for(RobotInfo robot : attackable) {
				if(robot.getType().equals(RobotType.MUCKRAKER))
					rc.empower(1);
			}
			attackable = rc.senseNearbyRobots(2, enemy);
			for(RobotInfo robot : attackable) {
				if(robot.getType().equals(RobotType.MUCKRAKER))
					rc.empower(2);
			}
			attackable = rc.senseNearbyRobots(3, enemy);
			for(RobotInfo robot : attackable) {
				if(robot.getType().equals(RobotType.MUCKRAKER))
					rc.empower(3);
			}
			switch (politicianMission) {
			case 10:
				if (closestSlanderer < 9) {
					System.out.println("Going North");
					if (rc.canMove(Direction.NORTH)) {
						rc.move(Direction.NORTH);
					} else if (rc.canMove(Direction.NORTHWEST)) {
						rc.move(Direction.NORTHWEST);
					} else if (rc.canMove(Direction.NORTHEAST)) {
						rc.move(Direction.NORTHEAST);
					}
				} else if (closestSlanderer > 18 && closestSlanderer != 10000) {
					if (rc.canMove(directionToSlanderer)) {
						rc.move(directionToSlanderer);
					} else if (rc.canMove(directionToSlanderer.rotateLeft())) {
						rc.move(directionToSlanderer.rotateLeft());
					} else if (rc.canMove(directionToSlanderer.rotateRight())) {
						rc.move(directionToSlanderer.rotateRight());
					}
				} else {
					if (directionToSlanderer != null) {
						rotateAround(directionToSlanderer);
					}
				}
				break;
			case 11:
				if (closestSlanderer < 9) {
					System.out.println("Going East");
					if (rc.canMove(Direction.EAST)) {
						rc.move(Direction.EAST);
					} else if (rc.canMove(Direction.NORTHEAST)) {
						rc.move(Direction.NORTHEAST);
					} else if (rc.canMove(Direction.SOUTHEAST)) {
						rc.move(Direction.SOUTHEAST);
					}
				} else if (closestSlanderer > 18 && closestSlanderer != 10000) {
					if (rc.canMove(directionToSlanderer)) {
						rc.move(directionToSlanderer);
					} else if (rc.canMove(directionToSlanderer.rotateLeft())) {
						rc.move(directionToSlanderer.rotateLeft());
					} else if (rc.canMove(directionToSlanderer.rotateRight())) {
						rc.move(directionToSlanderer.rotateRight());
					}
				} else {
					if (directionToSlanderer != null) {
						rotateAround(directionToSlanderer);
					}
				}
				break;
			case 12:
				if (closestSlanderer < 9) {
					System.out.println("Going South");
					if (rc.canMove(Direction.SOUTH)) {
						rc.move(Direction.SOUTH);
					} else if (rc.canMove(Direction.SOUTHEAST)) {
						rc.move(Direction.SOUTHEAST);
					} else if (rc.canMove(Direction.SOUTHWEST)) {
						rc.move(Direction.SOUTHWEST);
					}
				} else if (closestSlanderer > 18 && closestSlanderer != 10000) {
					if (rc.canMove(directionToSlanderer)) {
						rc.move(directionToSlanderer);
					} else if (rc.canMove(directionToSlanderer.rotateLeft())) {
						rc.move(directionToSlanderer.rotateLeft());
					} else if (rc.canMove(directionToSlanderer.rotateRight())) {
						rc.move(directionToSlanderer.rotateRight());
					}
				} else {
					if (directionToSlanderer != null) {
						rotateAround(directionToSlanderer);
					}
				}
				break;
			case 13:
				if (closestSlanderer < 9) {
					System.out.println("Going West");
					if (rc.canMove(Direction.WEST)) {
						rc.move(Direction.WEST);
					} else if (rc.canMove(Direction.NORTHWEST)) {
						rc.move(Direction.NORTHWEST);
					} else if (rc.canMove(Direction.SOUTHWEST)) {
						rc.move(Direction.SOUTHWEST);
					}
				} else if (closestSlanderer > 18 && closestSlanderer != 10000) {
					if (rc.canMove(directionToSlanderer)) {
						rc.move(directionToSlanderer);
					} else if (rc.canMove(directionToSlanderer.rotateLeft())) {
						rc.move(directionToSlanderer.rotateLeft());
					} else if (rc.canMove(directionToSlanderer.rotateRight())) {
						rc.move(directionToSlanderer.rotateRight());
					}
				} else {
					if (directionToSlanderer != null) {
						rotateAround(directionToSlanderer);
					}
				}
				break;

			}
		}
		
		doRandomMove();

	}

	static void rotateAround(Direction slandererDirection) throws GameActionException {
		System.out.println("Attempting to go Clockwise");
		switch (slandererDirection) {
		case NORTH:
			politicianMission = 12;
			if (rc.canMove(Direction.WEST)) {
				rc.move(Direction.WEST);
			} else if (rc.canMove(Direction.NORTHWEST)) {
				rc.move(Direction.NORTHWEST);
			} else if (rc.canMove(Direction.SOUTHEAST)) {
				rc.move(Direction.SOUTHEAST);
			}
			break;
		case NORTHEAST:
			politicianMission = 13;
			if (rc.canMove(Direction.NORTH)) {
				rc.move(Direction.NORTH);
			} else if (rc.canMove(Direction.NORTHWEST)) {
				rc.move(Direction.NORTHWEST);
			} else if (rc.canMove(Direction.NORTHEAST)) {
				rc.move(Direction.NORTHEAST);
			}
			break;
		case EAST:
			politicianMission = 13;
			if (rc.canMove(Direction.NORTH)) {
				rc.move(Direction.NORTH);
			} else if (rc.canMove(Direction.NORTHWEST)) {
				rc.move(Direction.NORTHWEST);
			} else if (rc.canMove(Direction.NORTHEAST)) {
				rc.move(Direction.NORTHEAST);
			}
			break;
		case SOUTHEAST:
			politicianMission = 10;
			if (rc.canMove(Direction.EAST)) {
				rc.move(Direction.EAST);
			} else if (rc.canMove(Direction.NORTHEAST)) {
				rc.move(Direction.NORTHEAST);
			} else if (rc.canMove(Direction.NORTHWEST)) {
				rc.move(Direction.NORTHWEST);
			}
			break;
		case SOUTH:
			politicianMission = 10;
			if (rc.canMove(Direction.EAST)) {
				rc.move(Direction.EAST);
			} else if (rc.canMove(Direction.NORTHEAST)) {
				rc.move(Direction.NORTHEAST);
			} else if (rc.canMove(Direction.NORTHWEST)) {
				rc.move(Direction.NORTHWEST);
			}
			break;
		case SOUTHWEST:
			politicianMission = 11;
			if (rc.canMove(Direction.SOUTH)) {
				rc.move(Direction.SOUTH);
			} else if (rc.canMove(Direction.SOUTHEAST)) {
				rc.move(Direction.SOUTHEAST);
			} else if (rc.canMove(Direction.SOUTHWEST)) {
				rc.move(Direction.SOUTHWEST);
			}
			break;
		case WEST:
			politicianMission = 11;
			if (rc.canMove(Direction.SOUTH)) {
				rc.move(Direction.SOUTH);
			} else if (rc.canMove(Direction.SOUTHEAST)) {
				rc.move(Direction.SOUTHEAST);
			} else if (rc.canMove(Direction.SOUTHWEST)) {
				rc.move(Direction.SOUTHWEST);
			}
			break;
		case NORTHWEST:
			politicianMission = 12;
			if (rc.canMove(Direction.WEST)) {
				rc.move(Direction.WEST);
			} else if (rc.canMove(Direction.NORTHWEST)) {
				rc.move(Direction.NORTHWEST);
			} else if (rc.canMove(Direction.SOUTHEAST)) {
				rc.move(Direction.SOUTHEAST);
			}
			break;
		}
	}

	/**
	 * Checks radius 2 for Enlightenment Center. Finds and set EC ID.
	 * 
	 * @throws GameActionException
	 *
	 * @requires turnCount == 1 and unit is created by EC
	 * @ensures enlightementCenterID is the correct ID
	 */
	static void firstTurn() throws GameActionException {
		RobotInfo[] nearby = rc.senseNearbyRobots(2, rc.getTeam());
		for (RobotInfo r : nearby) {
			if (r.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				enlightenmentCenterID = r.getID();
				ecLoc = rc.senseRobot(enlightenmentCenterID).getLocation();
				friendlyECs.add(ecLoc);
				return;
			}

		}
	}

	/**
	 * Does a random move. Attempts all directions
	 *
	 */
	static void doRandomMove() throws GameActionException {
		int start = (int) (Math.random() * directions.length);
		Direction dir;
		for (int i = 0; i < directions.length; i++) {
			dir = directions[(i + start) % directions.length];
			if (rc.canMove(dir))
				rc.move(dir);
		}

	}

	/**
	 * Optimized version of rc.move(rc.currentLocation().directionTo(loc))
	 *
	 * To be used by politicians storming neutral ECs and perhaps by muckrakers
	 * trying to go to corners
	 *
	 * If moving to a diagonal location, move whichever spot IN DIRECTION OF TARGET
	 * has highest passability If moving to straight target in straight line, try to
	 * move in that direction
	 *
	 * returns true if made a move, false if didn't
	 *
	 */
	static boolean navigateTo(MapLocation loc) throws GameActionException {
		if (!rc.isReady())
			return false;
		MapLocation currentLoc = rc.getLocation();
		int xDif = loc.x - currentLoc.x, yDif = loc.y - currentLoc.y;

		if (xDif == 0 && yDif == 0)
			return false;
		// Scale down to -1,0,1 in each direction
		if (xDif > 0)
			xDif = 1;
		else if (xDif < 0)
			xDif = -1;
		if (yDif > 0)
			yDif = 1;
		else if (yDif < 0)
			yDif = -1;

		double xPass = 0, yPass = 0, diagonalPass = 0;
		if (rc.canSenseLocation(currentLoc.translate(xDif, 0)) && !rc.isLocationOccupied(currentLoc.translate(xDif, 0)))
			xPass = rc.sensePassability(currentLoc.translate(xDif, 0));
		if (rc.canSenseLocation(currentLoc.translate(0, yDif)) && !rc.isLocationOccupied(currentLoc.translate(0, yDif)))
			yPass = rc.sensePassability(currentLoc.translate(0, yDif));
		MapLocation xyTranslated = currentLoc.translate(xDif, yDif);

		// If we want to move diagonally, see which way that's is best
		if (xDif != 0 && yDif != 0) {
			if (rc.canSenseLocation(xyTranslated) && !rc.isLocationOccupied(xyTranslated))
				diagonalPass = rc.sensePassability(xyTranslated);
			if (diagonalPass >= xPass && diagonalPass >= yPass && rc.canMove(currentLoc.directionTo(xyTranslated))) {
				rc.move(currentLoc.directionTo(xyTranslated));
				return true;
			}
			if (xPass >= yPass && rc.canMove(currentLoc.directionTo(currentLoc.translate(xDif, 0)))) {
				rc.move(currentLoc.directionTo(currentLoc.translate(xDif, 0)));
				return true;
			}
			if (rc.canMove(currentLoc.directionTo(currentLoc.translate(0, yDif)))) {
				rc.move(currentLoc.directionTo(currentLoc.translate(0, yDif)));
				return true;
			}
			return false;
		}

		// This means the target is lateral. Attempt to move that way
		if (rc.canMove(currentLoc.directionTo(xyTranslated))) {
			rc.move(currentLoc.directionTo(xyTranslated));
			return true;
		}
		return false;
	}

	/**
	 * Uses muckrakerMission, number 0 - 7
	 *
	 * @throws GameActionException
	 */
	static void searchOtherECs() throws GameActionException {
		System.out.println("I HAVE MISSION : " + muckrakerMission);
		if (!(muckrakerMission >= 2 && muckrakerMission <= 9))
			return;
		Direction[][] movements = { { Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST },
				{ Direction.NORTH, Direction.NORTHEAST, Direction.EAST },
				{ Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST },
				{ Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH },
				{ Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST },
				{ Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST },
				{ Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST },
				{ Direction.WEST, Direction.NORTHWEST, Direction.NORTH } };
		ArrayList<Direction> canMove = new ArrayList<Direction>();
		for (Direction a : movements[muckrakerMission - 2]) {
			if (rc.canMove(a))
				canMove.add(a);

		}
		if (canMove.size() > 0) {
			int random = (int) (Math.random() * canMove.size());
			System.out.println("CHOOSING FROM: " + canMove.size() + " moves");
			rc.move(canMove.get(random));
			turnsWaiting = 0;
		} else {
			turnsWaiting++;
			if (turnsWaiting > 3) {
				muckrakerMission = 0;// Reset ourselves
				doRandomMove();
			}
			Clock.yield();
		}
	}

// ExamplePlayerFuncs Methods	

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
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
			return false;
	}

}
