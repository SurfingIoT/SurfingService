package org.surfing.service.admin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.surfing.Service;
import org.surfing.service.ftp.FTPService;

/**
 *
 * @author vsenger
 */
@Path("/system")
public class AdminREST extends Service {

    public static String ADMIN_PASSWORD = "surfing";
    public static String REMOTE_DIR;
    public static String LOCAL_DIR;

    @Override
    public void run() {
        update();
    }

    @GET
    @Produces("text/html")
    @Path("/download-updates")
    public String update() {
        String retorno = "<hmtl><body><h1> Surfing IoT Update</h1>";
        try {
            retorno += "<h2>Starting FTP</h2>";
            List<String> files = FTPService.downloadDir(REMOTE_DIR, LOCAL_DIR);
            retorno += "<p>" + files.size() + " files downloaded:</p>";
            retorno += "<ul>";
            for (String f : files) {
                retorno += "<li>" + f + "</li>";
            }
            retorno += "</ul>";
        } catch (IOException ex) {
            retorno += "<p>Critical error updating:" + ex.getMessage() + "</p>";
            Logger.getLogger(AdminREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        retorno += " </body></html>";
        return retorno;
    }

    @GET
    @Produces("text/html")
    @Path("/execute/{script}/{password}")
    public String execute(@PathParam("script") String script, @PathParam("password") String password) {

        if (!password.equals(ADMIN_PASSWORD)) {
            return "<html><body><h1>Wrong password</h1></body></html>";
        }
        String retorno = "<hmtl><body><h1> Surfing IoT Update</h1>";
        retorno += "<h2>Script Executed</h2>";
        Process pr;
        try {
            Runtime.getRuntime().exec("chmod 755 " + LOCAL_DIR + "/" + script);
            pr = Runtime.getRuntime().exec(LOCAL_DIR + "/" + script + " > " + LOCAL_DIR + "/stdout.txt 2> " 
                    + LOCAL_DIR + "/error.txt ");
            pr.waitFor();
            BufferedReader bf = new BufferedReader(new FileReader(LOCAL_DIR + "/stdout.txt"));
            String linha, outStd = "";

            while ((linha = bf.readLine()) != null) {
                outStd += "" + (char) pr.getInputStream().read();

            }
            bf.close();
            retorno += "<h3>Log output</h3>" + outStd;

            /*String outErro = "";
             while (pr.getErrorStream().available() > 0) {
             outErro += "" + (char) pr.getErrorStream().read();
             }
             retorno += "<h3>Error Log</h3>" + outErro;*/
        } catch (IOException ex) {
            Logger.getLogger(AdminREST.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(AdminREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }

    @Override
    public void start() {
        if (getConfig().getProperty("admin.password") != null) {
            ADMIN_PASSWORD = getConfig().getProperty("admin.password");
        }
        if (getConfig().getProperty("ftp.remote.dir") != null) {
            REMOTE_DIR = getConfig().getProperty("ftp.remote.dir");
        }
        if (getConfig().getProperty("ftp.local.dir") != null) {
            LOCAL_DIR = getConfig().getProperty("ftp.local.dir");
        }
    }

    @Override
    public void stop() {
    }

}
