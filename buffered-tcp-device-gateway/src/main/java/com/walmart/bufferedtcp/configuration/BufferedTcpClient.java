package com.walmart.bufferedtcp.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 Author: Nick Robinson
 Date: 10/21/21
 Contents:
     TCP socket functionality
     The default size of the buffer is 8192 bytes (8kb)
 */

public class BufferedTcpClient{
    private Socket socket;
    private String ipAddress;
    private int port;
    private BufferedReader input;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BufferedTcpClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void openSocket() {
        try{
            socket = new Socket(ipAddress, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info(String.format("Successfully opened connection to: (%s) port (%s)", ipAddress, port));
        } catch (Exception e) {
            logError(e, "Error connecting to: (%s) port (%s), cause: (%s)");
        }
    }

    public void closeSocket() {
        try{
            input.close();
            socket.close();
            logger.info(String.format("Successfully closed connection to: (%s) port (%s)", ipAddress, port));
        } catch (Exception e) {
            logError(e, "Error closing connection to: (%s) port (%s), cause: (%s)");
        }
    }

    public String readBuffer() {

        ArrayList<String> messages = new ArrayList();
        String line;
        try {
            while (input.ready() && (line = input.readLine()) != null)
                messages.add(line);
        }  catch (Exception e) {
            logError(e,"Error reading message from: (%s) port (%s), cause: (%s)");
        }
        return String.join(",", messages);
    }

    public Boolean isConnected() {
        return socket.isConnected();
    }

    public void logError(Exception e, String format) {

        logger.error(String.format(format, ipAddress, port, e.getCause()));
    }
}
