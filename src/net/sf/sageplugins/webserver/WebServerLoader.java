package net.sf.sageplugins.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Proxy class for (re)loading webserver classes, eliminating the need to restart SageTV when
 * changing servlet classes.  Only the webserver needs to be restarted.
 * <p>
 * Currently this class is only intended to be used during development.  The need to use it
 * during development is higher than for end users because of the frequent number of restarts
 * needed during development and testing.
 * <p>
 * Instructions:
 * <p>
 * <ol>
 * <li>Stop SageTV</li>
 * <li>Replace <code>net.sf.sageplugins.webserver.StartServer</code>
 * in the value of <code>load_at_startup_runnable_classes</code> in Sage.properties
 * with <code>net.sf.sageplugins.webserver.WebServerLoader</code></li>
 * <li>Compile this file and put it in SageTV's classpath.  This can be deployed either
 * as a JAR file in the JARs folder in the SageTV home, or in the
 * net/sf/sageplugins/webserver folder in the SageTV home.</li>
 * <li>Remove nielm_sagewebserver.jar from the JARs folder.  This is very important.  If any
 * of the desired classes are in SageTV's classpath, they cannot be reloaded (at least not
 * with the current implementation, which uses the default of parent-first classloading).</li>
 * <li>Copy all the .class files from the development location into &lt;SageTV home&gt;/webserver/classes</li>
 * <li>Start SageTV.</li>
 * <li>When changes are made to the servlets, copy the .class files to the above directories
 * and either 'telnet machinename 8088' and press enter to send a blank line, or point the
 * web browser to http://machinename:8088.  A message will be displayed indicating the
 * webserver classes have been reloaded.</li>
 * </ol>
 * <p> there are 2 properties which this classloader uses:</p>
 * <ul>
 * <li>
 * <code>nielm/webserver/classloader/classpath (default: webserver/classes)</code><br/>
 * defining the a ';' separated set of locations where the java class files can be found.<br/>
 * JAR files can also be used by giving a value such as: 
 * <code>webserver/classes;webserver/classes/nielm_sagexmlinfo.jar;webserver/classes/nielm_sagewebserver.jar</code>
 * </li>
 * <li><code>nielm/webserver/classloader/port (default: 8088)</code>
 * This is the port number that the classloader should listen on.  It should not be exposed over the
 * Internet. </li>
 * </ul>
 * 
 */
public class WebServerLoader implements Runnable {

    private static final String CLASSPATH_PROPERTY         = "nielm/webserver/classloader/classpath";
    private static final String CLASSPATH_DEFAULT          = "webserver"+File.separator+"classes";
    private static final String CLASSLOADER_PORT_PROPERTY  = "nielm/webserver/classloader/port";
    private static final int    CLASSLOADER_PORT_DEFAULT   = 8088;
    private static URL[]        classLoaderURLs            = null;
    private URLClassLoader      classLoader                = null;
	private Class<?>               startServerClass           = null;
    private Class<?>               acmeServeClass             = null;
    private Method              acmeServeStopMethod        = null;
	private Object              startServerObject          = null;

    /**
     * Called by SageTV server as a result of this class being registered in the
     * "load_at_startup_runnable_classes" line in Sage.properties
     */
    public void run() {

        Thread.currentThread().setName("WebServer ClassLoader");

        reloadServer();

        // listen for reload command on a port
        log("listening for webserver reload requests");
        listenForReload();
    }

    /**
     * Template method to stop the current server, reload classes, and restart the webserver
     */
    private void reloadServer() {

        // stop the current webserver
        stopWebserver();

        // recreate class loader
        createClassLoader();

        // load new classes
        loadWebserver();

        // start new webserver      
        startWebServer();
    }

    /**
     * Stop the current webserver
     */
    private void stopWebserver() {

        // stop the current server
        if (acmeServeStopMethod != null) {
            // stop server
            try {
                log("stopping webserver");
                acmeServeStopMethod.invoke(acmeServeClass, (Object[])null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read the properties file and recreate the ClassLoader
     */
    private void createClassLoader() {

        try {
            classLoader = null;
            String classpath = (String) sage.SageTV.api("GetProperty", new Object[]{CLASSPATH_PROPERTY, CLASSPATH_DEFAULT});
            log(CLASSPATH_PROPERTY + " = " + classpath);

            String[] classpathArray = classpath.split("[;:]");
            log("number of items in classpath = " + classpathArray.length);
            classLoaderURLs = new URL[classpathArray.length];

            for (int i = 0; i < classLoaderURLs.length; i++) {
                classLoaderURLs[i] = new File(classpathArray[i]).toURI().toURL();
                log("classLoaderURLs[" + i + "] = " + classLoaderURLs[i]);
            }

            classLoader = new URLClassLoader(classLoaderURLs);
            log("classloader " + classLoader.toString());

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Reload the webserver classes
     */
    private void loadWebserver() {

        try {

            startServerClass  = null;
            acmeServeClass    = null;
            startServerObject = null;

            log("reloading webserver classes");
            startServerClass = classLoader.loadClass("net.sf.sageplugins.webserver.StartServer");
            startServerObject = startServerClass.newInstance();
            acmeServeClass = classLoader.loadClass("Acme.Serve.Serve");
            acmeServeStopMethod = acmeServeClass.getMethod("stop", (Class<?>[])null);

            log("startServerClass "    + startServerClass.toString());
            log("startServerObject "   + startServerObject.toString());
            log("acmeServeClass "      + acmeServeClass.toString());
            log("acmeServeStopMethod " + acmeServeStopMethod.toString());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restart the webserver with the new classes
     */
    private void startWebServer() {
        log("starting new webserver");
		Thread t = new Thread((Runnable) startServerObject);
		t.setName("Webserver");
		t.start();
	}

    /**
     * Listen for a connection on port 8088 by default.  When a connection is made,
     * stop the current webserver, reload the classes, and restart the webserver.
     */
    private void listenForReload() {

        ServerSocket serverSocket = null;

        try {
            String classloaderPort = (String) sage.SageTV.api("GetProperty", new Object[]{CLASSLOADER_PORT_PROPERTY, new Integer(CLASSLOADER_PORT_DEFAULT)});
            int port = Integer.parseInt(classloaderPort);
            log(CLASSLOADER_PORT_PROPERTY + " = " + classloaderPort);

            serverSocket = new ServerSocket(port);
 
            /*if (arguments.get(ARG_BINDADDRESS) != null)
                hostName = serverSocket.getInetAddress().getHostName();
            else
                hostName = InetAddress.getLocalHost().getHostName();*/

            while (true) {

                Socket socket = null;
                BufferedReader reader = null;
                PrintStream printer = null;

                try {
                    log("Waiting to accept connection");
                    socket = serverSocket.accept();
                    log("Connection from " + socket.getInetAddress() + ":" + socket.getPort() + " accepted");

                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printer = new PrintStream(socket.getOutputStream());

                    String s = reader.readLine();

                    log("Command received from client: " + s);

                    log("Restarting web server");
                    printer.println("WebServerLoader: Restarting web server");
                    printer.flush();

                    reloadServer();

                } finally {
                    if (printer != null) printer.close();
                    if (reader != null) reader.close();
                    if (socket != null) socket.close();
                }
            }
        } catch (IOException e) {
            log(e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            log(e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            log(e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
            log(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {}
        }
    }

    /**
     * Provide standard prefix on log message so sagetv_0.txt can be grepped.
     * <p>
     * <code>tail -f sagetv_0.txt | grep WebServerLoader</code>
     * @param msg
     */
    private static void log(String msg) {
        System.out.println("WebServerLoader: " + msg);
    }
}
