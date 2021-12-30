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
    private Long lastConnectFailTime;
    private Long lastNonNullMessageTime;
    private int RECONNECT_TIME = 60000;
    private int NO_MESSAGE_TIMEOUT = 30000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BufferedTcpClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.lastConnectFailTime = System.currentTimeMillis();
        this.lastNonNullMessageTime = System.currentTimeMillis();
    }

    public void openSocket() {
        try{
            socket = new Socket(ipAddress, port);
            socket.setKeepAlive(true);
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
        if(isConnected()) {
            try {
                while (input.ready() && (line = input.readLine()) != null)
                    messages.add(line);
            }  catch (Exception e) {
                logError(e,"Error reading message from: (%s) port (%s), cause: (%s)");
            }
        }
        if(messages.size() > 0) {
            lastNonNullMessageTime = System.currentTimeMillis();
        }
        return String.join(",", messages);
    }

    public Boolean isConnected() {
        Boolean connected = false;
        try {
            connected = socket.isConnected();
        } catch (Exception e) {

        }
        if (!connected) {
            if (System.currentTimeMillis() - lastConnectFailTime > RECONNECT_TIME) {
                this.lastConnectFailTime = System.currentTimeMillis();
                openSocket();
            }
        }
        if (connected) {
            if (System.currentTimeMillis() - lastNonNullMessageTime > NO_MESSAGE_TIMEOUT) {
                logger.error(String.format("Resetting connection due to no messages received: (%s: %s)", ipAddress, port));
                lastNonNullMessageTime = System.currentTimeMillis();
                closeSocket();
                openSocket();
            }
        }
        return connected;
        }

    public void logError(Exception e, String format) {

        logger.error(String.format(format, ipAddress, port, e));
    }
}
