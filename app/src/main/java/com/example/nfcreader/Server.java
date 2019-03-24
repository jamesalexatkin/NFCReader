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
import java.util.Optional;

public class Server {
    MainActivity activity;
    ServerSocket serverSocket;
    static final int socketServerPORT = 1337;

    private Optional<String> bookingId = Optional.empty();

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

    public void nfcUnlockNotify(String bookingId) {
        this.bookingId = Optional.of(bookingId);
    }

    private class SocketServerThread extends Thread {

//         socketServerReplyThread;

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT, 0, InetAddress.getLoopbackAddress());

                while (true) {
                    // block the call until connection is created and return Socket object
                    Socket socket = serverSocket.accept();

                    setTextViewServerText("Connection established");

                    PrintStream printStream = new PrintStream(socket.getOutputStream());

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(printStream);
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
                while (!bookingId.isPresent()) {
                    // Block and do nothing while we wait
                }
                sendUnlockSignal();
            }
        }

        private void sendUnlockSignal() {
            String room = bookingId.get();
            printStream.print(room.length());
            printStream.print(room);

            Log.i("PI SIGNAL", "Sending unlock signal to Pi");
            setTextViewServerText("Sending unlock signal to Pi");

            bookingId = Optional.empty();
        }
    }

    private void setTextViewServerText(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.txtServer.setText(text);
            }
        });
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