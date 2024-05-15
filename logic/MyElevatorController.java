/*
 * Author: Matěj Šťastný
 * Date created: 5/13/2024
 * Github link: repository not public
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package logic;

import java.time.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import game.ElevatorController;
import game.Game;

/**
 * <h2>My Elevator Controller class</h2>
 * Class designed to controll the game elevator. Contains onEvent methods, that
 * are called on events in the game accordingly.
 * </p>
 * <h4>Algorithm system:</h4>
 * </p>
 * TODO: Algorithm definiton
 *
 */
public class MyElevatorController implements ElevatorController {

    /////////////////
    // Constants
    ////////////////

    private final String PLAYER_NAME = "Matěj Šťastný";
    private final int PLAYER_PERIOD = 6;
    private final int GAME_LENGHT = 120;
    private final double ELEVATOR_SPEED = 0.5; // How many seconds per floor
    private final double ZOMBIE_BOARD_TIME = 2.8; // How many seconds from the eleator arrival to zombie in
    private final double ZOMBIE_UNBOARD_TIME = 1.5;
    private final String COLOR_RESET = "\u001B[0m";
    private final String COLOR_ELEVATOR_MOVE = "\u001B[35m";
    private final String COLOR_CALL_ELEVATOR = "\u001B[33m";
    private final String COLOR_QUE_FLOOR = "\u001B[32m";
    private final String COLOR_IMPORTANT = "\u001B[41m";
    private final String COLOR_IMPORTANT_2 = "\u001B[44m";

    /////////////////
    // Class variables
    ////////////////

    private Game game;
    private boolean toDestination;
    private boolean isFirstReq;
    private boolean areEmpty;
    private ArrayList<ElevatorRequest> waitList;
    private ArrayList<ElevatorRequest> gotoList;
    private ScheduledExecutorService scheduler;
    private int elevatorTargetFloor;

    /////////////////
    // Accesor methods
    ////////////////

    /**
     * Returns the player name.
     *
     * @return {@code String} of the player name.
     */
    public String getStudentName() {
        return this.PLAYER_NAME;
    }

    /**
     * Returns the player period number.
     *
     * @return {@code int} of the player period number.
     */
    public int getStudentPeriod() {
        return this.PLAYER_PERIOD;
    }

    /////////////////
    // OnEvent methods
    ////////////////

