package com.sdis1516t1g02.testapp;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

    /**
     * Initiates the TestApp.
     * @param args the arguments passed by the user
     */
    public static void main(String[] args) {
        try {
            if (args.length < 3 || args.length > 4 || (args.length > 3 && (!args[1].equals("BACKUP") && !args[1].equals("BACKUPENH"))) ) {
                System.out.println("Numero de argumentos incorreto. ");
                return;
            }
            String peer_ap = args[0], sub_protocol = args[1], filename = args[2], repDegree = null, response = null;

            if (sub_protocol.equals("BACKUP") || sub_protocol.equals("BACKUPENH"))
                repDegree = args[3];

            Registry registry = null;
            registry = LocateRegistry.getRegistry(); // iniciar o rmi
            RMI_Interface rmiInterface = null;
            rmiInterface = (RMI_Interface) registry.lookup(peer_ap);

            switch(sub_protocol) {
                case "BACKUP":
                    response = rmiInterface.backup(filename, Integer.parseInt(repDegree), false);
                    System.out.println(response);
                    break;
                case "RESTORE":
                    response = rmiInterface.restore(filename, false);
                    System.out.println(response);
                    break;
                case "DELETE":
                    response = rmiInterface.delete(filename, false);
                    System.out.println(response);
                    break;
                case "RECLAIM":
                    response = rmiInterface.reclaim(Integer.parseInt(filename), false);
                    System.out.println(response);
                    break;
                case "BACKUPENH":
                    response = rmiInterface.backup(filename, Integer.parseInt(repDegree), true);
                    System.out.println(response);
                    break;
                case "RESTOREENH":
                    response = rmiInterface.restore(filename, true);
                    System.out.println(response);
                    break;
                case "DELETEENH":
                    response = rmiInterface.delete(filename, true);
                    System.out.println(response);
                    break;
                case "RECLAIMENH":
                    response = rmiInterface.reclaim(Integer.parseInt(filename), true);
                    System.out.println(response);
                    break;
                default:
                    System.out.println("Nome do protocolo incorreto");
                    break;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
