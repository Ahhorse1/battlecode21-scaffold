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

    //Variables that store ID number of the created units for enlightenment centers
    //-Ben
    static ArrayList<Integer> createdPoliticiansID = new ArrayList<Integer>();
    static ArrayList<Integer> createdSlanderersID = new ArrayList<Integer>();
    static ArrayList<Integer> createdMuckrakersID = new ArrayList<Integer>();

    //Array version
    //Each array is +1 size larger than it's supposed to be, with max_int or min_int in the 0 position to signal whether...
    //...the array is supposed to be activated (max_int = yea, min_int = nay)
    // areas with ids have numbers, empty spaces hold -1
    static int[] createdPoliticians25 = new int[1];
    static int[] createdPoliticians11 = new int[1];
    static int[] createdSlanderers41 = new int[1];
    /*Stores what needs to be built
      They should be added in as influence*10 + type, where slanderer = 1, politician = 2, muckraker =3
      -Winston */
    static ArrayList<Integer> toBeConstructed = new ArrayList<Integer>();

    //Stores the previous round's number of created units and influence
    static int previousPolitician = 0;
    static int previousSlanderer = 0;
    static int previousMuckrakers = 0;
    static int previousInfluence = 0;

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

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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


    static void runEnlightenmentCenter() throws GameActionException
    {
        if(turnCount == 0)
        {
            createdPoliticians11[0] = Integer.MAX_VALUE;
            createdPoliticians11 = resizeArray(createdPoliticians11, 4);
            createdPoliticians25[0] = Integer.MAX_VALUE;
            createdPoliticians25 = resizeArray(createdPoliticians25, 2);
            createdSlanderers41[0] = Integer.MAX_VALUE;
            createdSlanderers41 = resizeArray(createdSlanderers41,4);
        }
        else if(turnCount == 30)
        {
            createdPoliticians11 = resizeArray(createdPoliticians11, 31);
            createdPoliticians25 = resizeArray(createdPoliticians25, 19);
            createdSlanderers41 = resizeArray(createdSlanderers41, 11);
        }
        else if(turnCount == 225)
        {
            createdPoliticians11 = resizeArray(createdPoliticians11, 101);
            createdPoliticians25 = resizeArray(createdPoliticians25, 51);
            createdSlanderers41 = resizeArray(createdSlanderers41, 26);
        }
        else if(turnCount == 625)
        {

        }
        else
        {
            runStageOne();
        }
    }
    /*
    Runs Stage One of the Strategy:
        3 Slanderer - 41 Influence (Total 123 Influence, 2 initially and 3rd is staggered)
        1 Politician - 25 Influence
        3 Politicians - 11 Influence
        Bidding 1 for vote
        - Winston */
    static void runStageOne() throws GameActionException {
        replace();
        if (rc.isReady())
        {
            buildUnits();
        }
    }
    /* static void runStageTwo() throws GameActionException {
         maintainForces();
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
     */

    //Builds units, starting with slanderers, then politicians25 then politicians11
     static void buildUnits() throws GameActionException
     {
         int x = indexOfArray(createdSlanderers41, -1);
         int y = indexOfArray(createdPoliticians25, -1);
         int z = indexOfArray(createdPoliticians11, -1);
         if(x != -1)
         {
             if(canConstruct(RobotType.SLANDERER, 41))
             {
                 MapLocation loc = construct(RobotType.SLANDERER, 41);
                 createdSlanderers41[x] = rc.senseRobotAtLocation(loc).getID();
             }
             return;
         }
         if(y != -1)
         {
             if(canConstruct(RobotType.POLITICIAN, 25))
             {
                 MapLocation loc = construct(RobotType.POLITICIAN, 25);
                 createdPoliticians25[y] = rc.senseRobotAtLocation(loc).getID();
             }
            return;
         }
         if(z != -1)
         {
             if(canConstruct(RobotType.POLITICIAN,11))
             {
                 MapLocation loc = construct(RobotType.POLITICIAN,11);
                 createdPoliticians11[z] = rc.senseRobotAtLocation(loc).getID();
             }
            return;
         }
     }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            //System.out.println("empowering...");
            rc.empower(actionRadius);
            //System.out.println("empowered");
            return;
        }
        tryMove(randomDirection());
        //if (tryMove(randomDirection()))
            //System.out.println("I moved!");
    }

    static void runSlanderer() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        RobotInfo[] neutral = rc.senseNearbyRobots(actionRadius, Team.NEUTRAL);
        if(turnCount>= 50)
        {
            rc.setFlag(1);
        }
        if ((attackable.length != 0 || neutral.length != 0) && rc.canEmpower(actionRadius)) {
            // System.out.println("empowering...");
            rc.empower(actionRadius);
            // System.out.println("empowered");
            return;
        }
        tryMove(randomDirection());
        // if (tryMove(randomDirection()))
        // System.out.println("I moved!");
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    //System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        tryMove(randomDirection());
            //System.out.println("I moved!");
    }
    static void replace() throws GameActionException
    {
        for(int i = 0; i<createdSlanderers41.length; i++)
        {
            if(!rc.canGetFlag(createdSlanderers41[i]))
            {
                createdSlanderers41[i] = -1;
            }
            else if(rc.getFlag(createdSlanderers41[i]) == 1)
            {
                createdSlanderers41[i] = -1;
            }
        }
        for(int ii = 0; ii<createdPoliticians25.length; ii++)
        {
            if(!rc.canGetFlag(createdPoliticians25[ii]))
            {
                createdPoliticians25[ii] = -1;
            }
        }
        for(int iii = 0; iii<createdPoliticians11.length; iii++)
        {
            if(!rc.canGetFlag(createdPoliticians11[iii]))
            {
                createdPoliticians11[iii] = -1;
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
        previousPolitician = getCntUnit(RobotType.POLITICIAN)[0];
        previousMuckrakers = getCntUnit(RobotType.SLANDERER)[0];
        previousSlanderer = getCntUnit(RobotType.MUCKRAKER)[0];
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
                for (int id : createdPoliticians11)
                    if (rc.canGetFlag(id))//canGetFlag is used as a proxy for whether it exists
                    {
                        cnt++;
                    }
                for(int id2 : createdPoliticians25)
                    if(rc.canGetFlag(id2))
                    {
                        cnt++;
                    }
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
        //System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