    /**
     * Method executed in the start of the game.
     *
     * @param game - {@code Game} object of the game.
     */
    public void onGameStarted(Game game) {
        System.out.println(this.COLOR_IMPORTANT + timeStamp() + "GAME START" + this.COLOR_RESET);
        this.game = game;
        this.areEmpty = false;
        this.isFirstReq = true;
        this.waitList = new ArrayList<ElevatorRequest>();
        this.gotoList = new ArrayList<ElevatorRequest>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            System.out.println(this.COLOR_IMPORTANT_2 + timeStamp() + "GAME END" + this.COLOR_RESET);
        }, this.GAME_LENGHT, TimeUnit.SECONDS);
    }

    /**
     * On event method called automaticly by the game, when specified event appeares
     * </p>
     * <h4>Event:</h4>
     * "outside-the-elevator" request, meaning zombie requesting an elevator from
     * it's floor.
     * </p>
     * The event will be triggered with the request is created/enabled and also when
     * it is
     * cleared (reqEnable indicates which).
     *
     * @param floorIdx  - index of the floor, where the zombie is calling the
     *                  elevator from.
     * @param dir       - direction that the elevator must go from it's position, to
     *                  reach the zombies floor.
     * @param reqEnable - if the request was created, or cleared.
     */
    public void onElevatorRequestChanged(int floorIdx, Direction dir, boolean reqEnable) {
        // Add the request to the zombie wait list, if it's a new request
        if (reqEnable) {
            System.out.println(this.COLOR_CALL_ELEVATOR + timeStamp() + "newElevatorCall(" + floorIdx + ")" + " Sum: "
                    + this.waitList.size() + this.COLOR_RESET);
            this.waitList.add(new ElevatorRequest(floorIdx, LocalTime.now()));
        }

        // If it's the first request, start the elevator move sequence
        if (this.isFirstReq) {
            this.isFirstReq = false;
            this.toDestination = false;
            startElevatorMoveSequence();
        }
    }

    /**
     * On event method called automaticly by the game, when specified event appeares
     * </p>
     * <h4>Event:</h4>
     * "inside-the-elevator" request, meaning a zombie in that entered the elevator
     * is requesting a floor it want's to go.
     * </p>
     * The event will be triggered with the request is created/enabled and also when
     * it is
     * cleared (reqEnable indicates which).
     *
     * @param elevatorIdx - index of the elevator, normally 0.
     * @param floorIdx    - index of the floor, that the zombie wan't to go to.
     * @param reqEnable   - if the request was created, or cleared.
     */
    public void onFloorRequestChanged(int elevatorIdx, int floorIdx, boolean reqEnable) {
        System.out.println(
                this.COLOR_QUE_FLOOR + timeStamp() + "floorRequestQued(" + elevatorIdx + ", " + floorIdx + ", "
                        + reqEnable + ")" + " Sum: " + this.gotoList.size() + this.COLOR_RESET);

        // Add the request to the que list, if new request
        if (reqEnable) {
            this.gotoList.add(new ElevatorRequest(floorIdx, LocalTime.now()));
        }
    }

    /**
     * On event method called automaticly by the game, when specified event appeares
     * </p>
     * <h4>Event:</h4>
     * The elevator arrived at a floor and opened it's door.
     * </p>
     * !The medhod doesn't work properly!
     *
     * @param elevatorIdx     - index of the elevator, normally 0.
     * @param floorIdx        - index of the floor, that the elevator arrived at.
     * @param traverDirection - I have no clue what this is :(
     */
    public void onElevatorArrivedAtFloor(int elevatorIdx, int floorIdx, Direction travelDirection) {
        // ! Method doesn't work!
    }

    /**
     * On event method called automaticly by the game, when specified event appeares
     * </p>
     * <h4>Event:</h4>
     * Called each frame of the simulation.
     *
     * @param deltaTime - delta time.
     */
    public void onUpdate(double deltaTime) {
        if (game == null) {
            return;
        }
        if (this.gotoList.size() + this.waitList.size() <= 0 && !this.isFirstReq) {
            if (!this.areEmpty) {
                System.out.println(this.COLOR_IMPORTANT + timeStamp() + "EMPTY ARRAYS" + this.COLOR_RESET);
                areEmpty = true;
            }
        } else if (areEmpty) {
            System.out.println(this.COLOR_IMPORTANT + timeStamp() + "MoveSequence Restored" + this.COLOR_RESET);
            areEmpty = false;
            resumeElevatorMoveSequence(this.elevatorTargetFloor);
        }
    }

    /////////////////
    // Private methods
    ////////////////

    /**
     * Algorythm to determine if the elevator should go for a zombie waiting for an
     * elevator, or deliver a zombie in the elevator.
     *
     * @return {@code boolean} value of "delivering zombie", meaning {@code true} if
     *         the next move should be delivering a zombie to it's destination, or
     *         {@code false} if the next move should be picking up a zombie waiting
     *         for an elevator.
     */
    private boolean getRequestMode() {

        // Assume, that the at least one of the arrays is not empty
        assert (this.gotoList.size() > 0 || this.waitList.size() > 0) : "Arraylists empty";
        boolean bol;

        if (this.toDestination) {
            // Previously going to destination, and wait list isn't empty
            if (waitList.size() > 0) {
                bol = false;
            }

            // Previously going to destination, but waitlist empty
            else {
                bol = true;
            }
        }

        else {
            // Previously going for new zombie on waitlist, and the gotoList isn't empty
            if (this.gotoList.size() > 0) {
                bol = true;
            }

            // Previously goig for new zombie, but the gotoList empty
            else {
                bol = false;
            }
        }

        return bol;
    }

    /**
     * ! Index 0 -> gotoList
     * ! Index 1 -> waitList
     * 
     * @return
     */
    private int[] getBestReqIndex() {
        if (this.waitList.size() < 1 && this.gotoList.size() < 1) {
            return null;
        }

        LocalTime bestTime;
        int[] bestIndex = new int[2];
        // If waitlist is empty take first from goto
        if (this.waitList.size() == 0) {
            bestIndex[0] = 0;
            bestIndex[1] = 0;
            bestTime = gotoList.get(0).getTime();
        } else {
            bestIndex[0] = 1;
            bestIndex[1] = 0;
            bestTime = waitList.get(0).getTime();
        }

        for (int i = 1; i < this.waitList.size(); i++) {
            if (bestTime.compareTo(waitList.get(i).getTime()) >= 0) {
                bestTime = waitList.get(i).getTime();
                bestIndex[0] = 1;
                bestIndex[1] = i;
            }
        }

        for (int i = 0; i < this.gotoList.size(); i++) {
            if (bestTime.compareTo(gotoList.get(i).getTime()) >= 0) {
                bestTime = gotoList.get(i).getTime();
                bestIndex[0] = 0;
                bestIndex[1] = i;
            }
        }

        return bestIndex;
    }

    @SuppressWarnings("unused")
    private int[] getNextIndex() {
        if (this.waitList.size() < 1 && this.gotoList.size() < 1) {
            return null;
        }

        int[] next = new int[2];
        this.toDestination = getRequestMode();
        if (this.toDestination) {
            next[0] = 0;
            next[1] = 0;
        } else {
            next[0] = 1;
            next[1] = 0;
        }
        return next;
    }

    /**
     * Starts the recursive method for moving the elevator.
     *
     */
    private void startElevatorMoveSequence() {
        moveToNext(true, 0);
    }

    /**
     * Starts the recursive method, but gives it a floor.
     * 
     * @param floor - floor, that the elevator is on.
     */
    private void resumeElevatorMoveSequence(int floor) {
        moveToNext(true, floor);
    }

    /**
     * This is a recursive method, with a time delay of when it's called.
     * </p>
     * This method will first remove the data of the floor the elevator arrived in
     * (if there are zombies on the 1st floor, and the elevator is on 1st floor,
     * than all the {@code elevatorRequest} for this floor will be cleared), than
     * moves the eleator to it's next position using reursion.
     *
     * @param init     - tells the method, if it should delete it's preious
     *                 destination data.
     * @param currFoor - current floor of the elevator.
     */
    private void moveToNext(boolean init, int currFoor) {

        // Remove previous destination data
        if (!init) {
            for (int i = 0; i < this.gotoList.size(); i++) {
                ElevatorRequest e = this.gotoList.get(i);
                if (e.getFloorNum() == currFoor) {
                    gotoList.remove(i);
                    i--;
                }
            }
            for (int i = 0; i < this.waitList.size(); i++) {
                ElevatorRequest e = this.waitList.get(i);
                if (e.getFloorNum() == currFoor) {
                    waitList.remove(i);
                    i--;
                }
            }
        }

        String mode = this.toDestination ? "Delivering" : "Picking up";
        int targetFloor;

        // Get next array request index
        int[] next = getBestReqIndex();

        if (next == null) {
            assert (false) : "Arrays are empty";
        }
        if (next[0] == 0) {
            targetFloor = gotoList.get(next[1]).getFloorNum();
            gotoList.get(next[1]).runRequest();
        } else {
            targetFloor = waitList.get(next[1]).getFloorNum();
            waitList.get(next[1]).runRequest();
        }

        // Print log
        System.out.println(this.COLOR_ELEVATOR_MOVE + timeStamp() + mode + "(From: " + currFoor + " To: " + targetFloor +  ")" + this.COLOR_RESET);

        // Save the target floor
        this.elevatorTargetFloor = targetFloor;

        // Counts the time it will take the eleator to travel in miliseconds
        int elevDelay = (int) (getElevatorTravelTime(currFoor, targetFloor) * 1000);
        // Creates a runnable, that should be excecuted after the elevator arrives
        Runnable onArrival = () -> {
            moveToNext(false, targetFloor);
        };
        // Schedules the onArrival code after the travel time of the elevator, and the
        // boarding time
        scheduler.schedule(onArrival, elevDelay + (int) (getDelay(targetFloor) * 1000),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the time stamp {@code String}. to append before an event log.
     *
     * @return {@code String} of a log time stamp. It's spaced and with a dividor.
     */
    private String timeStamp() {
        LocalTime time = LocalTime.now();
        return "> " + time.toString() + " | ";
    }

    /**
     * Determines the elevator waiting time, depending on the class variable.
     *
     * @return {@code double} of the time in seconds.
     */
    private double getDelay(int onFloor) {
        int numWaiting = 0;
        for (ElevatorRequest e : this.waitList) {
            if (e.getFloorNum() == onFloor) {
                numWaiting++;
            }
        }

        double delay;

        if (numWaiting == 0) {
            delay = this.ZOMBIE_UNBOARD_TIME;
        } else {
            numWaiting = 1; // TODO DANGER
            delay = this.ZOMBIE_BOARD_TIME * numWaiting;
        }

        return delay;
    }

    /**
     * Gets the time it will take the elevator to travel from a floor to another.
     *
     * @param currFloor   - current floor of the elevator.
     * @param targetFloor - target floor of the elevator.
     * @return {@code double} of the time in seconds.
     */
    private double getElevatorTravelTime(int currFloor, int targetFloor) {
        int floors = Math.abs(targetFloor - currFloor);
        return (double) (floors * this.ELEVATOR_SPEED);
    }

    /////////////////
    // Nested classes
    ////////////////

    /**
     * Class containing the data of an elevator request.
     *
     */
    @SuppressWarnings("unused")
    private final class ElevatorRequest {

        private final int ELEVATOR_INDEX = 0;

        private int floorNum;
        private LocalTime time;

        public ElevatorRequest(int floorNum, LocalTime time) {
            this.floorNum = floorNum;
            this.time = time;
        }

        public void runRequest() {
            boolean b = gotoFloor(this.ELEVATOR_INDEX, this.floorNum);
            // System.out.println(COLOR_ELEVATOR_RUN + timeStamp() + "Going to(" + this.floorNum + ")" + COLOR_RESET);
        }

        public int getFloorNum() {
            return this.floorNum;
        }

        public LocalTime getTime() {
            return this.time;
        }

        @Override
        public String toString() {
            return "FNum: " + this.floorNum;
        }
    }

}
