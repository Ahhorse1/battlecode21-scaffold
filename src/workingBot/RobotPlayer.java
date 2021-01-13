
package workingBot;
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

    /*Stores what needs to be built
      They should be added in as influence*10 + type, where slanderer = 1, politician = 2, muckraker =3
      -Winston */
    static ArrayList<Integer> toBeConstructed = new ArrayList<Integer>();

    //Stores the previous round's number of created units and influence
    static int previousPolitician;
    static int previousSlanderer;
    static int previousMuckrakers;
    static int previousInfluence;

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

   
    static void runEnlightenmentCenter() throws GameActionException {
        if(turnCount == 1)
        {
            int[] toAddStageOne = {411,411,202,202,202,411,202};
            for(Integer I : toAddStageOne)
            {
                toBeConstructed.add(I);
            }
            setPrevious();
        }
        else if(turnCount<30)
        {
            runStageOne();
        }
        else if(turnCount == 30)
        {
            System.out.println("STAGE TWO COMMENCES");
            for(int stageTwoS = 0; stageTwoS<10; stageTwoS ++)
            {
                System.out.println("STAGE TWO WORKS");
                toBeConstructed.add(411);
            }
            for(int stageTwoM = 0; stageTwoM <48; stageTwoM++)
            {
                toBeConstructed.add(202);
            }
            setPrevious();
        }
        else if(turnCount == 225)
        {
            System.out.println("STAGE THREE COMMENCES");
            for(int stageThreeS = 0; stageThreeS<12; stageThreeS++)
            {
                toBeConstructed.add(411);
            }
            for(int stageThreeM = 0; stageThreeM < 102; stageThreeM++)
            {
                toBeConstructed.add(202);
            }
            setPrevious();
        }
        else if(turnCount == 625)
        {
            System.out.println("STAGE FOUR COMMENCES");
            for(int stageFourS = 0; stageFourS <75; stageFourS++)
            {
                toBeConstructed.add(411);
            }
            for(int stageFourM = 0; stageFourM < 150; stageFourM++)
            {
                toBeConstructed.add(202);
            }
        }
        else
        {
            runStageTwo();
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
        //if (tryMove(randomDirection()))
            //System.out.println("I moved!");
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
