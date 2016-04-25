package org.surfing.service.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.surfing.Service;

/**
 *
 * @author Vinicius Senger
 */
public class FTPService extends Service {

    public static String FTP_SERVER;
    public static int FTP_PORT;
    public static String FTP_USER;
    public static String FTP_PASSWORD;
    public static String FTP_LOCAL_DIR;
    public static String FTP_LOCAL_STORAGE_DIR;
    public static String FTP_REMOTE_DIR;
    public static int RETRY=3;
    private static FTPClient ftp;

    @Override
    public void start() {
        if (getConfig().getProperty("ftp.server") != null) {
            FTP_SERVER = getConfig().getProperty("ftp.server");
        }
        if (getConfig().getProperty("retry") != null) {
            RETRY = Integer.parseInt(getConfig().getProperty("retry"));
        }

        if (getConfig().getProperty("ftp.port") != null) {
            FTP_PORT = Integer.parseInt(getConfig().getProperty("ftp.port"));
        }
        if (getConfig().getProperty("ftp.user") != null) {
            FTP_USER = getConfig().getProperty("ftp.user");
        }
        if (getConfig().getProperty("ftp.password") != null) {
            FTP_PASSWORD = getConfig().getProperty("ftp.password");
        }
        if (getConfig().getProperty("ftp.local.dir") != null) {
            FTP_LOCAL_DIR = getConfig().getProperty("ftp.local.dir");
        }
        if (getConfig().getProperty("ftp.local.storage.dir") != null) {
            FTP_LOCAL_STORAGE_DIR = getConfig().getProperty("ftp.local.storage.dir");
        }
        if (getConfig().getProperty("ftp.remote.dir") != null) {
            FTP_REMOTE_DIR = getConfig().getProperty("ftp.remote.dir");
        }

    }

    @Override
    public void stop() {
        if (ftp != null && ftp.isConnected()) {
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(FTPService.class.getName()).log(Level.SEVERE, "Error disconnecting FTP client!", ex);
                }
            }
        }
    }

    //este método vai pegar tudo que está no FTP_LOCAL_DIR, 
    //vai fazer upload para FTP_REMOTE_DIR e vai mover o conteúdo para
    //FTP_LOCAL_STORAGE_DIR..
    @Override
    public void run() {
        File dir = new File(FTP_LOCAL_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        uploadDir(dir, "/");
    }

    public static void uploadDir(File dir, String workingDir) {
        File storage = new File(FTP_LOCAL_STORAGE_DIR);
        storage.mkdirs();
        if (dir.listFiles().length == 0) {
            return;
        }
        //let's work!
        File toUpload[] = dir.listFiles();
        for (File fileToUpload : toUpload) {
            try {
                if (fileToUpload.isDirectory()) {
                    upload(fileToUpload, workingDir);
                    Logger.getLogger(FTPService.class.getName()).log(Level.INFO, "Checking Directory " + workingDir);
                    new File(storage.getAbsolutePath() + workingDir + fileToUpload.getName()).mkdir();
                    uploadDir(fileToUpload, workingDir + fileToUpload.getName() + "/");
                    fileToUpload.delete();
                } else {
                    upload(fileToUpload, workingDir);
                    File toRename = new File(storage.getAbsolutePath() + workingDir + fileToUpload.getName());
                    Logger.getLogger(FTPService.class.getName()).log(Level.INFO, "Uploading " + fileToUpload.getName());
                    fileToUpload.renameTo(toRename);
                    Logger.getLogger(FTPService.class.getName()).log(Level.INFO, fileToUpload.getName() + " uploaded!");
                }
            } catch (IOException ex) {
                Logger.getLogger(FTPService.class.getName()).log(Level.SEVERE, "Error uploading file "
                        + fileToUpload.getName() + " to server "
                        + FTP_SERVER + "/" + FTP_REMOTE_DIR, ex);
            }
        }

    }

    public static void getFTPClient() throws IOException {
        //if (ftp == null || !ftp.isConnected()) {
            ftp = new FTPClient();
            ftp.connect(FTP_SERVER, FTP_PORT);
            ftp.login(FTP_USER, FTP_PASSWORD);
            ftp.enterLocalPassiveMode();
        //}
    }

    public static void upload(File file, String workingDir) throws IOException {
        for (int x = 0; x < RETRY; x++) {
            getFTPClient();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            if (file.isDirectory()) {
                ftp.makeDirectory(FTP_REMOTE_DIR + workingDir + file.getName());
                Logger.getLogger(FTPClient.class.getName()).log(Level.INFO, "Directory created {0}", file.getName());
                return;
            }
            InputStream inputStream = new FileInputStream(file);
            String remoteFileName = FTP_REMOTE_DIR + workingDir + file.getName();
            boolean done = ftp.storeFile(remoteFileName, inputStream);
            inputStream.close();
            if (done) {
                Logger.getLogger(FTPService.class.getName()).log(Level.INFO, "File uploaded {0}", file.getName());
                break;
            }
        }
    }

    public static void download(String remoteDirectory, String remoteFile, String localDirectory) throws IOException {
        getFTPClient();
        ftp.enterLocalPassiveMode();
        OutputStream outputStream = new FileOutputStream(localDirectory + "/" + remoteFile);
        ftp.retrieveFile(remoteDirectory + "/" + remoteFile, outputStream);
        outputStream.close();

    }

    public static List<String> downloadDir(String remoteDirectory, String localDirectory) throws IOException {
        getFTPClient();
        List<String> filesDownloaded = new ArrayList<String>();
        ftp.enterLocalPassiveMode();
        ftp.changeWorkingDirectory(remoteDirectory);
        FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles != null && ftpFiles.length > 0) {
            for (FTPFile file : ftpFiles) {
                if (!file.isFile() && !file.isDirectory()) {
                    continue;
                }
                filesDownloaded.add(remoteDirectory + "/" + file.getName());
                if (file.isDirectory()) {
                    File dirLocal = new File(localDirectory + "/" + file.getName());
                    dirLocal.mkdirs();
                    filesDownloaded.addAll(downloadDir(remoteDirectory + "/" + file.getName(),
                            localDirectory + "/" + file.getName()));
                } else {
                    OutputStream output;
                    output = new FileOutputStream(localDirectory + "/" + file.getName());
                    ftp.retrieveFile(file.getName(), output);
                    output.close();
                }
            }
        }
        return filesDownloaded;
    }
}
