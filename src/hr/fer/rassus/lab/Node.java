package hr.fer.rassus.lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node {

    private static final int BACKLOG = 10;
    private static final int NUMBER_OF_THREADS = 4;
    private static final String configurationFile = "treeConfig.txt";

    private int id;
    private InetAddress address;
    private int port;
    private List<NodeRepresentation> neighbours;
    private List<String> config;
    private AtomicBoolean treeCreated;
    Map<Integer,NodeRepresentation> treeParents;
    Map<Integer,List<NodeRepresentation>> treeChildren;

    private ServerSocket serverSocket;
    private final AtomicInteger activeConnections;
    private final ExecutorService executor;
    private final AtomicBoolean runningFlag;

    public Node(int id) throws IOException {
        this.id = id;

        neighbours = new ArrayList<>();
        treeCreated = new AtomicBoolean(false);
        treeParents = new HashMap<>();
        treeChildren = new HashMap<>();
        activeConnections = new AtomicInteger(0);
        runningFlag = new AtomicBoolean(false);
        executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        config = Files.readAllLines(Paths.get("./resources/" + configurationFile));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NodeRepresentation getParent(int id) {
        return treeParents.get(id);
    }

    public void setParent(int id, NodeRepresentation par) {
        treeParents.put(id, par);
    }


    public void init() throws UnknownHostException {
        List<String> followingNodes = new ArrayList<>();
        for(String s : config) {
            if(s.startsWith(String.valueOf(id) + "\t")) {
                String[] data = s.split("\t+");
                address = InetAddress.getByName(data[1]);
                port = Integer.parseInt(data[2]);

                String[] next = data[3].split(",");
                for(String c : next) {
                    followingNodes.add(c);
                }
                break;
            }

        }
        for(String s : config) {
            String[] data = s.split("\t+");
            if(followingNodes.contains(data[0])) {
                neighbours.add(new NodeRepresentation(Integer.parseInt(data[0]), InetAddress.getByName(data[1]),
                        Integer.parseInt(data[2])));
            }
        }
    }

    public void startup() {
        try {
            serverSocket = new ServerSocket(port, BACKLOG, address);
            serverSocket.setSoTimeout(500);
            runningFlag.set(true);
            System.out.println("Server is ready!");

            Thread work = new Thread(() -> {
                serverLoop();
            });
//			work.setDaemon(true);
            work.start();

        } catch (SocketException e1) {
            System.err.println("Exception caught when setting server socket timeout: " + e1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverLoop() {
        while (runningFlag.get()) {
            try {
                // listen for a connection to be made to server socket from a client
                // accept connection and create a new active socket which communicates with the
                // client
                Socket clientSocket = serverSocket.accept();/* ACCEPT */

                // execute a new request handler in a new thread
                Runnable worker = new ClientWorker(clientSocket, this, runningFlag, activeConnections);
                executor.execute(worker);
                // increment the number of active connections
                activeConnections.getAndIncrement();
            } catch (SocketTimeoutException ste) {
                // do nothing, check the runningFlag flag
            } catch (IOException e) {
                System.err.println("Exception caught when waiting for a connection: " + e);
            }
        }
    }


    public void createTree(int rootId) throws IOException, InterruptedException, ClassNotFoundException {
        List<NodeRepresentation> chi = new ArrayList<>();
        for(NodeRepresentation c : neighbours) {
            Socket clientSocket = null;
            clientSocket = new Socket(c.getAddress(), c.getPort(),
                    getAddress(), 0);

            Thread.sleep((long) (0 + Math.random()* 5000));

            Message message = new Message(rootId, "create", "search");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream.writeObject(message);



            Message received = (Message) objectInputStream.readObject();

            if(received.getContent().equals("accept")) {
                chi.add(c);
            }
            clientSocket.close();
        }
        treeChildren.put(rootId, chi);

    }

    public void sendMessage(Message message) throws InterruptedException, IOException {
        for(NodeRepresentation c : treeChildren.get(message.getId())) {
            Socket clientSocket = null;

            clientSocket = new Socket(c.getAddress(), c.getPort(),getAddress(), 0);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            Thread.sleep((long) (0 + Math.random()* 5000));
            objectOutputStream.writeObject(message);

            clientSocket.close(); // CLOSE client socket

        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isCreated() {
        return treeCreated.get();
    }

    public void setCreated(boolean value) {
        treeCreated.set(value);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        if(args.length != 1) {
            System.out.println("Invalid input");
            System.exit(1);
        }
        int ordNum = Integer.parseInt(args[0]);
        System.out.println("Čvor "+ ordNum);
        Node client = new Node(ordNum);
        client.init();
        client.startup();

        BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Type in \"create\" to start creating spanning tree>");
        while (true) {
            if (scanner.readLine().toLowerCase().equals("create")) {
                client.createTree(client.getId());
                break;
            }
        }
        while(!client.isCreated()) {
            Thread.sleep(1000);
        }
        System.out.println("Done creating tree nodes");
        while (true) {
            System.out.println("Send a message from the root node: ");
            client.sendMessage(new Message(client.getId(), "message", scanner.readLine()));

        }
    }



    public static class NodeRepresentation {

        private int id;
        private InetAddress address;
        private int port;

        public NodeRepresentation(int id, InetAddress address, int port) {
            this.id = id;
            this.address = address;
            this.port = port;
        }

        public NodeRepresentation(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

    }
}
