package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import java.util.List;

/**
 * HUSKYLENS MASTER SAMPLE
 * 
 * This OpMode combines all features into one comprehensive example:
 * 1. Basic Object Tracking (Blocks)
 * 2. Line Tracking Logic (Arrows & Steering)
 * 3. Advanced UI Customization (Names & Text Overlays)
 * 4. System Metadata (Pro status, Firmware, Frame rates)
 * 5. Learning & Memory Management (Learn/Forget)
 * 6. SD Card Operations (Models, Photos, Screenshots)
 */
@TeleOp(name = "HuskyLens: MASTER SAMPLE", group = "Sensor")
public class HuskyLensMasterSample extends LinearOpMode {

    @Override
    public void runOpMode() {
        HuskyLens huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");

        /* 1. INITIALIZATION & METADATA */
        telemetry.addData("Status", "Connecting...");
        telemetry.update();

        if (huskyLens.knock()) {
            telemetry.addData("HuskyLens", "Connected!");
        } else {
            telemetry.addData("HuskyLens", "NOT FOUND! Check wires.");
        }

        telemetry.addData("Is Pro", huskyLens.isPro());
        telemetry.addData("Firmware", huskyLens.getFirmwareVersion());
        telemetry.update();

        /* 2. UI SETUP */
        // Set custom names for learned IDs (IDs appear on camera screen)
        huskyLens.setCustomName("Team Prop", 1);
        huskyLens.setCustomName("Goal", 2);
        huskyLens.clearText(); // Clear any old custom text
        
        // Default to Object Recognition
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION);

        waitForStart();

        while (opModeIsActive()) {
            /* 3. ALGORITHM SWITCHING (via D-Pad) */
            if (gamepad1.dpad_up) huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION);
            if (gamepad1.dpad_down) huskyLens.selectAlgorithm(HuskyLens.Algorithm.LINE_TRACKING);
            if (gamepad1.dpad_left) huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);

            /* 4. SYSTEM DATA */
            telemetry.addLine("=== SYSTEM ===");
            telemetry.addData("Algorithm", "Up:Obj, Down:Line, Left:Tag");
            telemetry.addData("Frame", huskyLens.frameNumber());
            telemetry.addData("Learned IDs", huskyLens.learnedObjCount());

            /* 5. DETECTION DATA (Blocks & Arrows) */
            telemetry.addLine("\n=== DETECTIONS ===");
            
            // Get all results in one request for efficiency
            HuskyLens.Result result = huskyLens.getAllObjects();
            telemetry.addData("Total Objects", result.count);

            // Handle Blocks (Used in Object/Face/Tag Recognition)
            for (HuskyLens.Block block : result.blocks) {
                telemetry.addData("Block ID " + block.id, "X:%d Y:%d", block.x, block.y);
            }

            // Handle Arrows (Used in Line Tracking)
            for (HuskyLens.Arrow arrow : result.arrows) {
                // Calculate steering error (center of screen is 160)
                double steerError = arrow.xHead - 160;
                telemetry.addData("Arrow ID " + arrow.id, "Steer: %.1f", steerError);
            }

            /* 6. SCREEN UI (Debugging on Camera LCD) */
            huskyLens.customText("Objects: " + result.count, 10, 10);
            huskyLens.customText("IDs: " + result.learnedCount, 10, 30);

            /* 7. INTERACTION (Buttons) */
            if (gamepad1.x) {
                huskyLens.learn(1); // Learn center object as ID 1
                telemetry.addLine("ACTION: Learn ID 1");
            }
            if (gamepad1.y) {
                huskyLens.forget(); // Wipe current algorithm's data
                telemetry.addLine("ACTION: Forget Data");
            }

            /* 8. SD CARD OPS */
            if (gamepad1.a) {
                huskyLens.saveScreenshotToSDCard();
                telemetry.addLine("ACTION: Screenshot Saved");
            }
            if (gamepad1.b) {
                huskyLens.savePictureToSDCard();
                telemetry.addLine("ACTION: Photo Saved");
            }
            
            // Example: Load specific model for this OpMode
            if (gamepad1.right_bumper) {
                huskyLens.loadModelFromSDCard(1);
                telemetry.addLine("ACTION: Loaded Model 1");
            }

            telemetry.update();
            sleep(50); // Prevent I2C bus congestion
        }
    }
}
