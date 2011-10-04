package net.megapowers.accessed;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Accessed extends JavaPlugin {

    AccessedThread reloadedThread;

    @Override
    public void onDisable() {
        reloadedThread.running = false;
        Logger.getLogger("Minecraft").info("[Accessed] Waiting 2 seconds for the socket to close");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        try{
            reloadedThread = new AccessedThread(createServerSocket(InetAddress.getLocalHost(), 7040), this);
            reloadedThread.start();
        }catch(UnknownHostException e)
        {
            e.printStackTrace();
            return;
        }
        Logger.getLogger("Minecraft").info("[Accessed] Enabled and listening on port 7040");
    }

    private ServerSocket createServerSocket(InetAddress addr, int port) {
        ServerSocket serverSocket = null;
        try {
            //local-link address
            serverSocket = new ServerSocket();
            SocketAddress sockaddr = new InetSocketAddress(addr, port);
            serverSocket.bind(sockaddr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return serverSocket;
    }
}
