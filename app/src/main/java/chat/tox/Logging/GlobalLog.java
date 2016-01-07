package chat.tox.Logging;

import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import chat.tox.antox.tox.ToxSingleton;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Ann on 1/4/16.
 * Logging System
 */
public class GlobalLog {
    //private static GlobalLog mGlobalLog;

    // True: turn on the logging mode
    private static boolean logSwitch = true;
    private static String logName = "AntoxLog.txt";
    private static SimpleDateFormat logSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");

    // message = time + event + source + destination
    /*
    * eventId = 1: receive a message -> MessageHelper
    * eventId = 2: like a message -> EvalActivity
    * eventId = 3: dislike a message -> EvalActivity
    * eventId = 4: send a message -> MessageHelper
    * eventId = 5: add a friend -> AntoxDB
    * */
    public static void log(String eventId, String destination) {
        if (logSwitch) {
            writeLogToFile(eventId, destination);
        }
    }

    //
    private static void writeLogToFile(String eventId, String destination) {
        Date nowtime = new Date();

        String source = ToxSingleton.tox().getAddress().toString();

        String needWriteMessage = logSdf.format(nowtime) + "\t" + eventId + "\t" + source + "\t" + destination;

        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/Antox/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Antox/", logName);

        try {
            FileWriter filerWriter = new FileWriter(file, true);
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
