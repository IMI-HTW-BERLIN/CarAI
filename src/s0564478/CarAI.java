package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.Random;

public class CarAI extends AI {
    private Point checkpoint;

    private float goalRadius = 1;
    private float decelerateRadius = 30;
    private float throttleTime = 2f;

    // In Degree
    private float goalAngle = 1;
    private float decelerateAngle = 90;
    private float steerTime = 1f;


    public CarAI(Info info) {
        super(info);
        Random random = new Random();
        goalRadius = random.nextFloat() * 10;
        decelerateRadius = goalRadius + random.nextFloat() * 50;
        throttleTime = 0.1f + random.nextFloat() * 5;

        goalAngle = random.nextFloat() * 10;
        decelerateAngle = goalRadius + random.nextFloat() * 135;
        steerTime = 0.1f + random.nextFloat() * 5;

        try {
            File values = new File(System.getProperty("user.dir") + "/values.txt");
            String data = this.getClass().getSimpleName() + ":  " + String.format("%.1f | %.1f | %.1f | %.1f | %.1f | %.1f", goalRadius, decelerateRadius, throttleTime, goalAngle, decelerateAngle, steerTime) + "\n";
            Files.write(values.toPath(), data.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        enlistForTournament(564478, 562886);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        checkpoint = info.getCurrentCheckpoint();

        float steering = getSteering();
        return new DriverAction(getThrottle(), steering);
    }

    @Override
    public String getTextureResourceName() {
        return "/s0564478/art/car.png";
    }

    private float getThrottle() {
        double distance = checkpoint.distance(info.getX(), info.getY());

        if (distance < goalRadius)
            return 0;
        else if (distance < decelerateRadius) {
            double newSpeed = (distance / decelerateRadius) * info.getMaxAbsoluteAcceleration();
            double speedDiff = newSpeed - info.getVelocity().length();
            return (float) (speedDiff / throttleTime);
        } else
            return info.getMaxAbsoluteAcceleration();
    }

    private float getSteering() {
        Vector2f checkpointDirection = new Vector2f((float) checkpoint.getX() - info.getX(), (float) checkpoint.getY() - info.getY());
        double carDirectionRadiant = info.getOrientation();
        Vector2f carDirection = new Vector2f((float) Math.cos(carDirectionRadiant), (float) Math.sin(carDirectionRadiant));
        double diffAngle = Vector2f.angle(carDirection, checkpointDirection);
        diffAngle = Math.toDegrees(diffAngle);

        boolean angleIsNegative = angleIsNegative(carDirection, checkpointDirection);

        if (diffAngle < goalAngle)
            return 0;
        else if (diffAngle < decelerateAngle) {
            double newVelocity = diffAngle / decelerateAngle * info.getMaxAbsoluteAngularVelocity() * (angleIsNegative ? -1 : 1) ;
            float newAcceleration = (float) ((newVelocity - info.getAngularVelocity()) / steerTime);
            return Math.max(Math.min(newAcceleration, info.getMaxAbsoluteAngularAcceleration()), -info.getMaxAbsoluteAngularAcceleration());
        } else
            return info.getMaxAbsoluteAngularAcceleration() * (angleIsNegative ? -1 : 1);
    }

    private boolean angleIsNegative(Vector2f from, Vector2f to) {
        return 0 >= from.getX() * to.getY() - from.getY() * to.getX();
    }

    /*

    @Override
    public void doDebugStuff() {
        double orientation = info.getOrientation();
        Vector2f direction = new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation));
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(1, 0, 0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getX() + direction.getX() * 10, info.getY() + direction.getY() * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(0, 1, 0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY());
        GL11.glEnd();
    }

     */
}
