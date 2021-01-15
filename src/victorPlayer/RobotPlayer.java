package victorPlayer;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    //protecting is whether or not a robot is protecting a square or not
    static int turnCount;
    static boolean protecting = false;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        
        
        
        
        //currently this is only running for politicians but definitely add more once everything is combined together
        latticeStructure();
       // if (tryMove(randomDirection()))
            //System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
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
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
    
    
    
    //this method builds the lattice structure
    
    static void latticeStructure() throws GameActionException
    {
    	//if it is protecting then this method just makes the robot not move at all
    	if(protecting)
    	{
    		System.out.println("I AM PROTECTING");
    		Clock.yield();
    	}
    	//if not protecting then this  tries to make it protecting
    	while(protecting == false)
    	{
    		protecting = afterMovedIsProtecting();

    	}
    	
    }
    
    
    //first this method moves the robot and then if it is on a square that is deemed good to protect then it switches protecting to true
    
    static boolean afterMovedIsProtecting() throws GameActionException
    {
    	
    	//to try to keep protection all around, random number generator to generate which square to go to
    	int random = (int) (Math.random()*4);
    	if(onCorner())
    	{
    		if(random == 0)
    			if(rc.canMove(Direction.NORTHEAST))
    			{
    				rc.move(Direction.NORTHEAST);
    				return true;
    			}
    		if(random == 1)
    			if(rc.canMove(Direction.NORTHWEST))
    			{
    				rc.move(Direction.NORTHWEST);
    				return true;
    			}
    		if(random == 2)
    			if(rc.canMove(Direction.SOUTHEAST))
    			{
    				rc.move(Direction.SOUTHEAST);
    				return true;
    			}
    		if(random == 3)
    			if(rc.canMove(Direction.SOUTHWEST))
    			{
    				rc.move(Direction.SOUTHWEST);
    				return true;
    			}
    	}
    	
    	//if it is not on a corner square (explained more detail in next method) then it tries to get on one
    	if(!onCorner())
    	{
    		if(random == 0)
    			if(rc.canMove(Direction.EAST))
    				rc.move(Direction.EAST);
    		if(random == 1)
    			if(rc.canMove(Direction.SOUTH))
    				rc.move(Direction.SOUTH);
    		if(random == 2)
    			if(rc.canMove(Direction.WEST))
    				rc.move(Direction.WEST);
    		if(random == 3)
    			if(rc.canMove(Direction.NORTH))
    				rc.move(Direction.NORTH);
    	}
		return false;
    }
    
    
    /*   Corner square is established as so: 
     * 		Pretend u is your robot and X are the available spots around it
     * 		X   X
     * 		  U
     * 		X	X
     * 		This would be considered a corner spot
     * 		The blank spaces are spaces where the robots are freely able to move around to try to find corner squares
     * 		The corner squares allow a robot to choose which square to protect
     */
    static boolean onCorner() throws GameActionException
    {
    	//currently there is a small bug where if a robot is stopped on a non corner square, it will think that robot is on a corner square
    	//hopefully that will be fixed soon
    	MapLocation currentLocation = rc.getLocation();
    	
    	MapLocation locationS = currentLocation.add(Direction.SOUTH);
    	RobotInfo robotS = rc.senseRobotAtLocation(locationS);
    	if(robotS.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
    		if(rc.canMove(Direction.EAST))
    			rc.move(Direction.EAST);
    	
    	if(!rc.canMove(Direction.NORTHEAST))
    	{
    		MapLocation locationNE = currentLocation.add(Direction.NORTHEAST);
    		RobotInfo robotNE = rc.senseRobotAtLocation(locationNE);
    		if(robotNE.getType().equals(rc.getType()) || robotNE.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
    			return true;
    	}
    	if(!rc.canMove(Direction.NORTHWEST))
    	{
    		MapLocation locationNW = currentLocation.add(Direction.NORTHWEST);
    		RobotInfo robotNW = rc.senseRobotAtLocation(locationNW);
    		if(robotNW.getType().equals(rc.getType()) || robotNW.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
    			return true;
    	}
    	if(!rc.canMove(Direction.SOUTHEAST))
    	{
    		MapLocation locationSE = currentLocation.add(Direction.SOUTHEAST);
    		RobotInfo robotSE = rc.senseRobotAtLocation(locationSE);
    		if(robotSE.getType().equals(rc.getType()) || robotSE.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
    			return true;
    	}
    	if(!rc.canMove(Direction.SOUTHWEST))
    	{
    		MapLocation locationSW = currentLocation.add(Direction.SOUTHWEST);
    		RobotInfo robotSW = rc.senseRobotAtLocation(locationSW);
    		if(robotSW.getType().equals(rc.getType()) || robotSW.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
    			return true;
    	}
    /*	if(!rc.canMove(Direction.NORTHEAST) || !rc.canMove(Direction.NORTHWEST) || !rc.canMove(Direction.SOUTHEAST) || !rc.canMove(Direction.SOUTHWEST))
    		return true;*/
    	return false;
    }
}
