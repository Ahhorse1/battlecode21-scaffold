package WinstonPlayer;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Arrays;

public strictfp class RobotPlayer {
	static RobotController rc;

	static boolean isAtCorner = false;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	static int turnCount;

	/**
	 * stores schema of the communications and how much to shift each n-1 parameter
	 */
	static int[] schema = { 4, 7, 7, 6 };
	static int[] shift = { schema[1] + schema[2] + schema[3], schema[2] + schema[3], schema[3] };

	/**
	 *
	 */
	static ArrayList<MapLocation> enemyECs = new ArrayList<MapLocation>();
	static ArrayList<MapLocation> friendlyECs = new ArrayList<MapLocation>();
	static ArrayList<MapLocation> neutralECs = new ArrayList<MapLocation>();

	static ArrayList<Integer> unitIDs = new ArrayList<Integer>();

	/**
	 * Arraylist of arrays Each array is +1 size larger than it's supposed to be,
	 * with the unit code in the 0 position The unit code being influence *10 +
	 * (1/2/3) for (politicians/slanderers/muckrakers)
	 */
	static ArrayList<int[]> Units = new ArrayList<int[]>();

	/**
	 * Variables that store Location of Enlightenment Center that created the robot
	 */
	static MapLocation ecLoc;

	/**
	 * Used for Corner Runners, mapCorners holds MapLocations of the corners
	 * cornerRunnerIDs holds the Muckrakers' IDs makeCornerRunner is used by the EC
	 * so they know when to replace atCorner is updated when a Muckraker reaches a
	 * corner runCorner is used so
	 */
	static MapLocation[] mapCorners = new MapLocation[4];
	static int[] cornerRunnerIDs = new int[4];
	static boolean[] makeCornerRunner = { true, true, true, true };
	static boolean[] atCorner = { false, false, false, false };
	static boolean runCorner = true;
	static int closestCorner;

	/**
	 * Stores the previous round's influence
	 */
	static int previousInfluence = 0;

	/**
	 * Indicates the mode for building armies Each mode lasts for 15 actions 1 =
	 * muckrakers, 2 = politicians, 3 = bidding
	 */
	static int[] modeCount = new int[2];

	/**
	 * protecting: If a unit is protecting a slanderer/EC hasDesintation: If a
	 * politician has a destination destination: MapLocation containing the
	 * destination
	 */
	static boolean protecting = false;
	static boolean hasDestination;
	static MapLocation destination;

	/**
	 * ID of Enlightenment Center
	 */
	static int enlightenmentCenterID;

	static boolean readyToDie = false;
	static int cornerNum = 0;

	/**
	 * Holds units for the first stage
	 */
	static int[][] stageOne = { { 0, 0 }, { 1302, 1072 } };
	static int[][] stageTwo = { { 0, 0, 0, 0, 0 }, { 201, 131, 131, 1302, 1072 } };
	/**
	 * Holds Booleans for flipping between two different modes
	 */
	static Boolean stageTwoMode = false;
	static Boolean stageFourMode = false;
	static Boolean stageFiveMode = false;
	static Boolean stageSixMode = false;

	// int because it needs to transfer between 3 modes
	static int[] stageSevenModes = { 0, 0 };
	/**
	 * Holds all integer breakpoints for the slanderers
	 */
	static int[] breakpoints = { 21, 41, 63, 85, 107, 130, 154, 178, 203, 228, 255 };

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
					if (turnCount == 1 && rc.getInfluence() != 150) {
						runCapturedEnlightenmentCenter();
					} else {
						runEnlightenmentCenter();
					}
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

	static void runEnlightenmentCenter() throws GameActionException {

		// Scan through flags and see what's up
		if (unitIDs.size() > 0) {
			int flagSetPriority = 0;// Set this to the priority of the flag you set
			// Neutral is priority 4, enemy is priority 3, slanderer storm is priority 2,
			int id;
			int[] flag;
			MapLocation loc;
			for (int i = 0; i < unitIDs.size(); ++i) {
				id = unitIDs.get(i);
				if (rc.canGetFlag(id)) {
					flag = decodeFlag(rc.getFlag(id));
					loc = getMapLocation(flag[1], flag[2]);
					System.out.println("GOT COMMUNICATION FROM: (" + loc.x + "," + loc.y + ").");
					switch (flag[0]) {
						case 1:// enemy EC
							if (!enemyECs.contains(loc))
								enemyECs.add(loc);
							if (friendlyECs.contains(loc))
								friendlyECs.remove(loc);
							if (neutralECs.contains(loc))
								neutralECs.remove(loc);
							break;
						case 2: // neutral HQ
							if (!neutralECs.contains(loc))
								neutralECs.add(loc);
							break;
						case 3: // Friendly EC
							if (!friendlyECs.contains(loc)) {
								friendlyECs.add(loc);
								rc.setFlag(encodeFlag(3, loc));
							}
							if (enemyECs.contains(loc))
								enemyECs.remove(loc);
							if (neutralECs.contains(loc)) {
								neutralECs.remove(loc);
							}
							break;
						case 12: // Slanderer storm
							int myFlag = encodeFlag(12, loc);
							if (rc.canSetFlag(myFlag))
								rc.setFlag(myFlag);

					}
				} else {
					unitIDs.remove(i);// This means the robot went bye-bye
				}
			}
		}
		// Actually set the flag
		if (neutralECs.size() > 0) {
			rc.setFlag(encodeFlag(2, neutralECs.get(0)));
		}

		if(runCorner)
			updateCorner();
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
	}

	static void runCapturedEnlightenmentCenter() throws GameActionException {
		if (turnCount == 1) {
			int[] slanderers = { 2302 };
			Units.add(slanderers);
		}
		Units.set(0, resizeArray(Units.get(0), Units.get(0).length + 1));
		runStageTwo();
	}

	static void runPolitician() throws GameActionException {
		if (turnCount == 1)
			firstTurn();

		if(decodeFlag(rc.getFlag(rc.getID()))[3]==10&&turnCount>=40)//Don't worry about lattice after a couple turns
			rc.setFlag(encodeFlag(0,0,0,12));

		Team enemy = rc.getTeam().opponent();
		MapLocation currentLoc = rc.getLocation();
		int sensorRadius = rc.getType().sensorRadiusSquared;
		int[] ecFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));

		// Update info from EC
		MapLocation loc = getMapLocation(ecFlag[1], ecFlag[2]);
		switch (ecFlag[0]) {
		case 1:// enemy EC
			if (!enemyECs.contains(loc))
				enemyECs.add(loc);
			if (friendlyECs.contains(loc))
				friendlyECs.remove(loc);
			if (neutralECs.contains(loc))
				neutralECs.remove(loc);
			break;
		case 2: // neutral HQ
			if (!neutralECs.contains(loc))
				neutralECs.add(loc);
			break;
		case 3: // Friendly EC
			if (!friendlyECs.contains(loc))
				friendlyECs.add(loc);
			if (enemyECs.contains(loc))
				enemyECs.remove(loc);
			if (neutralECs.contains(loc))
				neutralECs.remove(loc);
			break;
		}

		// Scan nearby robots, update stuff
		RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius);
		for (RobotInfo robot : nearby) {
			loc = robot.getLocation();
			if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {// Found an EC
				if (robot.getTeam().equals(Team.NEUTRAL) && !neutralECs.contains(loc)) {
					neutralECs.add(loc);
					rc.setFlag(encodeFlag(2, loc));
				} else if (robot.getTeam().equals(rc.getTeam()) && !friendlyECs.contains(loc)) {
					friendlyECs.add(loc);
					rc.setFlag(encodeFlag(3, loc));
					if (neutralECs.contains(loc))
						neutralECs.remove(loc);
				} else if (robot.getTeam().equals(enemy) && !enemyECs.contains(loc)) {//Found an enemy EC
					enemyECs.add(loc);
					rc.setFlag(encodeFlag(1,loc));
					if (neutralECs.contains(loc))
						neutralECs.remove(loc);
					if (friendlyECs.contains(loc))
						friendlyECs.remove(loc);
				}
			}
		}

		if (rc.getFlag(rc.getID()) == 0) {
			if (rc.getInfluence() == 20)
				rc.setFlag(encodeFlag(0, 0, 0, 10));
		}

		if (rc.isReady()) {
			if (decodeFlag(rc.getFlag(rc.getID()))[3] == 10) {
				latticeStructure();
				return;
			} else if (decodeFlag(rc.getFlag(rc.getID()))[3] == 11) {
				RobotInfo[] attackableRadiusOne = rc.senseNearbyRobots(3, enemy);
				if (attackableRadiusOne.length != 0)
					rc.empower(3);
				return;
			}
		}

		//Attempt to storm neutral ECs
		if (neutralECs.size() > 0) {
			loc = neutralECs.get(0);
			if (rc.isReady()) {
				if (currentLoc.isWithinDistanceSquared(loc, 2)) {
					rc.empower(2);
					return;
				} else if (rc.canMove(currentLoc.directionTo(loc))) {
					rc.move(currentLoc.directionTo(loc));
					return;
				} else if(currentLoc.isWithinDistanceSquared(loc,rc.getType().actionRadiusSquared)) {//We may be blocked from EC
					rc.empower(currentLoc.distanceSquaredTo(loc));
					return;
				}
			}
		}

		//Attempt to attack a nearby unit
		RobotInfo[] attackable = rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent());
		if (attackable.length > 0) {
			for (int i = 0; i < attackable.length; i++) {
				if(attackable[i].getLocation().isWithinDistanceSquared(currentLoc,2)){
					rc.empower(2);
				}
				if (attackable[i].getType() == RobotType.ENLIGHTENMENT_CENTER) {
					if (rc.isReady()) {
						if (rc.canMove(currentLoc.directionTo(attackable[i].location)))
							rc.move(currentLoc.directionTo(attackable[i].location));
						else if(currentLoc.isWithinDistanceSquared(loc,rc.getType().actionRadiusSquared))//We may be blocked from EC
							rc.empower(currentLoc.distanceSquaredTo(loc));
					}
				}
			}
		} else if (ecFlag[0] == 1 || ecFlag[0] == 2) {
			MapLocation targetEC = getMapLocation(ecFlag[1], ecFlag[2]);
			if (rc.isReady()) {
				if (rc.canMove(currentLoc.directionTo(targetEC))) {
					rc.move(currentLoc.directionTo(targetEC));
				} else if (rc.canMove(currentLoc.directionTo(targetEC).rotateLeft())) {
					rc.move(currentLoc.directionTo(targetEC).rotateLeft());
				} else if (rc.canMove(currentLoc.directionTo(targetEC).rotateRight())) {
					rc.move(currentLoc.directionTo(targetEC).rotateRight());
				} else if (rc.canMove(currentLoc.directionTo(targetEC).rotateLeft().rotateLeft())) {
					rc.move(currentLoc.directionTo(targetEC).rotateLeft().rotateLeft());
				} else if (rc.canMove(currentLoc.directionTo(targetEC).rotateRight().rotateRight())) {
					rc.move(currentLoc.directionTo(targetEC).rotateRight().rotateRight());
				}
			}
		}

		//If all else fails, do something
		doRandomMove();


	}

	static void runSlanderer() throws GameActionException {
		int ECFlag;
		if (turnCount == 1) {
			firstTurn();
			ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID))[3];
			if (ECFlag >= 30 && ECFlag <= 33)
				cornerNum = ECFlag;
		}
		if (turnCount == 100)
			rc.setFlag(10);
		if (turnCount == 299)
			rc.setFlag(0);
		if (!rc.isReady())
			return;
		if (cornerNum == 0) {
			ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID))[3];
			if (ECFlag >= 30 && ECFlag <= 33)
				cornerNum = ECFlag;
		}
		if (cornerNum > 0) {
			switch (cornerNum) {
			case 30:
				if (rc.canMove(Direction.NORTHWEST)) {
					rc.move(Direction.NORTHWEST);
					return;
				} else if (rc.canMove(Direction.NORTH)) {
					rc.move(Direction.NORTH);
					return;
				} else if (rc.canMove(Direction.WEST)) {
					rc.move(Direction.WEST);
					return;
				}
				break;
			case 31:
				if (rc.canMove(Direction.NORTHEAST)) {
					rc.move(Direction.NORTHEAST);
					return;
				} else if (rc.canMove(Direction.NORTH)) {
					rc.move(Direction.NORTH);
					return;
				} else if (rc.canMove(Direction.EAST)) {
					rc.move(Direction.EAST);
					return;
				}

				break;
			case 32:
				if (rc.canMove(Direction.SOUTHEAST)) {
					rc.move(Direction.SOUTHEAST);
					return;
				} else if (rc.canMove(Direction.SOUTH)) {
					rc.move(Direction.SOUTH);
					return;
				} else if (rc.canMove(Direction.EAST)) {
					rc.move(Direction.EAST);
					return;
				}
				break;
			case 33:
				if (rc.canMove(Direction.SOUTHWEST)) {
					rc.move(Direction.SOUTHWEST);
					return;
				} else if (rc.canMove(Direction.SOUTH)) {
					rc.move(Direction.SOUTH);
					return;
				} else if (rc.canMove(Direction.WEST)) {
					rc.move(Direction.WEST);
					return;
				}
				break;
			}
		} else {
			if (rc.getLocation().isWithinDistanceSquared(ecLoc, 25))
				tryMove(randomDirection());
		}

	}

	static void runMuckraker() throws GameActionException {
		// Define some constants
		int senseRadius = RobotType.MUCKRAKER.detectionRadiusSquared;
		int actionRadius = RobotType.MUCKRAKER.actionRadiusSquared;

		// First turn stuffs
		if (turnCount == 1) {
			firstTurn();// Sets ECFlag
			int[] ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));
			switch (ECFlag[3]) {
			case 20:
				rc.setFlag(encodeFlag(0, 0, 0, 20));
				break;
			case 21:
				rc.setFlag(encodeFlag(0, 0, 0, 21));
				break;
			case 22:
				rc.setFlag(encodeFlag(0, 0, 0, 22));
				break;
			case 23:
				rc.setFlag(encodeFlag(0, 0, 0, 23));
				break;
			}
		}

		MapLocation targetDestination = rc.getLocation();// Set this to something if we got some place to go (i.e. go
															// help out a bro)
		boolean haveDestination = false;// Set this to true if we got some place to go

		// 1 Collect information from ECflag
		if (rc.canGetFlag(enlightenmentCenterID)) {
			int[] ECFlag = decodeFlag(rc.getFlag(enlightenmentCenterID));
			MapLocation loc = getMapLocation(ECFlag[1], ECFlag[2]);
			switch (ECFlag[0]) {
			case 1:// enemy EC
				if (!enemyECs.contains(loc))
					enemyECs.add(loc);
				if (friendlyECs.contains(loc))
					friendlyECs.remove(loc);
				if (neutralECs.contains(loc))
					neutralECs.remove(loc);
				break;
			case 2: // neutral HQ
				if (!neutralECs.contains(loc))
					neutralECs.add(loc);
				break;
			case 3: // Friendly EC
				if (!friendlyECs.contains(loc))
					friendlyECs.add(loc);
				if (enemyECs.contains(loc))
					enemyECs.remove(loc);
				if (neutralECs.contains(loc))
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
		Team enemy = rc.getTeam().opponent();
		for (RobotInfo robot : rc.senseNearbyRobots(senseRadius)) {
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
					// Found new enemy HQ
					rc.setFlag(encodeFlag(1, robot.getLocation()));
					flagSet = true;
				} else if (robot.getTeam().equals(Team.NEUTRAL)) {
					// We've found a neutral enlightenment center!!!! Set flag at all costs
					rc.setFlag(encodeFlag(2, robot.getLocation()));
					flagSet = true;
				}
			}
		}
		if (enemySlandererCnt >= 12 && flagSet == false) { // notificationCutoff is set at 12, can increase whenever
			// If the number of enemies warrants calling for reinforcements and there's not
			// more important info to send
			rc.setFlag(encodeFlag(12, rc.getLocation()));
		}

		if (isCornerRunner()) {
			int ECFlagMsg = decodeFlag(rc.getFlag(enlightenmentCenterID))[3];
			if (ECFlagMsg >= 30 && ECFlagMsg <= 33) {// Means that EC has already found a flag
				int[] flag = decodeFlag(rc.getFlag(rc.getID()));
				if (flag[3] != ECFlagMsg) {
					rc.setFlag(encodeFlag(flag[0], flag[1], flag[2], 0));
				}
			} else {
				if(!isAtCorner) {
					findCorner();
				}
				return;
			}
		}

		// Now in action phase of muckraker logic
		if (!rc.isReady())
			return;

		// Find a target to expose
		RobotInfo[] nearby = rc.senseNearbyRobots(actionRadius,enemy);
		for (RobotInfo robot : nearby)
			if (robot.getType().canBeExposed() && rc.isReady()) {
				rc.expose(robot.getLocation());
				return;
			}

		// Move somewhere based on destination, then antigrouping, then randomly
		if (haveDestination && rc.canMove(rc.getLocation().directionTo(targetDestination)))
			rc.move(rc.getLocation().directionTo(targetDestination));
		else if (rc.canMove(antiGroupingMovement()))//Run antigrouping stuff
			rc.move(antiGroupingMovement());
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
		int actionRadius = rc.getType().actionRadiusSquared;
		int sensorRadius = rc.getType().sensorRadiusSquared;
		int quadrantOne = 0, quadrantTwo = 0, quadrantThree = 0, quadrantFour = 0;
		Boolean furtherX = false;
		Boolean furtherY = false;
		RobotInfo[] nearby = rc.senseNearbyRobots(sensorRadius, rc.getTeam());
		for (RobotInfo robot : nearby) {
			MapLocation location = robot.getLocation();
			furtherX = (location.x >= selfX);
			furtherY = (location.y >= selfY);
			if (furtherX && furtherY)
				quadrantOne++;
			else if (furtherX && !furtherY)
				quadrantTwo++;
			else if (!furtherX && !furtherY)
				quadrantThree++;
			else
				quadrantFour++;
		}

		double west = rc.sensePassability(rc.adjacentLocation((Direction.WEST)));
		double northwest = rc.sensePassability(rc.adjacentLocation((Direction.NORTHWEST)));
		double north = rc.sensePassability(rc.adjacentLocation((Direction.NORTH)));
		double northeast = rc.sensePassability(rc.adjacentLocation((Direction.NORTHEAST)));
		double east = rc.sensePassability(rc.adjacentLocation((Direction.EAST)));
		double south = rc.sensePassability(rc.adjacentLocation((Direction.SOUTH)));
		double southwest = rc.sensePassability(rc.adjacentLocation((Direction.SOUTHWEST)));
		double southeast = rc.sensePassability(rc.adjacentLocation((Direction.SOUTHEAST)));
		if (quadrantOne < quadrantTwo && quadrantOne < quadrantThree && quadrantOne < quadrantFour) {
			// Go to quadrant I
			if (northeast > north && northeast > east)
				return Direction.NORTHEAST;
			else if (north > east)
				return Direction.NORTH;
			else
				return Direction.EAST;
		}
		if (quadrantTwo < quadrantThree && quadrantTwo < quadrantFour) {
			// Go to quadrant II
			if (southeast > east && southeast > south)
				return Direction.SOUTHEAST;
			else if (south > east)
				return Direction.SOUTH;
			else
				return Direction.EAST;
		}
		if (quadrantThree < quadrantFour) {
			// Go to quadrant III
			if (southwest > south && southwest > west)
				return Direction.SOUTHWEST;
			else if (south > west)
				return Direction.SOUTH;
			else
				return Direction.WEST;
		}

		// Go to quadrant IV
		if (northwest > north && northwest > west)
			return Direction.NORTHWEST;
		else if (west > north)
			return Direction.WEST;
		else
			return Direction.NORTH;

	}
