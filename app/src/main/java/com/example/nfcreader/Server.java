package com.example.nfcreader;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;

public class Server {
    MainActivity activity;
    ServerSocket serverSocket;
    String message = "";
    static final int socketServerPORT = 1337;

    boolean nfcUnlock = false;

    public Server(MainActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void nfcUnlockNotify() {
        nfcUnlock = true;
    }

    private class SocketServerThread extends Thread {
        int count = 0;

        SocketServerReplyThread socketServerReplyThread;

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT, 0, InetAddress.getLoopbackAddress());

                while (true) {
                    // block the call until connection is created and return Socket object
                    Socket socket = serverSocket.accept();
                    count++;
                    message += "#" + count + " from "
                            + socket.getInetAddress() + ":"
                            + socket.getPort() + "\n";

                    PrintStream printStream = new PrintStream(socket.getOutputStream());

                    socketServerReplyThread = new SocketServerReplyThread(printStream);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     }

    private class SocketServerReplyThread extends Thread {

        PrintStream printStream;

        public SocketServerReplyThread(PrintStream printStream) {
            this.printStream = printStream;
        }

        @Override
        public void run() {
            while (true) {
                while(!nfcUnlock) {
                    // Block and do nothing while we wait
                }

                // Send unlock signal to Pi
                printStream.print(true);
                Log.i("PI SIGNAL", "Sending unlock signal to Pi");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.txtServer.setText("Sent unlock signal to Pi");
                    }
                });
                nfcUnlock = false;
            }
        }
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}