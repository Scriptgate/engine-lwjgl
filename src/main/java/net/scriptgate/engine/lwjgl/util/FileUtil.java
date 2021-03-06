package net.scriptgate.engine.lwjgl.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    //TODO: add support for filename
    public static File getUniqueFileNameWithTimestamp(File parentDirectory, String extension) {
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        int index = 1;

        File result;
        do {
            result = new File(parentDirectory, timestamp + (index == 1 ? "" : "_" + index) + "." + extension);
            ++index;
        } while (result.exists());
        return result;
    }
}
