package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * FTC Driver for HuskyLens Gen1 by DFRobot.
 */
@I2cDeviceType
@DeviceProperties(name = "HuskyLens", description = "DFRobot HuskyLens AI Camera", xmlTag = "HuskyLens")
public class HuskyLens extends I2cDeviceSynchDevice<I2cDeviceSynch> {

    public static final I2cAddr DEFAULT_ADDRESS = I2cAddr.create7bit(0x32);

    private static final byte[] HEADER = {(byte) 0x55, (byte) 0xAA};
    private static final byte ADDRESS = 0x11;

    public enum Algorithm {
        FACE_RECOGNITION(0x0000),
        OBJECT_TRACKING(0x0001),
        OBJECT_RECOGNITION(0x0002),
        LINE_TRACKING(0x0003),
        COLOR_RECOGNITION(0x0004),
        TAG_RECOGNITION(0x0005),
        OBJECT_CLASSIFICATION(0x0006),
        QR_CODE_RECOGNITION(0x0007),
        BARCODE_RECOGNITION(0x0008);

        public final int id;
        Algorithm(int id) { this.id = id; }
    }

    public static class Block {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final int id;

        public Block(int x, int y, int width, int height, int id) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.id = id;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.US, "Block(x=%d, y=%d, w=%d, h=%d, id=%d)", x, y, width, height, id);
        }
    }

    public static class Arrow {
        public final int xTail;
        public final int yTail;
        public final int xHead;
        public final int yHead;
        public final int id;

        public Arrow(int xTail, int yTail, int xHead, int yHead, int id) {
            this.xTail = xTail;
            this.yTail = yTail;
            this.xHead = xHead;
            this.yHead = yHead;
            this.id = id;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.US, "Arrow(tx=%d, ty=%d, hx=%d, hy=%d, id=%d)", xTail, yTail, xHead, yHead, id);
        }
    }

    public HuskyLens(I2cDeviceSynch deviceClient) {
        super(deviceClient, true);
        this.deviceClient.setI2cAddress(DEFAULT_ADDRESS);
        super.registerArmingStateCallback(false);
        this.deviceClient.engage();
    }

    @Override
    protected boolean doInitialize() {
        return knock();
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "HuskyLens";
    }

    public boolean knock() {
        writeCommand(0x2C);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    public void selectAlgorithm(Algorithm algorithm) {
        byte[] data = new byte[2];
        data[0] = (byte) (algorithm.id & 0xFF);
        data[1] = (byte) ((algorithm.id >> 8) & 0xFF);
        writeCommand(0x2D, data);
        readResponse(); // Consume response
    }

    /**
     * Learn the current object as the specified ID.
     */
    public boolean learn(int id) {
        byte[] data = new byte[2];
        data[0] = (byte) (id & 0xFF);
        data[1] = (byte) ((id >> 8) & 0xFF);
        writeCommand(0x36, data);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Forget all learned data for the current algorithm.
     */
    public boolean forget() {
        writeCommand(0x37);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Set a custom name for a specific ID.
     */
    public boolean setCustomName(String name, int id) {
        byte[] nameBytes = name.getBytes();
        // Protocol: [ID, length+1, name..., 0x00]
        byte[] data = new byte[nameBytes.length + 3];
        data[0] = (byte) (id & 0xFF);
        data[1] = (byte) (nameBytes.length + 1);
        System.arraycopy(nameBytes, 0, data, 2, nameBytes.length);
        data[data.length - 1] = 0x00;
        writeCommand(0x2F, data);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Display custom text on the HuskyLens screen.
     */
    public boolean customText(String text, int x, int y) {
        byte[] textBytes = text.getBytes();
        byte[] data = new byte[textBytes.length + 4];
        data[0] = (byte) textBytes.length;
        // x coordinate logic: if x > 255, byte1=0xFF and byte2=x-255
        data[1] = (byte) (x > 255 ? 0xFF : 0x00);
        data[2] = (byte) (x > 255 ? x % 255 : x);
        data[3] = (byte) (y & 0xFF);
        System.arraycopy(textBytes, 0, data, 4, textBytes.length);
        writeCommand(0x34, data);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Clear all custom text from the screen.
     */
    public boolean clearText() {
        writeCommand(0x35);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Save current model to SD card.
     */
    public boolean saveModelToSDCard(int modelIndex) {
        byte[] data = new byte[2];
        data[0] = (byte) (modelIndex & 0xFF);
        data[1] = (byte) ((modelIndex >> 8) & 0xFF);
        writeCommand(0x32, data);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Load model from SD card.
     */
    public boolean loadModelFromSDCard(int modelIndex) {
        byte[] data = new byte[2];
        data[0] = (byte) (modelIndex & 0xFF);
        data[1] = (byte) ((modelIndex >> 8) & 0xFF);
        writeCommand(0x33, data);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Save current frame as a picture to SD card.
     */
    public boolean savePictureToSDCard() {
        writeCommand(0x30);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Save current screen screenshot to SD card.
     */
    public boolean saveScreenshotToSDCard() {
        writeCommand(0x39);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    /**
     * Check if the HuskyLens is the Pro version.
     */
    public boolean isPro() {
        writeCommand(0x3B);
        byte[] response = readResponse();
        if (response == null || response.length < 6) return false;
        return response[5] == 0x01; 
    }

    /**
     * Get the firmware version of the HuskyLens.
     */
    public String getFirmwareVersion() {
        writeCommand(0x3C);
        byte[] response = readResponse();
        if (response == null || response.length < 6) return "Unknown";
        int len = response[3] & 0xFF; // Data length byte from header
        if (response.length < 5 + len) return "Unknown";
        // String starts at index 5 (HEADER(2), ADDR(1), LEN(1), CMD(1))
        return new String(response, 5, len);
    }

    /**
     * Write sensor data (for specific modes).
     */
    public boolean writeSensor(int s1, int s2, int s3) {
        byte[] data = new byte[10]; // C++ sends 5 shorts
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) s1);
        buffer.putShort((short) s2);
        buffer.putShort((short) s3);
        buffer.putShort((short) 0);
        buffer.putShort((short) 0);
        writeCommand(0x3D, data);
        byte[] response = readResponse();
        return response != null && response[3] == 0x2E;
    }

    public List<Block> getBlocks() {
        writeCommand(0x21);
        return processRequest().blocks;
    }

    public List<Block> getBlocksByID(int id) {
        byte[] data = new byte[2];
        data[0] = (byte) (id & 0xFF);
        data[1] = (byte) ((id >> 8) & 0xFF);
        writeCommand(0x27, data);
        return processRequest().blocks;
    }

    public List<Arrow> getArrows() {
        writeCommand(0x22);
        return processRequest().arrows;
    }

    public List<Arrow> getArrowsByID(int id) {
        byte[] data = new byte[2];
        data[0] = (byte) (id & 0xFF);
        data[1] = (byte) ((id >> 8) & 0xFF);
        writeCommand(0x28, data);
        return processRequest().arrows;
    }

    /**
     * Get all objects (blocks and arrows) detected in the current frame.
     */
    public Result getAllObjects() {
        writeCommand(0x20);
        return processRequest();
    }

    /**
     * Get all learned objects (blocks and arrows).
     */
    public Result getLearnedObjects() {
        writeCommand(0x23);
        return processRequest();
    }

    /**
     * Get all learned blocks.
     */
    public List<Block> getLearnedBlocks() {
        writeCommand(0x24);
        return processRequest().blocks;
    }

    /**
     * Get all learned arrows.
     */
    public List<Arrow> getLearnedArrows() {
        writeCommand(0x25);
        return processRequest().arrows;
    }

    /**
     * Get both blocks and arrows with a specific ID.
     */
    public Result getObjectsByID(int id) {
        byte[] data = new byte[2];
        data[0] = (byte) (id & 0xFF);
        data[1] = (byte) ((id >> 8) & 0xFF);
        writeCommand(0x26, data);
        return processRequest();
    }

    public static class Result {
        public final List<Block> blocks;
        public final List<Arrow> arrows;
        public final int count;
        public final int learnedCount;
        public final int frameNumber;

        public Result(List<Block> blocks, List<Arrow> arrows, int count, int learnedCount, int frameNumber) {
            this.blocks = blocks;
            this.arrows = arrows;
            this.count = count;
            this.learnedCount = learnedCount;
            this.frameNumber = frameNumber;
        }
    }

    public int count() {
        writeCommand(0x20);
        return processRequest().count;
    }

    public int learnedObjCount() {
        writeCommand(0x20);
        return processRequest().learnedCount;
    }

    public int frameNumber() {
        writeCommand(0x20);
        return processRequest().frameNumber;
    }

    private Result processRequest() {
        List<Block> blocks = new ArrayList<>();
        List<Arrow> arrows = new ArrayList<>();
        int count = 0;
        int learnedCount = 0;
        int frameNumber = 0;

        byte[] headerResponse = readResponse();
        if (headerResponse == null || headerResponse.length < 5) {
            return new Result(blocks, arrows, 0, 0, 0);
        }

        int infoCommand = headerResponse[3] & 0xFF;
        if (infoCommand != 0x29) {
            return new Result(blocks, arrows, 0, 0, 0);
        }

        int numItems = (headerResponse[5] & 0xFF) | ((headerResponse[6] & 0xFF) << 8);
        count = numItems;
        learnedCount = (headerResponse[7] & 0xFF) | ((headerResponse[8] & 0xFF) << 8);
        frameNumber = (headerResponse[9] & 0xFF) | ((headerResponse[10] & 0xFF) << 8);

        for (int i = 0; i < numItems; i++) {
            byte[] itemResponse = readResponse();
            if (itemResponse == null || itemResponse.length < 5) break;

            int command = itemResponse[3] & 0xFF;
            ByteBuffer buffer = ByteBuffer.wrap(itemResponse, 5, itemResponse.length - 6);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            if (command == 0x2A) { // Block
                blocks.add(new Block(
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF
                ));
            } else if (command == 0x2B) { // Arrow
                arrows.add(new Arrow(
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF,
                        buffer.getShort() & 0xFFFF
                ));
            }
        }
        return new Result(blocks, arrows, count, learnedCount, frameNumber);
    }

    private void writeCommand(int command) {
        writeCommand(command, new byte[0]);
    }

    private void writeCommand(int command, byte[] data) {
        byte[] packet = new byte[6 + data.length];
        packet[0] = HEADER[0];
        packet[1] = HEADER[1];
        packet[2] = ADDRESS;
        packet[3] = (byte) data.length;
        packet[4] = (byte) command;
        System.arraycopy(data, 0, packet, 5, data.length);

        int checksum = 0;
        for (int i = 0; i < packet.length - 1; i++) {
            checksum += packet[i] & 0xFF;
        }
        packet[packet.length - 1] = (byte) (checksum & 0xFF);

        deviceClient.write(packet);
    }

    private byte[] readResponse() {
        // First read the header to get the data length
        byte[] header = deviceClient.read(5);
        if (header.length < 5 || (header[0] & 0xFF) != 0x55 || (header[1] & 0xFF) != 0xAA) {
            return null;
        }

        int dataLength = header[3] & 0xFF;
        byte[] remaining = deviceClient.read(dataLength + 1);
        
        byte[] fullResponse = new byte[header.length + remaining.length];
        System.arraycopy(header, 0, fullResponse, 0, header.length);
        System.arraycopy(remaining, 0, fullResponse, header.length, remaining.length);

        // Validate checksum
        int checksum = 0;
        for (int i = 0; i < fullResponse.length - 1; i++) {
            checksum += fullResponse[i] & 0xFF;
        }
        if ((byte) (checksum & 0xFF) != fullResponse[fullResponse.length - 1]) {
            return null;
        }

        return fullResponse;
    }
}
