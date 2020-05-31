package hr.fer.rassus.lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import hr.fer.rassus.lab.Node.NodeRepresentation;

public class ClientWorker implements Runnable {

    private final Socket clientSocket;
    private final AtomicBoolean isRunning;
    private final AtomicInteger activeConnections;
    private Node node;

    public ClientWorker(Socket clientSocket, Node node, AtomicBoolean isRunning, AtomicInteger activeConnections) {
        this.clientSocket = clientSocket;
        this.isRunning = isRunning;
        this.activeConnections = activeConnections;
        this.node = node;
    }

    @Override
    public void run() {
        try (ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());){

            Message receivedString;

            while ((receivedString = (Message) inFromClient.readObject()) != null/* READ */) {
                if (!(receivedString.getId() == node.getId()))
                    System.out.println("Server received: " + receivedString.toString());
                if(receivedString.getType().equals("create")) {
                    int rootId = receivedString.getId();
                    if(node.getParent(rootId) == null && node.getId() != rootId) {
                        node.setParent(rootId, new NodeRepresentation(clientSocket.getInetAddress(), clientSocket.getLocalPort()));
                        outToClient.writeObject(new Message(rootId, receivedString.getType(), "accept"));
                        node.createTree(rootId);
                    }
                    else {
                        if(node.getId() == rootId) {
                            node.setCreated(true);
                        }
                        outToClient.writeObject(new Message(rootId, receivedString.getType(), "reject"));
                    }
                }
                else {
                    node.sendMessage(receivedString);
                }

            }
            clientSocket.close();
            activeConnections.getAndDecrement();
        } catch (IOException ex) {
//			System.err.println("Exception caught when trying to read or send data: " + ex);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}
