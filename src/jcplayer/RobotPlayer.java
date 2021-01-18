package jcplayer;

import battlecode.common.*;
import java.util.ArrayList;

public strictfp class RobotPlayer {
	static RobotController rc;

	static final RobotType[] spawnableRobot = { RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER, };

	static final Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, };

	static int turnCount;

	// Variables that store Location of Enlightenment Center that created the robot
	static MapLocation ecLoc;

	static MapLocation[] mapCorners = new MapLocation[4];
	static int[] cornerRunnerIDs = new int[4];
	static boolean[] makeCornerRunner = { true, true, true, true };
	static boolean[] atCorner = { false, false, false, false };
	static boolean runCorner = true;
	static MapLocation closestCorner;

	// Variables that store ID number of the created units for enlightenment centers
	static ArrayList<Integer> createdPoliticiansID = new ArrayList<Integer>();
	static ArrayList<Integer> createdSlanderersID = new ArrayList<Integer>();
	static ArrayList<Integer> createdMuckrakersID = new ArrayList<Integer>();
	
    /*Stores what needs to be built
    They should be added in as influence*10 + type, where slanderer = 1, politician = 2, muckraker =3
    -Winston */
	 static ArrayList<Integer> toBeConstructed = new ArrayList<Integer>();
	
	 //Stores the previous round's number of created units and influence
	 static int previousPolitician;
	 static int previousSlanderer;
	 static int previousMuckrakers;
	 static int previousInfluence;

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

		// System.out.println("I'm a " + rc.getType() + " and I just got created!");
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
			if (runCorner) {
				flag = runCorner(rc);
			}
		}
		rc.setFlag(flag);
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
						int flag = rc.getFlag(temp.ID);

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
		if (rc.isReady()) {
			if (isCornerRunner(rc)) {
				findCorner(rc);
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

	
	/*
    Runs Stage One of the Strategy:
        3 Slanderer - 41 Influence (Total 123 Influence, 2 initially and 3rd is staggered)
        1 Politician - 25 Influence
        3 Politicians - 11 Influence
        Bidding 1 for vote
        - Winston */
    static void runStageOne() throws GameActionException {
        if (createdMuckrakersID.size() < previousMuckrakers) {
            for (int i = 0; i < previousMuckrakers - createdMuckrakersID.size(); i++) {
                toBeConstructed.add(0, 13);
            }
        }
        if (createdSlanderersID.size() < previousSlanderer) {
            for (int i = 0; i < previousSlanderer - createdSlanderersID.size(); i++) {
                toBeConstructed.add(0, 411);
            }
        }
        if (createdPoliticiansID.size() < previousPolitician) {
            for (int i = 0; i < previousPolitician - createdPoliticiansID.size(); i++) {
                toBeConstructed.add(0, 202);
            }
        }
        if (rc.isReady())
        {
         if (toBeConstructed.size() == 0) {
                if (rc.canBid(1)) {
                   rc.bid(1);
             }
         }
         else if (canConstruct()) {
             construct();
            }
        }
        setPrevious();
    }
    static void runStageTwo() throws GameActionException {
        if (createdMuckrakersID.size() < previousMuckrakers) {
            for (int i = 0; i < previousMuckrakers - createdMuckrakersID.size(); i++) {
                toBeConstructed.add(0, 13);
            }
        }
        if (createdSlanderersID.size() < previousSlanderer) {
            for (int i = 0; i < previousSlanderer - createdSlanderersID.size(); i++) {
                toBeConstructed.add(0, 411);
            }
        }
        if (createdPoliticiansID.size() < previousPolitician) {
            for (int i = 0; i < previousPolitician - createdPoliticiansID.size(); i++) {
                toBeConstructed.add(0, 202);
            }
        }
        if (rc.isReady())
        {

            if (canConstruct()) {
                construct();
            }
            else if (toBeConstructed.size() == 0)
            {
                if(turnCount%4 == 1 || turnCount%4 == 3) {
                    int influenceDifference = rc.getInfluence() - previousInfluence;
                    if (rc.canBid(influenceDifference)) {
                        rc.bid(influenceDifference);
                    }
                }
                else
                {
                    toBeConstructed.add(13);
                    construct();
                }
            }

        }
        setPrevious();
    }
    
    /* Sets all the previous variables to current round settings.
    * To be used right before moving onto the next round*/
    static void setPrevious()
    {
        previousPolitician = createdPoliticiansID.size();
        previousMuckrakers = createdMuckrakersID.size();
        previousSlanderer = createdSlanderersID.size();
        previousInfluence = rc.getInfluence();
    }
    /* Builds units according to the ArrayList toBeConstructed

     */
    static void construct() throws GameActionException
    {
        int code = toBeConstructed.get(0);
        int influence = code/10;
        RobotType toBuild;
        if(code%10 == 1)
        {
            toBuild = RobotType.SLANDERER;
            System.out.println("CHECKED SLANDERER: " + code);
        }
        else if(code%10 == 2)
        {
            toBuild = RobotType.POLITICIAN;
            System.out.println("CHECKED POLITICIAN: " + code);
        }
        else
        {
            toBuild = RobotType.MUCKRAKER;
            System.out.println("CHECKED MUCKRAKER: " + code);
        }
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence))
            {
                System.out.println("BUILT");
                rc.buildRobot(toBuild, dir, influence);
                toBeConstructed.remove(0);
            }
            else
            {
                break;
            }
        }
    }
    /*Tells you if you can construct the unit on top of the stack*/

    static boolean canConstruct() throws GameActionException
    {
        if(toBeConstructed.size() == 0)
        {
            return false;
        }
        int code = toBeConstructed.get(0);
        int influence = code/10;
        RobotType toBuild;
        if(code%10 == 1)
        {
            toBuild = RobotType.SLANDERER;
        }
        if(code%10 == 2)
        {
            toBuild = RobotType.POLITICIAN;
        }
        else
        {
            toBuild = RobotType.MUCKRAKER;
        }
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                return true;
            }
        }
        return false;
    }
    /**
     *To be run as an enlightenment center
     * Also updates slanderers to politicians in the local list
     * Returns integer array of first the cnt, and then the direction of the first slanderer detected
     * @param type The type of robot to be counted
     *-Ben
     */
    static int[] getCntUnit(RobotType type) throws GameActionException{
        int cnt=0;
        int dir=0;
        switch(type) {
            case POLITICIAN:
                for (int id : createdPoliticiansID)
                    if (rc.canGetFlag(id))//canGetFlag is used as a proxy for whether it exists
                        cnt++;
            case SLANDERER:
                for (int x=0;x<createdSlanderersID.size();x++) {
                    int id=createdSlanderersID.get(x);
                    if (rc.canGetFlag(id))
                        if (rc.canSenseRobot(id)&&rc.senseRobot(id).getType().equals(RobotType.SLANDERER)) {
                            cnt++;
                            if(rc.getFlag(id)>=11&&rc.getFlag(id)<=14){
                                if(rc.getFlag(rc.getID())%10==0){//We find direction of slanderer if we haven't set one yet
                                    dir=rc.getFlag(id)%10;
                                }
                            }
                        }
                        else { //Ensure that slanderer hasn't changed to a politician and update
                            createdSlanderersID.remove(x);
                            createdPoliticiansID.add(id);
                        }
                }
            case MUCKRAKER:
                for (int id : createdMuckrakersID)
                    if (rc.canGetFlag(id))
                        cnt++;

        }
        int[] ret={cnt,dir};
        return ret;

    }

    /**
     *Outputs what we want the enlightenment center flag to be
     * @param p politician cnt
     * @param s slanderer cnt
     * @param m muckracker cnt
     *  2^24=1 67 77 21 6
     *          p  s  m  dir
     * @param dir 1 means top left, 2 top right, 3 bottom right, 4 bottom left
     *-Ben
     */
    static int getEnlFlag(int p, int s, int m, int dir) throws GameActionException{
        int flg=0;
        flg+=p*100000;
        flg+=s*100;
        flg+=m*10;
        flg+=dir;
        if(rc.canSetFlag(flg))//Could theoretically be removed after testing
            return flg;
        return 0;

    }


    /**Code for non-enlightenment center units
     * Finds the ID of the enlightenment center that created it and sets that to a proper number
     * -Ben
     */
    static void firstTurn(){
        RobotInfo[] nearby=rc.senseNearbyRobots(2,rc.getTeam());
        for(RobotInfo r : nearby){
            if(r.getType().equals(RobotType.ENLIGHTENMENT_CENTER)){
                enlightenmentCenterID=r.getID();
                return;
            }

        }
    }
    
	static void findCorner(RobotController rc) throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
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
	}
	
	static boolean isCornerRunner(RobotController rc) throws GameActionException {
		// checks Robot Flag's to see if it is a corner runner
		int flag = rc.getFlag(rc.getID());
		boolean isCornerRunner = false;
		if (flag / 1000000 > 0)
			isCornerRunner = true;
		return isCornerRunner;
	}
	
	static int runCorner(RobotController rc) throws GameActionException {
		int flag = rc.getFlag(rc.getID());
		for (int i = 0; i < cornerRunnerIDs.length; i++) {
			if (rc.canGetFlag(cornerRunnerIDs[i])) {
				int tempFlag = rc.getFlag(cornerRunnerIDs[i]);
				if ((tempFlag / 10000000) == 1) {
					atCorner[i] = true;
					mapCorners[i] = getCornerLocation(rc, tempFlag);
				}
			} else {
				makeCornerRunner[i] = true;
			}
		}
		for (int i = 0; i < makeCornerRunner.length; i++) {
			if (makeCornerRunner[i]) {
				for (Direction dir : directions) {
					if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 20)) {
						rc.buildRobot(RobotType.MUCKRAKER, dir, 20);
						System.out.println("Built CornerRunner");
						makeCornerRunner[i] = false;
						flag = i;
						cornerRunnerIDs[i] = rc.senseRobotAtLocation(rc.getLocation().add(dir)).getID();
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
		if (tempCounter == 4) {
			runCorner = false;
			System.out.println("Corners have been found!");
			System.out.println("Northwest Corner " + mapCorners[0].x + "," + mapCorners[0].y);
			System.out.println("Northeast Corner " + mapCorners[1].x + "," + mapCorners[1].y);
			System.out.println("Southeast Corner " + mapCorners[2].x + "," + mapCorners[2].y);
			System.out.println("Southwest Corner " + mapCorners[3].x + "," + mapCorners[3].y);
			closestCorner = closestCorner(rc, mapCorners);
			System.out.println("Closest Corner + " + closestCorner.x + "," + closestCorner.y);
		
		}
		return flag;
	}

	static MapLocation getCornerLocation(RobotController rc, int flag) {
		MapLocation corner = rc.getLocation();
		int x = 0;
		int y = 0;
		if (flag % 1000000 >= 100000)
			x = 0 - (flag % 100000) / 1000;
		else
			x = (flag % 100000) / 1000;
		if (flag % 1000 >= 100)
			y = 0 - (flag % 100);
		else
			y = flag % 100;
		corner = corner.translate(x, y);
		return corner;
	}
	
	static MapLocation closestCorner(RobotController rc, MapLocation[] corners) {
		MapLocation currentLoc = rc.getLocation();
		MapLocation cornerLoc = rc.getLocation();
		int cornerDis = 10000;
		for(MapLocation temp : corners) {
			if(currentLoc.distanceSquaredTo(temp) < cornerDis) {
				cornerLoc = temp;
				cornerDis = currentLoc.distanceSquaredTo(temp);
			}		
		}
		return cornerLoc;
	}
}
