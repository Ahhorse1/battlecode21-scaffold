package BenPlayer;

import battlecode.common.*;
import com.sun.xml.internal.xsom.util.DeferedCollection;

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

    static int turnCount=0;

    //Variables that store ID number of the created units for enlightenment centers
    static ArrayList<Integer> createdPoliticiansID;
    static ArrayList<Integer> createdSlanderersID;
    static ArrayList<Integer> createdMuckrakersID;

    //Store ID of the enlightenment center that created you
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
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
                MapLocation loc= rc.adjacentLocation(dir);

                //Update list of built robots MUST BE RUN WHEN BUILDING A ROBOT
                switch(toBuild){
                    case MUCKRAKER:
                        createdMuckrakersID.add(rc.senseRobotAtLocation(loc).getID());
                        break;
                    case SLANDERER:
                        createdSlanderersID.add(rc.senseRobotAtLocation(loc).getID());
                        break;
                    case POLITICIAN:
                        createdPoliticiansID.add(rc.senseRobotAtLocation(loc).getID());
                        break;
                }
            } else {
                break;
            }
        }

        //set the flag of the enlightenment center by encoding politician cnt, slanderer cnt, muckraker cnt
        int polCnt=getCntUnit(RobotType.POLITICIAN)[0];
        int[] slanCnt=getCntUnit(RobotType.SLANDERER); //the second value of array is the direction of where to go
        int mucCnt=getCntUnit(RobotType.MUCKRAKER)[0];
        rc.setFlag(getEnlFlag(polCnt,slanCnt[0],mucCnt, slanCnt[1]));




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
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
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

        if(turnCount==1){

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
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
    *To be run as an enlightenment center
     * Also updates slanderers to politicians in the local list
     * Returns integer array of first the cnt, and then the direction of the first slanderer detected
     * @param type The type of robot to be counted
     *
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
     *
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
}
