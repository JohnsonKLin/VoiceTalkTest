package yumeko.voicetalktest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by suzuno on 13-7-15.
 */
public class Connection {

    int             iPort;

    DatagramSocket  mSendingSocket;
    InetAddress     mIP;

    public static Connection connect(String serverIPAddress,int port) throws SocketException, UnknownHostException {


        InetAddress ip = InetAddress.getByName(serverIPAddress);

        Connection connection  = new Connection();
        connection.mSendingSocket = new DatagramSocket();
        connection.mIP = ip;
        connection.iPort = port;

        return connection;
    }

    public void send(byte[] buffer) throws IOException {

        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        packet.setAddress(mIP);
        packet.setPort(iPort);
        mSendingSocket.send(packet);

    }

    public void close(){
        mSendingSocket.close();
    }
}
