package org.surfing.service.audio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import org.surfing.Service;

/**
 *
 * @author Vinicius Senger
 */
@Named
public class AudioTTS extends Service {

    private static AudioTTS instance = new AudioTTS();
    public static String SPEAK_CMD = "espeak -p10 -k5 -s150 -v en -f ";
    public static String WELCOME_TEXT = "Welcome to Surfing I.O.T Services ";

    static Runtime rt = Runtime.getRuntime();

    public synchronized static void speak(String text, boolean waitFor) {
        try {
            File f = new File("speaktemp.txt");
            FileOutputStream fw = new FileOutputStream(f, false);
            BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(fw, "UTF-8"));
            bf.write(text);
            bf.flush();
            bf.close();
            String comando = SPEAK_CMD.replace("<filename>", f.getAbsolutePath());
            System.out.println("Comando voz " + comando);
            Process pr1 = rt.exec(comando);
            if (waitFor) {
                pr1.waitFor();
            }
        } catch (Exception ex) {
            Logger.getLogger(AudioTTS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //raspistill -t 1 -n -o
    //raspivid -t <segundos> -o
    public static AudioTTS getInstance() {
        return instance;
    }

    @Override
    public void start() {
        if (getConfig().getProperty("speak.cmd") != null) {
            SPEAK_CMD = getConfig().getProperty("speak.cmd");
        }
        if (getConfig().getProperty("welcome.text") != null) {
            WELCOME_TEXT = getConfig().getProperty("welcome.text");
        }
        
        if(WELCOME_TEXT!=null && !WELCOME_TEXT.equals("")) {
            speak(WELCOME_TEXT, true);
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void run() {
    }

}
