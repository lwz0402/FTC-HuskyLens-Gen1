package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.huskylens.HuskyLens;

import java.util.List;

/**
 * Practical OpMode: AirTag Recognition and Ball Launching
 * 
 * Logic:
 * 1. Initialize HuskyLens in TAG_RECOGNITION mode.
 * 2. Scan for Tags (AirTags).
 * 3. If a Tag is found, calculate its distance from the screen center (160px).
 * 4. Adjust the 'turret' servo to bring the Tag to the center.
 * 5. Once centered (within a threshold), spin up the launcher motor and fire.
 */
@TeleOp(name = "HuskyLens: AirTag Launcher", group = "Practical")
public class AirTagLauncherOpMode extends LinearOpMode {

    // Hardware Constants
    static final double CENTER_X = 160.0;     // HuskyLens width is 320, so center is 160
    static final double ERROR_THRESHOLD = 10; // Pixels of tolerance
    static final double SERVO_SPEED = 0.005;   // Adjustment speed for the servo
    static final double LAUNCHER_POWER = 0.8; // Motor power for launching

    @Override
    public void runOpMode() {
        // Hardware Mapping
        HuskyLens huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
        Servo turretServo = hardwareMap.get(Servo.class, "turretServo");
        DcMotor launcherMotor = hardwareMap.get(DcMotor.class, "launcherMotor");

        double servoPosition = 0.5; // Start servo at middle position
        turretServo.setPosition(servoPosition);

        telemetry.addData("Status", "Ready. Using TAG_RECOGNITION.");
        telemetry.update();

        // Ensure we are in Tag Recognition mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

        waitForStart();

        while (opModeIsActive()) {
            List<HuskyLens.Block> tags = huskyLens.getBlocks();
            
            if (!tags.isEmpty()) {
                // Focus on the first detected tag
                HuskyLens.Block targetTag = tags.get(0);
                double error = targetTag.x - CENTER_X;

                // 1. ROTATE SERVO TO CENTER
                if (Math.abs(error) > ERROR_THRESHOLD) {
                    // If error is positive, tag is to the right, move servo left (or vice versa depending on mount)
                    if (error > 0) {
                        servoPosition -= SERVO_SPEED;
                    } else {
                        servoPosition += SERVO_SPEED;
                    }
                    
                    // Clip servo position between 0 and 1
                    servoPosition = Math.max(0, Math.min(1, servoPosition));
                    turretServo.setPosition(servoPosition);
                    
                    launcherMotor.setPower(0); // Don't fire while moving
                    telemetry.addData("Action", "Centering Tag...");
                } 
                // 2. CENTERED -> LAUNCH
                else {
                    telemetry.addData("Action", "LOCKED! Launching!!!");
                    launcherMotor.setPower(LAUNCHER_POWER);
                    // Add a small delay for the launch sequence
                    sleep(1000); 
                    launcherMotor.setPower(0);
                }

                telemetry.addData("Tag X", targetTag.x);
                telemetry.addData("Error", error);
            } else {
                telemetry.addData("Action", "Scanning for Tags...");
                launcherMotor.setPower(0);
            }

            telemetry.addData("Servo Pos", "%.3f", servoPosition);
            telemetry.update();
        }
    }
}