// Enlightenment Center Methods Below

	static void runStageOne() throws GameActionException {
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
			if (makeCornerRunner[0] || makeCornerRunner[1] || makeCornerRunner[2] || makeCornerRunner[3]) {
				runCorner();
			} else if (canConstruct(RobotType.MUCKRAKER, 1)) {
				construct(RobotType.MUCKRAKER, 1);
			}
		}

	}

	/**
	 * Either Builds or Bids depending on buildUnits. Run by EC from 555-1500
	 * 
	 * @throws GameActionException
	 */
	static void runStageTwo() throws GameActionException {
		if (rc.isReady()) {
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
			if (canConstruct(RobotType.MUCKRAKER, 1)) {
				construct(RobotType.MUCKRAKER, 1);
			}
		}
	}

	static void runStageThree() throws GameActionException {
		if(neutralECs.size() > 0) {
			
		}
		/**
		 * if neutral enlightenment center int neutralEnlightenmentCenterValue = ---;
		 * if(rc.getInfluence() >= neutralEnlightenmentCenterValue+10) {
		 * if(canConstruct(RobotType.POLITICIAN, neutralEnlightenmentCenterValue+10) {
		 * construct(RobotType.POLITICIAN, neutralEnlightenmentCenterValue+10); }
		 * if(canConstruct(RobotType.MUCKRAKER,1) { construct(RobotType.MUCKRAKER, 1); }
		 * }
		 */
	}

	static void runStageFour() throws GameActionException {
		if (rc.isReady()) {
			if (stageFourMode) {
				if (canConstruct(RobotType.SLANDERER, 107)) {
					construct(RobotType.SLANDERER, 107);
					stageFourMode = false;
				}
			} else {
				if (canConstruct(RobotType.SLANDERER, 130)) {
					construct(RobotType.SLANDERER, 130);
				}
			}
		}
	}

	static void runStageFive() throws GameActionException {
		/**
		 * if neutral enlightenment center int neutralEnlightenmentCenterValue = ---;
		 * if(rc.getInfluence() >= neutralEnlightenmentCenterValue+10) {
		 * if(canConstruct(RobotType.POLITICIAN, neutralEnlightenmentCenterValue+10) {
		 * construct(RobotType.POLITICIAN, neutralEnlightenmentCenterValue+10); }
		 * if(canConstruct(RobotType.MUCKRAKER,1) { construct(RobotType.MUCKRAKER, 1); }
		 * }
		 */
		if (rc.isReady()) {
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
					stageFiveMode = false;
					return;
				}
			}
			if (canConstruct(RobotType.MUCKRAKER, 1)) {
				construct(RobotType.MUCKRAKER, 1);
			}
		}
	}

	static void runStageSix() throws GameActionException {
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
		if (rc.isReady()) {
			if (stageSevenModes[0] == 0) {
				if (canConstruct(RobotType.POLITICIAN, 100)) {
					construct(RobotType.POLITICIAN, 100);
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
				if (rc.canBid(10)) {
					rc.bid(10);
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
	 * Builds units based on the Units Array
	 * 
	 * @return true/false depending on if a unit was build
	 * @throws GameActionException
	 */
	static int buildUnits() throws GameActionException {
		RobotType toBuild;
		int toBuildInfluence;
		for (int i = Units.size() - 1; i >= 0; i--) {
			int x = indexOfArray(Units.get(i), -1);
			if (x != -1) {
				int y = Units.get(i)[0] % 10;
				if (Units.get(i)[0] == 13) {
					runCorner();
					break;
				} else if (y == 1)
					toBuild = RobotType.POLITICIAN;
				else if (y == 2)
					toBuild = RobotType.SLANDERER;
				else
					toBuild = RobotType.MUCKRAKER;

				toBuildInfluence = Units.get(i)[0] / 10;
				if (canConstruct(toBuild, toBuildInfluence)) {
					MapLocation loc = construct(toBuild, toBuildInfluence);
					Units.get(i)[x] = rc.senseRobotAtLocation(loc).getID();
					return 1;
				} else {
					return 2;
				}
			}
		}
		System.out.println("full");
		return 0;
	}

	/**
	 * Finds which units need to be replaced.
	 * 
	 * @requires rc.getType == RobotType.ENLIGHTENMENT_CENTER
	 * @ensures Units = #Unit - IDs of Robots that have been destroyed replaced with
	 *          -1
	 * @throws GameActionException
	 */
	static void replace() throws GameActionException {
		for (int i = 0; i < Units.size(); i++) {
			int x = Units.get(i)[0];
			if (x % 10 == 2) {
				for (int ii = 1; ii < Units.get(i).length; ii++) {
					if (Units.get(i)[ii] == 0) {
					} else if (!rc.canGetFlag(Units.get(i)[ii])) {
						Units.get(i)[ii] = -1;
					} else if (rc.getFlag(Units.get(i)[ii]) == 10) {
						Units.get(i)[ii] = -1;
					}
				}
			} else {
				for (int iii = 1; iii < Units.get(i).length; iii++) {
					if (Units.get(i)[iii] == 0) {
					} else if (!rc.canGetFlag(Units.get(i)[iii])) {
						Units.get(i)[iii] = -1;
					}
				}
			}
		}
	}

	/**
	 * Resizes an array to the size passed in.
	 * 
	 * @param array, old array
	 * @param size, size of new array
	 * @return resized Array
	 * @ensures resized.length == size and resized contains the values of array
	 * @throws GameActionException
	 */
	static int[] resizeArray(int[] array, int size) throws GameActionException {
		int[] old = array;
		int[] resized = new int[old.length + size];
		for (int i = 0; i < old.length; i++) {
			resized[i] = old[i];
		}
		for (int ii = old.length; ii < resized.length; ii++) {
			resized[ii] = -1;
		}
		return resized;
	}

	/**
	 * Resizes a specific array in Units
	 * 
	 * @param code, identifies the array within Units to be resized
	 * @param size, new size for the array
	 * @ensures The specified array is resized
	 * @throws GameActionException
	 */
	static void resizeFromList(int code, int size) throws GameActionException {
		for (int i = 0; i < Units.size(); i++)
			if (Units.get(i)[0] == code)
				Units.set(i, resizeArray(Units.get(i), size));
	}

	/**
	 * Checks to see if an Array contains a specific int, else returns -1
	 * 
	 * @param array, containing ints
	 * @param needle, int being identified
	 * @return index of needle or -1 if not found
	 * @ensures correct index of int or -1
	 */
	static int indexOfArray(int[] array, int needle) {
		for (int i = 0; i < array.length; i++)
			if (array[i] == needle)
				return i;
		return -1;
	}

	/**
	 * Sets previousInfluence to current influence
	 * 
	 * @requires rc.getType == RobotType.ENLIGHTENMENTCENTER
	 * @ensures previousInfluence = rc.getInfluence()
	 * @throws GameActionException
	 */
	static void setPrevious() throws GameActionException {
		previousInfluence = rc.getInfluence();
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
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				rc.buildRobot(toBuild, dir, influence);
				unitIDs.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());// Store unit ID
				return rc.adjacentLocation(dir);
			}
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

// Lattice Structure Methods Below

	/**
	 * Checks for destination, else calls findDestination Then traverses towards
	 * that destination
	 * 
	 * @throws GameActionException
	 */
	static void latticeStructure() throws GameActionException {
		if (hasDestination == false)
			findDestination();
		traverse(destination);
	}

	/**
	 * Looks for a destination for the Politician when building a lattice structure
	 * 
	 * @throws GameActionException
	 */
	static void findDestination() throws GameActionException {
		RobotInfo[] nearby = rc.senseNearbyRobots(2);
		MapLocation center = rc.getLocation();
		ArrayList<MapLocation> options = new ArrayList<MapLocation>();

		for (int i = 0; i < nearby.length; i++) {
			int id = nearby[i].getID();
			if (nearby[i].getType().equals(RobotType.ENLIGHTENMENT_CENTER)
					|| decodeFlag(rc.getFlag(nearby[i].getID()))[3] == 11) {
				options.add(nearby[i].getLocation());
			}
		}

		int optionsRandom = (int) (Math.random() * options.size());
		center = options.get(optionsRandom);

		MapLocation nw = center.add(Direction.NORTHWEST).add(Direction.NORTHWEST);
		MapLocation n = center.add(Direction.NORTH).add(Direction.NORTH);
		MapLocation ne = center.add(Direction.NORTHEAST).add(Direction.NORTHEAST);
		MapLocation e = center.add(Direction.EAST).add(Direction.EAST);
		MapLocation se = center.add(Direction.SOUTHEAST).add(Direction.SOUTHEAST);
		MapLocation s = center.add(Direction.SOUTH).add(Direction.SOUTH);
		MapLocation sw = center.add(Direction.SOUTHWEST).add(Direction.SOUTHWEST);
		MapLocation w = center.add(Direction.WEST).add(Direction.WEST);

		MapLocation[] mapSquares = { nw, n, ne, e, se, s, sw, w };
		int[] booleanmapSquares = new int[8];

		// 0 is location is occupied, 1 is location is empty, 2 is if location is not on
		// the map

		for (int i = 0; i < 8; i++) {
			if (!rc.onTheMap(mapSquares[i]))
				booleanmapSquares[i] = 2;
			else if (rc.isLocationOccupied(mapSquares[i]))
				booleanmapSquares[i] = 0;
			else
				booleanmapSquares[i] = 1;
		}

		ArrayList<MapLocation> availableLocations = new ArrayList<MapLocation>();
		ArrayList<MapLocation> locationsOnTheMap = new ArrayList<MapLocation>();

		for (int i = 0; i < 8; i++) {
			if (booleanmapSquares[i] == 1)
				availableLocations.add(mapSquares[i]);
			if (booleanmapSquares[i] != 2)
				locationsOnTheMap.add(mapSquares[i]);
		}

		if (availableLocations.size() > 0) {
			int random = (int) (Math.random() * availableLocations.size());
			destination = availableLocations.get(random);
		} else {
			int random = (int) (Math.random() * locationsOnTheMap.size());
			destination = locationsOnTheMap.get(random);
			destination = locationsOnTheMap.get(random);
		}

		hasDestination = true;
	}

	/**
	 * Moves towards the destination
	 * 
	 * @param destination, valid MapLocation
	 * @requires destination is not occupied, there is a route, and Robot isReady
	 * @ensures the Robot moves toward the destination
	 * @throws GameActionException
	 */
	static void traverse(MapLocation destination) throws GameActionException {
		Direction toGo = rc.getLocation().directionTo(destination);
		int id = rc.getID();
		// System.out.println("THIS IS MY FLAG " + rc.getFlag(id));
		if (decodeFlag(rc.getFlag(id))[3] == 11) { // 11 means in lattice structure
			// System.out.println("YIELDING");
			Clock.yield();
		}
		if (rc.canMove(toGo)) {
			rc.move(toGo);

			if (rc.getLocation().equals(destination))
				rc.setFlag(encodeFlag(0, 0, 0, 11));
			else
				rc.setFlag(encodeFlag(0, 0, 0, 10));
			Clock.yield();
		}
		if (rc.isLocationOccupied(destination)) {
			int idOfRobotAtDestination = rc.senseRobotAtLocation(destination).getID();
			if (!rc.canMove(toGo) && rc.getLocation().isAdjacentTo(destination)
					&& rc.getFlag(idOfRobotAtDestination) == 1 && !rc.getLocation().equals(destination)) {

				findDestination();
			}
		}

		if (!rc.canMove(toGo) && rc.getFlag(id) != 1) {
			Direction[] options = { toGo.rotateLeft(), toGo.rotateRight(), toGo.rotateLeft().rotateLeft(),
					toGo.rotateRight().rotateRight(), toGo.rotateLeft().rotateLeft().rotateLeft(),
					toGo.rotateRight().rotateRight().rotateRight() };
			for (Direction a : options) {
				if (rc.canMove(a)) {
					rc.move(a);
					break;
				}
			}

		}
		if (!rc.canSenseLocation(destination)) {
			findDestination();
		}

	}

// Corner Runner Methods Below

	/**
	 * Determines movement for CornerRunner Muckrakers and checks if they've reached
	 * a corner.
	 * 
	 * @throws GameActionException
	 */
	static void findCorner() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		int cornerNum = decodeFlag(rc.getFlag(rc.getID()))[3];
		switch (cornerNum) {
			case 20:// Go to top left
				if (rc.isReady()) {
					if (rc.canMove(Direction.NORTHWEST)) {
						rc.move(Direction.NORTHWEST);
						return;
					} else if (rc.canMove(Direction.NORTH)) {
						rc.move(Direction.NORTH);
						return;
					} else if (rc.canMove(Direction.WEST)) {
						rc.move(Direction.WEST);
						return;
					} else {// Check if we're at the corner
						MapLocation tempLoc = currentLoc;
						tempLoc = tempLoc.add(Direction.NORTH);
						tempLoc = tempLoc.add(Direction.NORTH);
						MapLocation tempLoc2 = currentLoc;
						tempLoc2 = tempLoc2.add(Direction.WEST);
						tempLoc2 = tempLoc2.add(Direction.WEST);
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
				}
				break;
			case 21: // Go to top right
				if (rc.isReady()) {
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
						tempLoc2 = tempLoc2.add(Direction.EAST);
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
				}
				break;
			case 22: // Go to bottom right
				if (rc.isReady()) {
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
						tempLoc2 = tempLoc2.add(Direction.EAST);
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
				}
				break;
			case 23: // Go to bottom right
				if (rc.isReady()) {
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
						tempLoc2 = tempLoc2.add(Direction.WEST);
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
				}
				break;

		}

		if (isAtCorner) // We've found a corner. Let the world know!
			rc.setFlag(encodeFlag(6, currentLoc, cornerNum));
	}

	/**
	 * Checks if a Muckraker is a cornerRunner
	 * 
	 * @requires rc.RobotType == Muckraker
	 * @ensures if the Robot has a flag that identifies it as a corner runner, it
	 *          returns true
	 * @return if a Muckraker is a cornerRunner
	 * @throws GameActionException
	 */
	static boolean isCornerRunner() throws GameActionException {
		int mission = decodeFlag(rc.getFlag(rc.getID()))[3];
		if (mission >= 20 && mission <= 23)
			return true;
		return false;

	}

	/**
	 * Either creates/replaces CornerRunners and checks to see if a CornerRunner has
	 * reached a corner
	 * 
	 * @ensures closestCorner is the closest corner, 0 = NW, 1 = NE, 2 = SE, 3 = SW
	 * @throws GameActionException
	 */
	static void runCorner() throws GameActionException {
		for (int i = 0; i < cornerRunnerIDs.length; i++) {
			if (rc.canGetFlag(cornerRunnerIDs[i])) {
				int[] tempFlag = decodeFlag(rc.getFlag(cornerRunnerIDs[i]));
				if (tempFlag[0] == 6)// corner has been found
				{
					atCorner[i] = true;
					mapCorners[i] = getMapLocation(tempFlag[1], tempFlag[2]);
				}
			} else {
				makeCornerRunner[i] = true;
			}
		}
		for (int i = 0; i < makeCornerRunner.length; i++) {
			if (makeCornerRunner[i]) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1)) {
						rc.buildRobot(RobotType.MUCKRAKER, dir, 1);
						System.out.println("Built CornerRunner");
						makeCornerRunner[i] = false;
						rc.setFlag(encodeFlag(0, 0, 0, i + 20));
						cornerRunnerIDs[i] = rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID();
						unitIDs.add(rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID());
						break;
					}
				}
			}

		}
		int tempCounter = 0;
		for (int i = 0; i < atCorner.length; i++) {
			if (atCorner[i])
				tempCounter++;
		}
		if (tempCounter >= 2) {
			runCorner = false;
			closestCorner = closestCorner(mapCorners);
			int[] myFlag = decodeFlag(rc.getFlag(rc.getID()));
			rc.setFlag(encodeFlag(myFlag[0], myFlag[1], myFlag[2], closestCorner + 30));
		}
	}



	/**
	 *
	 */
	static void updateCorner() throws GameActionException {

		for (int i = 0; i < cornerRunnerIDs.length; i++) {
			if (rc.canGetFlag(cornerRunnerIDs[i])) {
				int[] tempFlag = decodeFlag(rc.getFlag(cornerRunnerIDs[i]));
				if (tempFlag[0] == 6)// corner has been found
				{
					atCorner[i] = true;
					System.out.println("FOUND CORNER: " + i);
					mapCorners[i] = getMapLocation(tempFlag[1], tempFlag[2]);
				}
			} else {
				makeCornerRunner[i] = true;
			}
		}

		int tempCounter = 0;
		for (int i = 0; i < atCorner.length; i++) {
			if (atCorner[i])
				tempCounter++;
		}
		if (tempCounter >= 2) {
			System.out.println("FOUND >TWO CORNERs:" + Arrays.toString(atCorner));
			System.out.println("MAPCORNERS: " + Arrays.toString(mapCorners));
			int[] flag = decodeFlag(rc.getFlag(rc.getID()));
			runCorner = false;
			closestCorner = closestCorner(mapCorners);
			rc.setFlag(encodeFlag(flag[0], flag[1], flag[2], closestCorner + 30));
		}
	}

	/**
	 * Calculates closestCorner based on array
	 * 
	 * @param corners, array containing MapLocations
	 * @ensures closestCorner is the closest corner, 0 = NW, 1 = NE, 2 = SE, 3 = SW
	 * @return closestCorner
	 */
	static int closestCorner(MapLocation[] corners) {
		MapLocation currentLoc = rc.getLocation();
		MapLocation cornerLoc = rc.getLocation();
		int cornerDis = 10000;
		int finalDir = 0;
		for (int i = 0; i < corners.length; i++) {
			if (corners[i] != null) {
				MapLocation temp = corners[i];
				if (currentLoc.distanceSquaredTo(temp) < cornerDis) {
					cornerLoc = temp;
					finalDir = i;
					cornerDis = currentLoc.distanceSquaredTo(temp);
				}
			}
		}
		return finalDir;
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
		else if(xDif<=-64)
			loc = loc.translate((128 + xDif) % 64, 0);
		else//larger than 64
			loc=loc.translate((-128 + xDif)%64,0);

		if (yDif < 64 && yDif > -64)
			loc = loc.translate(0, yDif);
		else if(yDif<=-64)
			loc = loc.translate(0, (128 + yDif) % 64); //eg, -96 means up 32
		else
			loc=loc.translate(0,(-128 + yDif)%64);


		return loc;
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


	/** Does a random move. Attempts all directions
	 *
	 */
	static void doRandomMove() throws GameActionException{
		int start=(int) (Math.random() * directions.length);
		Direction dir;
		for(int i=0;i<directions.length;i++){
			dir=directions[(i+start)%directions.length];
			if(rc.canMove(dir))
				rc.move(dir);
		}

	}
}
