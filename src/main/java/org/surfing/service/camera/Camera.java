package org.surfing.service.camera;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.surfing.Service;

/**
 *
 * @author Vinicius Senger
 */
@Named
public class Camera extends Service {

    private static Camera instance = new Camera();
    public static String CAMERA_PICTURE_CMD = "ffmpeg -f video4linux2 -i /dev/video0 -vframes 1 ";
    public static String CAMERA_VIDEO_CMD = "ffmpeg -f alsa -i default -f video4linux2 -i /dev/video0 ";
    public static String TIME_FORMAT = "hh:mm:ss";

    //raspistill -t 1 -n -o
    //raspivid -t <segundos> -o
    public static Camera getInstance() {
        return instance;
    }

    @Override
    public void start() {
        if (getConfig().getProperty("camera.time.format") != null) {
            TIME_FORMAT = getConfig().getProperty("camera.time.format");
        }
        if (getConfig().getProperty("camera.picture.cmd") != null) {
            CAMERA_PICTURE_CMD = getConfig().getProperty("camera.picture.cmd");
        }
        if (getConfig().getProperty("camera.video.cmd") != null) {
            CAMERA_VIDEO_CMD = getConfig().getProperty("camera.video.cmd");
        }
        

    }

    @Override
    public void stop() {
    }

    @Override
    public void run() {
    }

    public void takePicture(String fileName, String cameraOptions) throws IOException, InterruptedException {
        String command = CAMERA_PICTURE_CMD.replace("<filename>", fileName);
        System.out.println("Picture command " + command);
        Process pr = Runtime.getRuntime().exec(command);
        pr.waitFor();
    }

    public void recordVideo(String fileName, String cameraOptions, long timeToRecord) throws IOException, InterruptedException {
        String tempo = "" + timeToRecord;
        if (TIME_FORMAT.equals("hh:mm:ss")) {
            tempo = String.format("%d:%d:%d",
                    TimeUnit.MILLISECONDS.toHours(timeToRecord),
                    TimeUnit.MILLISECONDS.toMinutes(timeToRecord),
                    TimeUnit.MILLISECONDS.toSeconds(timeToRecord)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeToRecord))
            );
        }
        
        String command = CAMERA_VIDEO_CMD.replace("<seconds>", tempo);
        command = command.replace("<filename>", fileName);
        
        System.out.println("Command " + command);
        Process pr = Runtime.getRuntime().exec(command);
        pr.waitFor();
    }

}
