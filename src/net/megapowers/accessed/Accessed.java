package net.megapowers.accessed;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Accessed extends JavaPlugin {

    ArrayList<AccessedThread> reloadedThreads;
    static Config config = new Config();

    @Override
    public void onDisable() {
        for (AccessedThread reloadedThread : reloadedThreads) {
            reloadedThread.running = false;
        }
        Logger.getLogger("Minecraft").info("[Accessed] Waiting 2 seconds for the socket to close");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onEnable() {

        List ips = (List) config.getConfig("ip-white-list");
        AccessedThread reloadedThread = null;
        for (Object ip : ips) {
            String raw[] = ((String) ip).split("@");
            Integer port = Integer.getInteger(raw[1], 7040);
            try {
                InetAddress addr = InetAddress.getByName(raw[0]);
                reloadedThread = new AccessedThread(createServerSocket(addr, port), this);
            } catch (UnknownHostException ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
                continue;
            } catch (IOException ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
                continue;
            }
            reloadedThreads.add(reloadedThread);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Accessed] Enabled and listening on {0}@{1}", new Object[]{raw[0], port});
        }

        try {
            reloadedThreads.add(new AccessedThread(createServerSocket(InetAddress.getLocalHost(), 7040), this));

            Logger.getLogger("Minecraft").log(Level.INFO, "[Accessed] Enabled and listening on port {0}@7040", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
        }
    }

    private ServerSocket createServerSocket(InetAddress addr, int port) throws UnknownHostException, IOException {
        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket();
        SocketAddress sockaddr = new InetSocketAddress(addr, port);
        serverSocket.bind(sockaddr);
        return serverSocket;
    }
}
