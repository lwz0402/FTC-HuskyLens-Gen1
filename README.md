# HuskyLens FTC Java Library

Java driver for **DFRobot HuskyLens Gen1** camera designed specifically for the FIRST Technology Challenge (FTC) platform and REV robot control.

## Development Information
- **Developer**: Arthur LIU from First Tech Challenge Team #25787 & #27570
- **Official Sponsor**: Proudly sponsored by **DFRobot**, the creators of HuskyLens.

## Features
- **Full Protocol Support**: Implements all commands from the official HuskyLens Protocol (v0.5.1).
- **All Algorithms**: Native support for Face Recognition, Object Tracking, Line Tracking, Color Recognition, Tag/AprilTag Recognition, and more.
- **Robust Communication**: Built-in I2C checksum validation to ensure data integrity during competition.
- **Custom UI**: Easily overlay custom text and names on the HuskyLens built-in LCD screen.
- **Advanced Metadata**: Access frame numbers, counts, and learned object statistics.
- **SD Card Management**: Save/Load AI models and capture screenshots directly from code.

## Installation
1. Copy the `HuskyLens.java` file into your `TeamCode` folder (suggested package: `org.firstinspires.ftc.teamcode.huskylens`).
2. In the FTC Driver Station app, configure your I2C device:
   - **Type**: `HuskyLens` (This matches the `@DeviceProperties` annotation in the code).
   - **Name**: "huskylens" (or your preferred name).
  
## Tips:
Only the following files/folders are related to this driver
**Essential files**
huskylens(TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens):This folder defines the HuskyLens driver class, and if deleted, this driver cannot be referenced.
HuskyLens.java(TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/HuskyLens.java):This file contains the core code of HuskyLens driver. If deleted, this driver will not function properly.
**Optional files**
HuskyLensMasterSample.java(TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/HuskyLensMasterSample.java):This document introduces the calling methods for all functions.
README.md(TeamCode/src/main/java/org/firstinspires/ftc/teamcode/huskylens/README.md):This document introduces the basic information and functions of this driver.
AirTagLauncherOpMode.java(TeamCode/src/main/java/org/firstinspires/ftc/teamcode/AirTagLauncherOpMode.java):This document demonstrates the functionality of using HuskyLens' Airtag recognition feature for shooting.

---

## Full API Reference

### 1. Initialization & Configuration
- `selectAlgorithm(Algorithm algorithm)`: Switch between AI modes (e.g., `TAG_RECOGNITION`).
- `knock()`: Check if the HuskyLens is connected and responding.
- `writeSensor(int s1, int s2, int s3)`: Write raw sensor data (for specific modes).

### 2. Object Detection (Blocks)
- `getBlocks()`: Get all detected blocks in the current frame.
- `getBlocksByID(int id)`: Get only blocks matching a specific learned ID.
- `getLearnedBlocks()`: Get all blocks that have been "learned" (ID >= 1).

### 3. Directional Detection (Arrows)
- `getArrows()`: Get all detected arrows (used for Line Tracking).
- `getArrowsByID(int id)`: Get only arrows matching a specific learned ID.
- `getLearnedArrows()`: Get all learned arrows.

### 4. Comprehensive Queries
- `getAllObjects()`: Returns a `Result` object containing ALL blocks and arrows on screen.
- `getLearnedObjects()`: Returns a `Result` containing all learned blocks and arrows.
- `getObjectsByID(int id)`: Returns a `Result` for a specific ID.

### 5. Metadata & Status
- `count()`: Number of objects in the current frame.
- `learnedObjCount()`: Number of unique IDs the sensor has learned.
- `frameNumber()`: The current frame index from the camera.
- `isPro()`: Returns `true` if using the HuskyLens Pro hardware.
- `getFirmwareVersion()`: Returns the firmware version string (e.g., "0.5.1").

### 6. Learning & Memory
- `learn(int id)`: Programmatically trigger the "Learn" function for the current object.
- `forget()`: Clear learned data for the current algorithm.

### 7. Custom UI (On-Screen Display)
- `setCustomName(String name, int id)`: Display a name above a specific ID on the HuskyLens screen.
- `customText(String text, int x, int y)`: Draw custom text at (x,y) on the camera's LCD.
- `clearText()`: Remove all custom text overlays.

### 8. SD Card Operations
- `saveModelToSDCard(int index)`: Save the current trained model to the SD card.
- `loadModelFromSDCard(int index)`: Load a previously saved model.
- `savePictureToSDCard()`: Take a photo of the current camera frame.
- `saveScreenshotToSDCard()`: Save a screenshot of the entire HuskyLens UI.

---

## Basic Usage Example

```java
HuskyLens huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION);

// In your loop
List<HuskyLens.Block> blocks = huskyLens.getBlocks();
for (HuskyLens.Block block : blocks) {
    telemetry.addData("ID", block.id);
    telemetry.addData("Pos", "X: %d, Y: %d", block.x, block.y);
}
```

## License
MIT License

Copyright © 2026 Arthur LIU

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
