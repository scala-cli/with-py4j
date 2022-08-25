//> using lib "net.sf.py4j:py4j:0.10.9.7"

package withpy4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Arrays;

import py4j.ClientServer;

public final class WithPy4j {

    private static final String portProperty = "with-py4j.port";
    private static final String secretProperty = "with-py4j.secret";

    public static final void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.err.println("Usage: with-py4j main-class args...");
            System.exit(1);
        }

        boolean debug = Boolean.getBoolean("with-py4j.debug");

        String mainClassName = args[0];
        String[] actualArgs = Arrays.copyOfRange(args, 1, args.length);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        final Class<?> mainClass;
        final Method mainMethod;
        try {
            mainClass = cl.loadClass(mainClassName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot find main class " + mainClassName, ex);
        }

        try {
            Class<?>[] params = { String[].class };
            mainMethod = mainClass.getMethod("main", params);
        }
        catch (NoSuchMethodException ex) {
            throw new RuntimeException("Cannot find main method in class " + mainClassName, ex);
        }

        int port = Integer.parseInt(System.getProperty(portProperty, "0"));

        String secret = System.getProperty(secretProperty);
        if (secret == null) {
            SecureRandom random = new SecureRandom();
            byte[] secretData = new byte[256 / 8];
            random.nextBytes(secretData);
            secret = new BigInteger(1, secretData).toString();
        }

        ClientServer server = new ClientServer.ClientServerBuilder()
            .javaPort(port)
            .authToken(secret)
            .javaAddress(InetAddress.getLoopbackAddress())
            .build();

        int actualPort = server.getJavaServer().getListeningPort();

        System.setProperty(portProperty, Integer.toString(actualPort));
        System.setProperty(secretProperty, secret);

        try {
            if (debug)
                System.err.println("Starting server on port " + actualPort);
            server.startServer();
    
            try {
                Object[] mainArgs = { actualArgs };
                mainMethod.invoke(null, mainArgs);
            }
            catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
        finally {
            server.shutdown();
        }
    }
}
