package WinstonPlayer;
import battlecode.common.*;
import java.util.ArrayList;

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

    static int turnCount;

    //stores schema of the communications and how much to shift each n-1 parameter
    static int[] schema = {4,7,7,6};
    static int[] shift= {schema[1]+schema[2]+schema[3],schema[2]+schema[3],schema[3]};

    //Arraylist of arrays
    //Each array is +1 size larger than it's supposed to be, with the unit code in the 0 position
    //The unit code being influence *10 + (1/2/3) for (politicians/slanderers/muckrakers)
    static ArrayList<int[]> Units = new ArrayList<int[]>();

    // Variables that store Location of Enlightenment Center that created the robot
    static MapLocation ecLoc;

    static MapLocation[] mapCorners = new MapLocation[4];
    static int[] cornerRunnerIDs = new int[4];
    static boolean[] makeCornerRunner = { true, true, true, true };
    static boolean[] atCorner = { false, false, false, false };
    static boolean runCorner = true;
    static MapLocation closestCorner;

    //Stores the previous round's influence
    static int previousInfluence = 0;

    //Indicates the mode for building armies
    //Each mode lasts for 15 actions
    //1 = muckrakers, 2 = politicians, 3 = bidding
    static int[] modeCount = new int[2];

    //protecting is whether or not a robot is protecting a square or not
    static boolean protecting = false;
    static boolean hasDestination;
    static MapLocation destination;

    //Store ID of the enlightenment center that created you
    //-Ben
    static int enlightenmentCenterID;

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

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
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

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    //It does stuff
    //Things should be added in the order: muckraker, politician, slanderer for each stage
    static void runEnlightenmentCenter() throws GameActionException
    {
        rc.setFlag(1);
        /*3 Slanderer - 41 Influence (412)
        3 Politician - 20 Influence (201)
        4 Corner Runners Politicians - 1 Influence (11) */
        if(turnCount == 1)
        {
            int[] slanderers41 = {412};
            int[] politicians20 = {201};
            int[] muckrakers1 = {13};

            Units.add(resizeArray(muckrakers1, 5));
            Units.add(resizeArray(politicians20, 4));
            Units.add(resizeArray(slanderers41,4));

        }
        /*Slanderers making around 20 influence per round
        5 Slanderers with 85 Influence each (852)
        18 Total Politicians with 20 Influence (201)
        8 Enlightenment Center Guards
        12 Politicians → Neutral Enlightenment Centers, move randomly in a select direction
        Make 10 Muckrakers with 10 Influence (103)*/
        else if(turnCount == 30)
        {
            int[] slanderers85 = {1702};
            int[] muckrakers10 = {203};

            resizeFromList(201,11);

            Units.add(1,resizeArray(muckrakers10, 6));
            Units.add(resizeArray(slanderers85, 4));
        }
        /*Slanderers making 50 influence per round
        5 Slanderers with 230 Influence each (2302)
        10 Total Politicians with 80 influence each (801)
        5 Total Politicians with 20 influence each (201)
        8 Enlightenment Center Guards 20 influence
        15 Politicians → Neutral Enlightenment Centers, move randomly in a select direction
        Make 10 Muckrakers with 50 Influence each (503)*/
        else if(turnCount == 165)
        {
            int[] slanderers230 = {4602};
            int[] politicians80 = {1601};
            int[] muckrakers50 = {1003};

            for(int i = 0; i<Units.size(); i++)
            {
                if(Units.get(i)[0] == 201)
                {
                    for(int x = 18; x>14; x--)
                    {
                        Units.get(i)[x] = 0;
                    }
                }
            }

            Units.add(resizeArray(muckrakers50, 6));
            Units.add(resizeArray(politicians80, 6));
            Units.add(resizeArray(slanderers230, 4));

        }
        /*Slanderers making 200 influence per round
        5 Slanderers with 1498 Influence each (14982)
        20 Total Politicians with 400 influence each (4001)
        5 Total Politicians with 100 influence (1001)
        24 Enlightenment Center Guards 100 influence (1001)
        20 Politicians → Neutral Enlightenment Centers, move randomly in a select direction
        Make 10 Muckrakers with 100 Influence each (1003)*/
        else if(turnCount == 360)
        {
            int[] slanderers1498 = {14982};
            int[] muckrakers100 = {2003};
            int[] politicians400 = {4001};
            int[] politicians100 = {2001};

            Units.add(resizeArray(muckrakers100, 6));
            Units.add(resizeArray(politicians100, 16));
            Units.add(resizeArray(politicians400, 11));
            Units.add(resizeArray(slanderers1498, 6));
        }
        /*Make 10 Muckrakers with 100 Influence Each
        20 Politicians → Neutral Enlightenment Centers, move randomly in a select direction
        Bid for 100 Votes*/
        else if(turnCount == 555)
        {
            for(int i = 0; i<Units.size(); i++)
            {
                int x = Units.get(i)[0];
                if(x != 14982 || x != 1001 || x !=4001)
                {
                    Units.remove(i);
                }
            }
            modeCount[0] = 1;
            modeCount[1] = 0;
        }
        else if(turnCount > 555)
        {
            runStageTwo();
        }
        else
        {
            runStageOne();
        }
    }

    static void runStageOne() throws GameActionException {
        replace();
        Boolean stop = true;
        if (rc.isReady())
        {
            stop = buildUnits();
        }
        if(!stop)
        {
            System.out.println("trying to bid");
            int previousIncome = rc.getInfluence() - previousInfluence;
            if(rc.canBid((int)(previousIncome*1.5)))
            {
                System.out.println("bid");
                rc.bid((int)(previousIncome*1.5));
            }
        }
        setPrevious();
    }
     static void runStageTwo() throws GameActionException
     {
         if(rc.isReady())
         {
            if(modeCount[0] == 1)
            {
                System.out.println("Mode Count 1");
                if(canConstruct(RobotType.MUCKRAKER, 100))
                {
                    construct(RobotType.MUCKRAKER, 100);
                    modeCount[1]++;
                }
                if(modeCount[1] >= 10)
                {
                    modeCount[0] = 2;
                    modeCount[1] = 0;
                }
            }
            else if(modeCount[0] == 2)
            {
                System.out.println("Mode Count 2");
                if(canConstruct(RobotType.POLITICIAN, 100))
                {
                    construct(RobotType.POLITICIAN, 100);
                    modeCount[1]++;
                }
                if(modeCount[1] >= 10)
                {
                    modeCount[0] = 3;
                    modeCount[1] = 0;
                }
            }
            else if(modeCount[0] == 3)
            {
                System.out.println("Mode Count 3");
                if(rc.canBid(100))
                {
                    rc.bid(100);
                    modeCount[1]++;
                }
                if(modeCount[1] >= 20)
                {
                    modeCount[0] = 1;
                    modeCount[1] = 0;
                }
            }
            else if(modeCount[0] == 4)
            {
                Boolean a = buildUnits();
                modeCount[1]++;
                if(!a)
                {
                    modeCount[0] = 1;
                    modeCount[1] = 0;
                }
                else if(modeCount[1] >= 40)
                {
                    modeCount[0] = 1;
                    modeCount[1] = 0;
                }
            }
         }

         setPrevious();
     }


    //Builds units based on empty spots in the arrays, starting from the end (higher influence units) and
    //working towards the beginning
    //Returns false if there's nothing to be built
    //return true if it tried to build something
     static Boolean buildUnits() throws GameActionException
     {
         RobotType toBuild;
         int toBuildInfluence;
         for(int i = Units.size()-1; i>=0; i--)
         {
             int x = indexOfArray(Units.get(i), -1);
             if(x != -1)
             {
                 int y = Units.get(i)[0]%10;
                 if(Units.get(i)[0] == 13)
                 {
                     runCorner(rc);
                     break;
                 }
                 else if(y == 1)
                 {
                     toBuild = RobotType.POLITICIAN;
                 }
                 else if(y == 2)
                 {
                     toBuild = RobotType.SLANDERER;
                 }
                 else
                 {
                     toBuild = RobotType.MUCKRAKER;
                 }
                 toBuildInfluence = Units.get(i)[0]/10;
                 if(canConstruct(toBuild, toBuildInfluence))
                 {
                     MapLocation loc = construct(toBuild, toBuildInfluence);
                     Units.get(i)[x] = rc.senseRobotAtLocation(loc).getID();
                     return true;
                 }
                 else
                 {
                     return true;
                 }
             }
         }
         System.out.println("full");
         return false;
     }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            // System.out.println("empowering...");
            rc.empower(actionRadius);
            // System.out.println("empowered");
            return;
        }

        //currently this is only running for politicians but definitely add more once everything is combined together

        if(rc.getFlag(rc.getID()) == 10)
        {
            tryMove(randomDirection());
        }
        else
        {
            latticeStructure();
        }
        // if (tryMove(randomDirection()))
        //System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        if(turnCount==1)
            firstTurn();

        if(!rc.isReady())
            return;

        int ECFlag=decodeFlag(rc.getFlag(enlightenmentCenterID))[3];
        int cornerNum = 0;
        if(ECFlag>=20&&ECFlag<=23)
            cornerNum=ECFlag;
        else
            cornerNum=decodeFlag(rc.getFlag(rc.getID()))[3];
        if(cornerNum>=20&&cornerNum<=23) {
            switch (cornerNum) {
                case 20:
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
                case 21:
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
                case 22:
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
                case 23:
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
        }
        else
        {
            tryMove(randomDirection());
        }



//Try moving randomly
    }

    static void runMuckraker() throws GameActionException {
        if(turnCount==1)
            firstTurn();//Will set enlightenmentCenterID
        if(turnCount==1)
            firstTurn();//Will set enlightenmentCenterID
        if(turnCount==1){
            int[] ECFlag=decodeFlag(rc.getFlag(enlightenmentCenterID));
            switch(ECFlag[3]){
                case 20:
                    rc.setFlag(encodeFlag(0,0,0,20));
                    break;
                case 21:
                    rc.setFlag(encodeFlag(0,0,0,21));
                    break;
                //Whatever
                case 22:
                    rc.setFlag(encodeFlag(0,0,0,22));
                    break;
                //Whatever
                case 23:
                    rc.setFlag(encodeFlag(0,0,0,23));
                    break;
                //Whatever

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
    }
    //this method builds the lattice structure

    static void latticeStructure() throws GameActionException
    {
        //if it is protecting then this method just makes the robot not move at all
    	/*if(protecting)
    	{
    		System.out.println("I AM PROTECTING");
    		Clock.yield();
    	}
    	//if not protecting then this  tries to make it protecting
    	while(protecting == false)
    	{
    		protecting = afterMovedIsProtecting();
    	}*/
        int id = rc.getID();
        if(hasDestination == false)
            findDestination();
        //System.out.println("" + destination.x + " " + destination.y);
        traverse(destination);
    }

    static void findDestination() throws GameActionException
    {
        //int random = (int) (Math.random()*8);

        RobotInfo[] nearby = rc.senseNearbyRobots(2);
        if(nearby.length == 0)
        {
           // System.out.println("NO NEARBY!!");
        }
        MapLocation center = rc.getLocation();

        ArrayList<MapLocation> options = new ArrayList<MapLocation>();


        for(int i = 0; i < nearby.length; i++)
        {
            int id = nearby[i].getID();
            //currently 1 is if the robot is protecting something or is the enlightenment center
            if(rc.getFlag(id) == 1)
            {
                options.add(nearby[i].getLocation());
            }


        }
       // System.out.println("OPTIONS SIZE IS " + options.size());
        int optionsRandom = (int)(Math.random()*options.size());
        center = options.get(optionsRandom);

        MapLocation nw = center.add(Direction.NORTHWEST).add(Direction.NORTHWEST);
        MapLocation n = center.add(Direction.NORTH).add(Direction.NORTH);
        MapLocation ne = center.add(Direction.NORTHEAST).add(Direction.NORTHEAST);
        MapLocation e = center.add(Direction.EAST).add(Direction.EAST);
        MapLocation se = center.add(Direction.SOUTHEAST).add(Direction.SOUTHEAST);
        MapLocation s = center.add(Direction.SOUTH).add(Direction.SOUTH);
        MapLocation sw = center.add(Direction.SOUTHWEST).add(Direction.SOUTHWEST);
        MapLocation w = center.add(Direction.WEST).add(Direction.WEST);

        MapLocation[] mapSquares = {nw, n, ne, e, se, s, sw, w};
        int[] booleanmapSquares = new int[8];

        //0 is location is occupied, 1 is location is empty, 2 is if location is not on the map

        for(int i = 0; i < 8; i++)
        {

            if(!rc.onTheMap(mapSquares[i]))
            {
                booleanmapSquares[i] = 2;
            }
            else if(rc.isLocationOccupied(mapSquares[i]))
            {
                booleanmapSquares[i] = 0;
            }
            else
            {
                booleanmapSquares[i] = 1;
            }
        }

        ArrayList<MapLocation> availableLocations = new ArrayList<MapLocation>();
        ArrayList<MapLocation> locationsOnTheMap = new ArrayList<MapLocation>();



        for(int i = 0; i < 8; i++)
        {
            if(booleanmapSquares[i] == 1)
                availableLocations.add(mapSquares[i]);
            if(booleanmapSquares[i] != 2)
                locationsOnTheMap.add(mapSquares[i]);
        }

        if(availableLocations.size() > 0)
        {
            int random = (int)(Math.random()*availableLocations.size());
            destination = availableLocations.get(random);
        }
        else
        {
            int random = (int)(Math.random()*locationsOnTheMap.size());
            destination = locationsOnTheMap.get(random);
            destination = locationsOnTheMap.get(random);
        }

    	/*switch (random)
    	{
    		case 0:
    			System.out.println("THIS IS 0");
    			destination = center.add(Direction.NORTHWEST);
    			destination = destination.add(Direction.NORTHWEST);
    			break;
    		case 1:
    			System.out.println("THIS IS 1");
    			destination = center.add(Direction.NORTH);
    			destination = destination.add(Direction.NORTH);
    			break;
    		case 2:
    			System.out.println("THIS IS 2");
    			destination = center.add(Direction.NORTHEAST);
    			destination = destination.add(Direction.NORTHEAST);
    			break;
    		case 3:
    			System.out.println("THIS IS 3");
    			destination = center.add(Direction.EAST);
    			destination = destination.add(Direction.EAST);
    			break;
    		case 4:
    			System.out.println("THIS IS 4");
    			destination = center.add(Direction.SOUTHEAST);
    			destination = destination.add(Direction.SOUTHEAST);
    			break;
    		case 5:
    			System.out.println("THIS IS 5");
    			destination = center.add(Direction.SOUTH);
    			destination = destination.add(Direction.SOUTH);
    			break;
    		case 6:
    			System.out.println("THIS IS 6");
    			destination = center.add(Direction.SOUTHWEST);
    			destination = destination.add(Direction.SOUTHWEST);
    			break;
    		case 7:
    			System.out.println("THIS IS 7");
    			destination = center.add(Direction.WEST);
    			destination = destination.add(Direction.WEST);
    			break;
    	}*/

        hasDestination = true;

    }

    static void traverse(MapLocation destination) throws GameActionException
    {
        Direction toGo = rc.getLocation().directionTo(destination);
        int id = rc.getID();
        //System.out.println("THIS IS MY FLAG " + rc.getFlag(id));
        if(rc.getFlag(id) == 1)
        {
            //System.out.println("YIELDING");
            Clock.yield();

        }
        if(rc.canMove(toGo))
        {
            rc.move(toGo);

            if(rc.getLocation().equals(destination))
                rc.setFlag(1);
            else
                rc.setFlag(0);
            Clock.yield();
        }
        if(rc.isLocationOccupied(destination))
        {
            int idOfRobotAtDestination = rc.senseRobotAtLocation(destination).getID();
            if(!rc.canMove(toGo) && rc.getLocation().isAdjacentTo(destination) && rc.getFlag(idOfRobotAtDestination) == 1 && !rc.getLocation().equals(destination))
            {

                findDestination();
            }
        }

        if(!rc.canMove(toGo) && rc.getFlag(id) != 1)
        {
            Direction[] options = {toGo.rotateLeft(), toGo.rotateRight(), toGo.rotateLeft().rotateLeft(), toGo.rotateRight().rotateRight(), toGo.rotateLeft().rotateLeft().rotateLeft(), toGo.rotateRight().rotateRight().rotateRight()};
            for (Direction a: options)
            {
                if(rc.canMove(a))
                {
                    rc.move(a);
                    break;
                }
            }

        }
        if(!rc.canSenseLocation(destination))
        {
            findDestination();
        }




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

    static void findCorner(RobotController rc) throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        boolean isAtCorner = false;
        int cornerNum=decodeFlag(rc.getFlag(rc.getID()))[3];
        switch (cornerNum) {
            case 20:
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
            case 21:
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
            case 22:
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
            case 23:
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
            cornerNum=decodeFlag(rc.getFlag(rc.getID()))[3];
            rc.setFlag(encodeFlag(6,currentLoc,cornerNum));
        }
    }

    static boolean isCornerRunner(RobotController rc) throws GameActionException
    {
        int mission=decodeFlag(rc.getFlag(rc.getID()))[3];
        if(mission>=20&&mission<=23)
            return true;
        return false;

    }

    static int runCorner(RobotController rc) throws GameActionException {
        int flag = rc.getFlag(rc.getID());
        for (int i = 0; i < cornerRunnerIDs.length; i++) {
            if (rc.canGetFlag(cornerRunnerIDs[i])) {
                int[] tempFlag=decodeFlag(rc.getFlag(cornerRunnerIDs[i]));
                if(tempFlag[0]==6)//corner has been found
                {
                    atCorner[i] = true;
                    mapCorners[i]=getMapLocation(tempFlag[1],tempFlag[2]);
                }
            } else {
                makeCornerRunner[i] = true;
            }
        }
        for (int i = 0; i < makeCornerRunner.length; i++) {
            if (makeCornerRunner[i]) {
                for (Direction dir : directions) {
                    if (rc.canBuildRobot(RobotType.MUCKRAKER, dir, 10)) {
                        rc.buildRobot(RobotType.MUCKRAKER, dir, 10);
                        System.out.println("Built CornerRunner");
                        makeCornerRunner[i] = false;
                        rc.setFlag(encodeFlag(0,0,0,i+20));
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
        if (tempCounter >= 2) {
            runCorner = false;
            int closestCorner=closestCorner(mapCorners);
            rc.setFlag(encodeFlag(6,mapCorners[closestCorner].x%128,mapCorners[closestCorner].y%128,closestCorner+20));
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

    static int closestCorner(MapLocation[] corners) {
        MapLocation currentLoc = rc.getLocation();
        MapLocation cornerLoc = rc.getLocation();
        int cornerDis = 10000;
        int finalDir = 0;
        for(int i = 0; i < corners.length; i++){
            if(corners[i] != null){
                MapLocation temp = corners[i];
                if(currentLoc.distanceSquaredTo(temp) < cornerDis) {
                    cornerLoc = temp;
                    finalDir = i;
                    cornerDis = currentLoc.distanceSquaredTo(temp);
                }
            }
        }
        return finalDir;
    }
    //Goes through all arrays and checks whether the determined id can be found
    //If it can't be found, it replaces it with a -1, signalling it needs to be replaced
    //If it's a 0, it ignores it
    static void replace() throws GameActionException
    {
        for(int i = 0; i<Units.size(); i++)
        {
            int x = Units.get(i)[0];
            if(x%10 == 2)
            {
                for(int ii = 1; ii<Units.get(i).length; ii++)
                {
                    if(Units.get(i)[ii] == 0)
                    {
                    }
                    else if(!rc.canGetFlag(Units.get(i)[ii]))
                    {
                        Units.get(i)[ii] = -1;
                    }
                    else if(rc.getFlag(Units.get(i)[ii]) == 10)
                    {
                        Units.get(i)[ii] = -1;
                    }
                }
            }
            else
            {
                for(int iii = 1; iii<Units.get(i).length; iii++)
                {
                    if(Units.get(i)[iii] == 0)
                    {
                    }
                    else if(!rc.canGetFlag(Units.get(i)[iii]))
                    {
                        Units.get(i)[iii] = -1;
                    }
                }
            }
        }
    }
    //Takes in an array and a size and then returns an array of that size, with empty spaces taken up by -1
    //Remember to call this with +1 size above what you need
    static int[] resizeArray(int [] array, int size) throws GameActionException
    {
        int[] old = array;
        int[] resized = new int[old.length + size];
        for(int i = 0; i<old.length; i++)
        {
            resized[i] = old[i];
        }
        for(int ii = old.length; ii<resized.length; ii++)
        {
            resized[ii] = -1;
        }
        return resized;
    }
    //Resizes an array based on a part of the ArrayList Units
    //Takes in a code, which is the unit array to be resized, and the size, which is the new size
    static void resizeFromList(int code, int size) throws GameActionException
    {
        for(int i = 0; i<Units.size(); i++)
        {
            if(Units.get(i)[0] == code)
            {
                Units.set(i,resizeArray(Units.get(i),size));
            }
        }
    }

    /** Encodes a message given the message type, x-cord, y-cord, and other information
     * see the flagSchema online
     * Data is validated
     * @param type 0-15, is the number signifying the type of action
     * @param locationX, 0-127, x-coordinate % 128
     * @param locationY 0-127, ycoordinate % 128
     * @param extrema  0-63, can encode any other information 0-63
     * @return returns a valid flag
     */
    static int encodeFlag(int type, int locationX,int locationY, int extrema){
        int[] shift= {schema[1]+schema[2]+schema[3],schema[2]+schema[3],schema[3]};//will store how much to shift first n-1 params
        int flag=((type<<shift[0])+(locationX<<shift[1])+(locationY<<shift[2])+extrema);
        if(rc.canSetFlag(flag))
            return flag;
        return 0;
    }
    static int encodeFlag(int type, MapLocation loc, int extrema){
        return encodeFlag(type, loc.x%128,loc.y%128,extrema);
    }
    static int encodeFlag(int type, int locationX, int locationY){
        return encodeFlag(type,locationX,locationY,0);
    }



    /**Decodes flag into array of size 4
     *
     * @param flag 24 bit integer of a flag to be decoded
     * @return returns the communication schema
     */
    static int[] decodeFlag(int flag){
        int[] info= {flag>>shift[0],(flag>>shift[1])%(1<<schema[1]),(flag>>shift[2])%(1<<schema[2]),flag%(1<<schema[3])};
        //Converts the bits into numbers brocken down by schema

        return info;
    }

    /** Takes in x and y coordinates % 128 (such as a flag intake) and then outputs absolute MapLocation
     *
     *
     */
    static MapLocation getMapLocation(int x, int y){
        MapLocation loc=rc.getLocation();
        int xDif=x - (loc.x%128);
        int yDif=y - (loc.y%128);

        if(xDif<64 && xDif>-64)
            loc=loc.translate(xDif,0);
        else
            loc=loc.translate((-128+xDif)%64,0);

        if(yDif<64 && yDif>-64)
            loc=loc.translate(0,yDif);
        else
            loc=loc.translate(0,(-128+yDif)%64);

        return loc;
    }
    //Finds the location of a given int within an array
    //Literally just indexOf but for an Array
    //If not found, returns -1
    static int indexOfArray(int[] array, int needle)
    {
        for(int i = 0; i<array.length; i++)
        {
            if(array[i] == needle)
            {
                return i;
            }
        }
        return -1;
    }
    /* Sets all the previous variables to current round settings.
    * To be used right before moving onto the next round*/
    static void setPrevious() throws GameActionException
    {
        previousInfluence = rc.getInfluence();
    }
    /* Builds units according to the ArrayList toBeConstructed

     */
    static MapLocation construct(RobotType typeToBuild, int influence) throws GameActionException
    {
        RobotType toBuild = typeToBuild;
        for (Direction dir : directions)
        {
            if (rc.canBuildRobot(toBuild, dir, influence))
            {
                rc.buildRobot(toBuild, dir, influence);
                return rc.adjacentLocation(dir);
                //Update list of built robots MUST BE RUN WHEN BUILDING A ROBOT
               /* switch (toBuild) {
                    case MUCKRAKER:
                        createdMuckrakersID.add(rc.senseRobotAtLocation(loc).getID());
                        break;
                    case SLANDERER:
                        createdSlanderersID.add(rc.senseRobotAtLocation(loc).getID());
                        break;
                    case POLITICIAN:
                        createdPoliticiansID.add(rc.senseRobotAtLocation(loc).getID());
                        break;*/
            }
        }
        return null;
    }
    /*Tells you if you can construct the unit on top of the stack*/

    static Boolean canConstruct(RobotType typeToBuild, int influence) throws GameActionException
    {
        RobotType toBuild = typeToBuild;
        for (Direction dir : directions)
        {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                return true;
            }
        }
        return false;
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
        } else return false;
    }
    
   
}
