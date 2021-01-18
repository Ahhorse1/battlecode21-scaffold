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

    //Arraylist of arrays
    //Each array is +1 size larger than it's supposed to be, with the unit code in the 0 position
    //The unit code being influence *10 + (1/2/3) for (politicians/slanderers/muckrakers)
    static ArrayList<int[]> Units = new ArrayList<int[]>();

    /*Stores what needs to be built
      They should be added in as influence*10 + type, where slanderer = 1, politician = 2, muckraker =3
      -Winston */
    static ArrayList<Integer> toBeConstructed = new ArrayList<Integer>();

    //Stores the previous round's influence
    static int previousInfluence = 0;

    //Indicates the mode for building armies
    //Each mode lasts for 15 actions
    //1 = muckrakers, 2 = politicians, 3 = bidding
    static int[] modeCount = new int[2];
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
        /*3 Slanderer - 41 Influence (412)
        3 Politician - 20 Influence (201)
        4 Corner Runners Politicians - 1 Influence (11) */
        if(turnCount == 0)
        {
            int[] slanderers41 = {412};
            int[] politicians20 = {201};
            int[] politicians1 = {11};

            Units.add(resizeArray(politicians1, 5));
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
            int[] slanderers81 = {852};
            int[] muckrakers10 = {103};

            resizeFromList(201,19);

            Units.add(resizeArray(muckrakers10, 11));
            Units.add(resizeArray(slanderers81, 6));
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
            int[] slanderers230 = {2302};
            int[] politicians80 = {810};
            int[] muckrakers50 = {503};

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

            Units.add(resizeArray(muckrakers50, 11));
            Units.add(resizeArray(politicians80, 11));
            Units.add(resizeArray(slanderers230, 6));

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
            int[] muckrakers100 = {1003};
            int[] politicians400 = {4001};
            int[] politicians100 = {1001};

            Units.add(resizeArray(muckrakers100, 11));
            Units.add(resizeArray(politicians100, 30));
            Units.add(resizeArray(politicians400, 21));
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
    /*
    Runs Stage One of the Strategy:
        3 Slanderer - 41 Influence (Total 123 Influence, 2 initially and 3rd is staggered)
        1 Politician - 25 Influence
        3 Politicians - 11 Influence
        Bidding 1 for vote
        - Winston */
    static void runStageOne() throws GameActionException {
        replace();
        Boolean stop = true;
        if (rc.isReady())
        {
            stop = buildUnits();
        }
        if(!stop)
        {
            int previousIncome = rc.getInfluence() - previousInfluence;
            if(rc.canBid((int)(previousIncome*1.5)))
            {
                rc.bid((int)(previousIncome*1.5));
            }
        }
        setPrevious();
    }
     static void runStageTwo() throws GameActionException
     {
         replace();
         Boolean stop = true;
         if(rc.isReady())
         {
             stop = buildUnits();
         }
         if(!stop)
         {
            if(modeCount[0] == 1)
            {
                if(canConstruct(RobotType.MUCKRAKER, 100))
                {
                    construct(RobotType.MUCKRAKER, 100);
                    modeCount[1]++;
                }
                if(modeCount[1] >= 15)
                {
                    modeCount[0] = 2;
                    modeCount[1] = 0;
                }
            }
            else if(modeCount[0] == 2)
            {
                if(canConstruct(RobotType.POLITICIAN, 100))
                {
                    construct(RobotType.POLITICIAN, 100);
                    modeCount[1]++;
                }
                if(modeCount[1] >= 15)
                {
                    modeCount[0] = 3;
                    modeCount[1] = 0;
                }
            }
            else if(modeCount[0] == 3)
            {
                if(rc.canBid(100))
                {
                    rc.bid(100);
                    modeCount[1]++;
                }
                if(modeCount[1] >= 15)
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
                 if(y == 1)
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
                 System.out.println("trying to build: " + toBuild + toBuildInfluence);
                 if(canConstruct(toBuild, toBuildInfluence))
                 {
                     System.out.println("successfully built: " + toBuild);
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
         return false;
     }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            rc.empower(actionRadius);
            return;
        }
        tryMove(randomDirection());
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
            rc.empower(actionRadius);
            return;
        }
        tryMove(randomDirection());
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        tryMove(randomDirection());
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
                    if(!rc.canGetFlag(Units.get(i)[ii]))
                    {
                        Units.get(i)[ii] = -1;
                    }
                    else if(rc.getFlag(Units.get(i)[ii]) == 1)
                    {
                        Units.get(i)[ii] = -1;
                    }
                }
            }
            else
            {
                for(int iii = 1; iii<Units.get(i).length; iii++)
                {
                    if(!rc.canGetFlag(Units.get(i)[iii]))
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
    /**
     *To be run as an enlightenment center
     * Also updates slanderers to politicians in the local list
     * Returns integer array of first the cnt, and then the direction of the first slanderer detected
     * @param type The type of robot to be counted
     *-Ben

    **NOW OBSOLETE, SCHEDULED FOR DELETION**
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

    } */

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
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
