package com.bupt.videoproc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Metrics {
    static String TAG = "Metrics";
    static String USBCurrent = "/sys/class/power_supply/usb/current_now";
    static String USBVoltage = "/sys/class/power_supply/usb/voltage_now";
    static String BatteryCurrent = "/sys/class/power_supply/battery/current_now";
    static String BatteryVoltage = "/sys/class/power_supply/battery/voltage_now";

    public static String exec(String cmd) {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes(cmd + "\n");
            outputStream.flush();
            Log.i(TAG, "exec: finish inputing " + cmd);

            DataInputStream inputStream = new DataInputStream(su.getInputStream());
            StringBuilder ret = new StringBuilder();
            String line = inputStream.readLine();
            while (line != null) {
                ret.append(line).append("\n");
                line = inputStream.readLine();
            }
            Log.i(TAG, "exec: return result = " + ret);

            return ret.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getFileContent(String filePath) {
        File file = new File(filePath);
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static String readPowerInfo(int duration) {
        for (int i = 0; i < 1000; i++) {
            long time = System.currentTimeMillis();
            String usbCurrent = getFileContent(USBCurrent);
            String usbVoltage = getFileContent(USBVoltage);
            String batteryCurrent = getFileContent(BatteryCurrent);
            String batteryVoltage = getFileContent(BatteryVoltage);
            Log.i(TAG, "readPowerInfo: time: " + time + ", usbCurrent: " + usbCurrent + ", usbVoltage: " + usbVoltage
                    + ", batteryCurrent: " + batteryCurrent + ", batteryVoltage: " + batteryVoltage);

            // Sleep 50 milliseconds after read file content each time, so 1 second will have 20 samples
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
