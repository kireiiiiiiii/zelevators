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

import game.ElevatorController;
import game.Game;

/**
 * <h2> My Elevator Controller class </h2>
 * Class designed to controll the game elevator. Contains onEvent methods, that are called on events in the game accordingly. 
 * </p>
 * <h4> Algorithm system: </h4>
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

    /////////////////
    // Class variables
    ////////////////

    private Game game;

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
        this.game = game;
    }

    /**
     * Event: "outside-the-elevator" request, requesting an elevator. 
     * The event will be triggered with the request is created/enabled & when it is cleared (reqEnable indicates which). 
     * 
     * @param floorIdx
     * @param dir
     * @param reqEnable
     */
    public void onElevatorRequestChanged(int floorIdx, Direction dir, boolean reqEnable) {
        System.out.println("onElevatorRequestChanged(" + floorIdx + ", " + dir + ", " + reqEnable + ")");

        // TODO
        if (reqEnable) {
            gotoFloor(0, floorIdx);
            System.out.println("  --> gotoFloor(" + floorIdx + ")");
        }
    }

    /**
     * Event: "inside-the-elevator" request, requesting to go to a floor.
     * The event will be triggered with the request is created/enabled & when it is cleared (reqEnable indicates which). 
     * 
     * @param elevatorIdx
     * @param floorIdx
     * @param reqEnable
     */
    public void onFloorRequestChanged(int elevatorIdx, int floorIdx, boolean reqEnable) {
        System.out.println("onFloorRequesteChanged(" + elevatorIdx + ", " + floorIdx + ", " + reqEnable + ")");

        // TODO
        gotoFloor(0, floorIdx);
        System.out.println("  --> gotoFloor(" + floorIdx + ")");
    }

    /**
     * Event: Elevator has arrived at the floor & doors are open.
     * 
     * @param elevatorIdx
     * @param floorIdx
     * @param traverDirection
     */
    public void onElevatorArrivedAtFloor(int elevatorIdx, int floorIdx, Direction travelDirection) {
        System.out.println("onElevatorArrivedAtFloor(" + elevatorIdx + ", " + floorIdx + ", " + travelDirection + ")");

        // TODO
    }

    /**
     * Event: Called each frame of the simulation (i.e. called continuously)
     * 
     * @param deltaTime
     */
    public void onUpdate(double deltaTime) {
        if (game == null) {
            return;
        }

        // TODO
    }

    /////////////////
    // Private methods
    ////////////////

}
