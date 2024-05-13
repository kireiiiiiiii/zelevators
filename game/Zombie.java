package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Zombie extends GameObject {
    // Consts...
    public static final Vec2 NOMINAL_HALFDIMS = new Vec2(0.3, 0.48);
    public static final double VERTICAL_OFFSET = -0.035;
    public static final double WALK_SPEED = 1.0;            // In units per second
    public static final double STARVATION_TIME = 40;

    // Enums...
    protected enum State {
        Entering,
        Waiting,
        Boarding,
        OnElevator,
        WalkingOff,
        Delivered,
        Starved,
    }

    // Private member variables...
    private State state = State.Entering;
    private int currentFloor = -1;
    private int targetFloor = -1;
    private int onElevatorIdx = -1;
    private double waitAreaOffset = World.ZOMBIE_WAITING_AREA_MAX_OFFSET;
    private double walkCycleAnimT = 0;
    private double walkSpeedScalar = 1.0;
    private double timeSinceStarved = 0.0;
    private BufferedImage image = null;
    private Vec2 halfDims = null;

    // Constructors...
    protected Zombie(int playerIdx, int initialFloor, int targetFloor) {
        // Super...
        super(playerIdx);

        // Save off our params...
        this.playerIdx = playerIdx;
        state = State.Entering;
        currentFloor = initialFloor;
        this.targetFloor = targetFloor;

        // Init (remove random in 2 player mode)...
        halfDims = Vec2.multiply(NOMINAL_HALFDIMS, Util.randRange(0.9, 1.0));
        pos = new Vec2(halfDims.x * ((Game.get().getPlayerCount() > 1) ? 1.0 : Util.randRange(0.75, 1.25)), World.FLOOR_HEIGHT * currentFloor + halfDims.y + VERTICAL_OFFSET);
        waitAreaOffset = World.ZOMBIE_WAITING_AREA_MAX_OFFSET * ((Game.get().getPlayerCount() > 1) ? 1.0 : Util.randRange(0.75, 1.25));
        walkSpeedScalar = (Game.get().getPlayerCount() > 1) ? 1.0 : Util.randRange(0.9, 1.1); 

        // Texture...
        try {
            int textureIndex = Util.randRangeInt(0, 5);
            image = ImageIO.read(new File("textures/zombie_0" + textureIndex + ".png"));
        } 
        catch (IOException e) {
        }
    }

    // Static helpers...
    protected static int calcRandomTargetFloor(int currentFloor) {
        int targetFloor = Util.randRangeInt(0, Game.get().getFloorCount() - 2);
        targetFloor = (targetFloor < currentFloor) ? targetFloor : (targetFloor + 1);
        return targetFloor;
    }

    // Accessors...
    protected State getState() {
        return state;
    }
    protected int getCurrentFloor() {
        return currentFloor;
    }
    protected int getTargetFloor() {
        return targetFloor;
    }
    protected boolean isGettingOnElevator(Elevator elevator) {
        return elevator.canAcceptNewZombiePassenger(this) && ((state == State.Waiting) || (state == State.Boarding));
    }
    protected boolean isOnAnElevator() {
        return (state == State.OnElevator);
    }
    protected boolean isOnElevator(int elevatorIdx) {
        return (state == State.OnElevator) && (onElevatorIdx == elevatorIdx);
    }

    // Update...
    protected void update(double deltaTime) {
        // Super...
        super.update(deltaTime);

        // Setup...
        boolean isWalking = false;
        double walkAnimSpeed = (walkCycleAnimT < 0.5) ? 0.6 : 1.0;

        // Per state update...
        switch (state) {
            case Entering : {
                // Walk to the elevator...
                double walkSpeed = (timeSinceBorn < 0.5) ? 0 : (WALK_SPEED * walkAnimSpeed * walkSpeedScalar);
                pos.x = Math.min(pos.x + walkSpeed * deltaTime, World.ELEVATOR_LEFT_RIGHT_SPACE + waitAreaOffset);
                if (pos.x >= World.ELEVATOR_LEFT_RIGHT_SPACE + waitAreaOffset) {
                    state = State.Waiting;
                    Simulation.get(playerIdx).addElevatorRequest(currentFloor, (targetFloor > currentFloor) ? ElevatorController.Direction.Up : ElevatorController.Direction.Down);
                }
                isWalking = true;
            } break;
            case Waiting : {
                // Check for an open elevator...
                ArrayList<Elevator> elevators = Simulation.get(playerIdx).getElevators();
                for (int i = 0; i < elevators.size(); i++) {
                    Elevator elevator = elevators.get(i);
                    if (elevator.canAcceptNewZombiePassenger(this)) {
                        // Looks good, make a run for it...
                        state = State.Boarding;
                    }
                }

                // Make sure our button is pressed...
                if ((state == State.Waiting) &&
                    !Simulation.get(playerIdx).hasElevatorRequest((int)currentFloor, (targetFloor > currentFloor) ? ElevatorController.Direction.Up : ElevatorController.Direction.Down)) {
                    // Press it again...
                    Simulation.get(playerIdx).addElevatorRequest(currentFloor, (targetFloor > currentFloor) ? ElevatorController.Direction.Up : ElevatorController.Direction.Down);
                }

                // Check to see if we've starved (can in waiting)
                if (timeSinceBorn >= STARVATION_TIME) {
                    // Zombie: Starved!
                    Game.get().awardPoints(playerIdx, Game.POINTS_ZOMBIE_STARVED);
                    timeSinceStarved = 0.0;
                    state = State.Starved;
                }
            } break;
            case Boarding : {
                // Find the target elevator (note that we might no longer have one)...
                ArrayList<Elevator> elevators = Simulation.get(playerIdx).getElevators();
                Elevator trgElevator = null;
                for (int i = 0; i < elevators.size(); i++) {
                    Elevator elevator = elevators.get(i);
                    if (elevator.canAcceptNewZombiePassenger(this)) {
                        trgElevator = elevator;
                        break;
                    }
                }

                // Walk to the elevator (if no longer have an option, walk back)...
                if (trgElevator != null) {
                    double trgPosX = trgElevator.getPos().x;
                    if (trgPosX > pos.x) {
                        pos.x = Math.min(pos.x + WALK_SPEED * walkAnimSpeed * walkSpeedScalar * deltaTime, trgPosX);
                        isWalking = true;
                    }
                    else {
                        pos.x = Math.max(pos.x - WALK_SPEED * walkAnimSpeed * walkSpeedScalar * deltaTime, trgPosX);
                        isWalking = true;
                    }
                    if (pos.x == trgPosX) {
                        // Pop, we're on...
                        state = State.OnElevator;
                        onElevatorIdx = trgElevator.getIndex();

                        // Request our desired floor...
                        trgElevator.requestFloor(targetFloor);
                    }
                }
                else {
                    // Walk back (sad)...
                    double trgPosX = World.ELEVATOR_LEFT_RIGHT_SPACE + waitAreaOffset;
                    pos.x = Math.max(pos.x - WALK_SPEED * walkAnimSpeed * walkSpeedScalar * deltaTime, trgPosX);
                    if (pos.x <= trgPosX) {
                        state = State.Waiting;
                    }
                    isWalking = true;
                }
            } break;
            case OnElevator : {
                // Wait till we're at our target floor and the doors are open...
                Elevator elevator = Simulation.get(playerIdx).getElevators().get(onElevatorIdx);
                if ((elevator.getCurrentFloor() == (double)targetFloor) && elevator.getAreDoorsOpen()) {
                    // We're here, get of...
                    state = State.WalkingOff;
                    onElevatorIdx = -1;
                }

                // Keep us locked to the elevator floor...
                currentFloor = (int)elevator.getCurrentFloor();
                pos.y = World.FLOOR_HEIGHT * elevator.getCurrentFloor() + halfDims.y + VERTICAL_OFFSET;
            } break;
            case WalkingOff : {
                pos.x = pos.x + WALK_SPEED * deltaTime;
                double posPixelsX = Util.toPixelsX(playerIdx, pos.x);
                if (posPixelsX >= (World.get(playerIdx).getOrigin().x + World.get(playerIdx).getCanvasSize().x)) {
                    // Zombie: Delivered!
                    Game.get().awardPoints(playerIdx, Game.POINTS_ZOMBIE_DELIVERED);
                    this.timeTillDeath = Math.max(this.timeTillDeath, 0.0001);
                    state = State.Delivered;
                }
                isWalking = true;

                // Check to see if we've starved (can in waiting)
                if ((state == State.WalkingOff) && (timeSinceBorn >= STARVATION_TIME)) {
                    // Zombie: Starved!
                    Game.get().awardPoints(playerIdx, Game.POINTS_ZOMBIE_STARVED);
                    timeSinceStarved = 0.0;
                    state = State.Starved;
                }
            } break;
            case Delivered : {
                // Nothing to do here but wait...
            } break;
            case Starved : {
                // Timer (keep out the starved for a while)...
                timeSinceStarved += deltaTime;

                // Make sure we're dying, then just wait for the cold embrace of death...
                if (timeSinceStarved >= 10) {
                    this.timeTillDeath = Math.max(this.timeTillDeath, 0.0001);
                }
            } break;
        }

        // Update walkCycleAnimT...
        if (isWalking) {
            walkCycleAnimT += deltaTime * 2.0;
            if (walkCycleAnimT >= 1.0) {
                walkCycleAnimT -= 1.0;
            }
        }
        else {
            // Go back to zero...
            if (walkCycleAnimT != 0.0) {
                walkCycleAnimT += deltaTime * 4.0;
                if (walkCycleAnimT >= 1.0) {
                    walkCycleAnimT = 0.0;
                }
            }
        }
    }

    // Draw...
    protected void drawShadow(Graphics2D g) {
    }
    protected void draw(Graphics2D g) {
        // If we are in an elevator & the doors are closed, don't render us...
        if ((state == State.OnElevator) && (Simulation.get(playerIdx).getElevator(onElevatorIdx).getAreDoorsClosed())) {
            return;
        }

        // Draw the image...
        if (image != null) {
            double rotDegMidPt = 0.7;
            double rotDegT = (walkCycleAnimT < rotDegMidPt) ? (walkCycleAnimT / rotDegMidPt) : ((1.0 - (walkCycleAnimT - rotDegMidPt) / (1 - rotDegMidPt)) * rotDegMidPt);
            double starveAnimT = Math.min(timeSinceStarved * 3.0, 1);
            double rotDeg = Util.lerp(rotDegT * -10, -90, starveAnimT);
            double scale = calcDrawScale();
            Vec2 posDraw = Vec2.subtract(Vec2.subtract(pos, Vec2.multiply(new Vec2(0, halfDims.y), 1 - scale)), Vec2.multiply(new Vec2(halfDims.x * 1.25, halfDims.y * 2.05), starveAnimT * scale));
            Draw.drawImageRotated(playerIdx, g, image, posDraw, halfDims, rotDeg, scale);
        }
    }
}
