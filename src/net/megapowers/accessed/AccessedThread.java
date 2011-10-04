package net.megapowers.accessed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class AccessedThread extends Thread {

    ServerSocket serverSocket;
    Socket clientSocket;
    PluginManager pluginManager;
    Server server;
    Plugin plugin;
    Configuration config;
    public boolean running;
    BufferedReader reader;
    PrintWriter writer;

    public AccessedThread(ServerSocket serverSocket, JavaPlugin plugin) {
        this.serverSocket = serverSocket;
        try {
            serverSocket.setSoTimeout(2000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.plugin = plugin;
        this.server = plugin.getServer();
        this.config = plugin.getConfiguration();
        this.pluginManager = server.getPluginManager();

        running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                clientSocket = serverSocket.accept();

                //TODO: make ip firewall system properly.
                /*String hostAddress = clientSocket.getInetAddress().getHostAddress();
                if (!hostAddress.equals("127.0.0.1") && !hostAddress.equals("0:0:0:0:0:0:0:1")) {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Accessed] {0} tried accessing, but was denied.", hostAddress);
                clientSocket.close();
                continue;
                }*/
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                String[] inputLineSplit;


                while ((inputLine = reader.readLine()) != null) {
                    inputLineSplit = inputLine.split(" ");
                    Callable c = null;
                    if (inputLineSplit[0].equalsIgnoreCase("reload")) {

                        c = new InputCallable<String, String>(inputLineSplit[1]) {

                            @Override
                            public String call() throws Exception {
                                try {
                                    Plugin pluginToReload = pluginManager.getPlugin(input);
                                    pluginManager.disablePlugin(pluginToReload);
                                    pluginManager.enablePlugin(pluginToReload);
                                    return "done";
                                } catch (Exception ex) {
                                    return "failed";
                                }
                            }
                        };
                    } else if (inputLineSplit[0].equalsIgnoreCase("reloadall")) {
                        if (reloadAll()) {
                            writer.println("done");
                        } else {
                            writer.println("failed");
                        }
                    } else if (inputLineSplit[0].equalsIgnoreCase("command")) {
                        String commandToSend = inputLine.substring(inputLineSplit[0].length() + 1);
                        if (commandToSend.equals("reload")) {
                            if (reloadAll()) {
                                writer.println("done");
                            } else {
                                writer.println("failed");
                            }
                        } else {
                            Logger.getLogger("Minecraft").log(Level.INFO, "[Accessed] Dispatching command \"{0}\" to console.", commandToSend);

                            c = new InputCallable<String, String>(commandToSend) {

                                @Override
                                public String call() throws Exception {
                                    try {
                                        server.dispatchCommand(server.getConsoleSender(), input);
                                        return "done";
                                    } catch (Exception ex) {
                                        return "failed";
                                    }
                                }
                            };
                        }

                    } else if (inputLineSplit[0].equalsIgnoreCase("list")) {
                        c = new Callable<String>() {

                            @Override
                            public String call() throws Exception {
                                try {
                                    Player[] in = server.getOnlinePlayers();
                                    String out = new String();
                                    int max = in.length;
                                    //for each player
                                    for (int i = 0; i < max; i++) {
                                        //append name to output
                                        out += in[i].getName();
                                        //formatting control
                                        if (i < max - 1) {
                                            out += ", ";
                                        }
                                    }
                                    //send output
                                    return out;
                                } catch (Exception ex) {
                                }
                                return "";
                            }
                        };
                    } else {
                        writer.println("unknown");
                    }
                    if (c != null) {
                        Future<String> returnFuture = server.getScheduler().callSyncMethod(plugin, c);
                        try {
                            writer.println(returnFuture.get());
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AccessedThread.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(AccessedThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
        }
    }

    boolean reloadAll() {

        Future<Boolean> returnFuture = server.getScheduler().callSyncMethod(plugin, new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                try {
                    for (Plugin plugin : pluginManager.getPlugins()) {
                        if (plugin.getDescription().getName().equals("Accessed")) {
                            continue;
                        }
                        pluginManager.disablePlugin(plugin);
                    }
                    for (Plugin plugin : pluginManager.getPlugins()) {
                        if (plugin.getDescription().getName().equals("Accessed")) {
                            continue;
                        }
                        pluginManager.enablePlugin(plugin);
                    }
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });
        try {
            return returnFuture.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(AccessedThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AccessedThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        //everything failed.
        return false;

    }
}
