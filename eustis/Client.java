import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
//import java.nio.file.Paths;

public class Client {
    private final int BUFFER_SIZE = 4096;
    private Socket connection;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;
    private int bytes;
    private static boolean write=false;
    private static int startByte=0;
    private static int endByte=0;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private FileInputStream fileIn;

    public String toBinary(String text) {
        StringBuilder sb = new StringBuilder();

        for (char character : text.toCharArray()) {
            sb.append(Integer.toBinaryString(character) + "\n");
        }

        return sb.toString();

    }

    public Client(String host, int port, String filename) {
        try {
            connection = new Socket(host, port);


            socketIn = new DataInputStream(connection.getInputStream()); // Read data from server
            socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to server

            socketOut.writeUTF(filename); // Write filename to server
            socketOut.writeInt(startByte);
            socketOut.writeInt(endByte);
            socketOut.writeBoolean(write);
//            System.out.println("Start byte: "+startByte+" end byte : "+endByte);

//          Write file contents to client
            if(write){
                fileIn = new FileInputStream(filename);
                while (true) {
                    bytes = fileIn.read(buffer, 0, BUFFER_SIZE); // Read from file
//                    System.out.print(new String(buffer).trim().getBytes());
                    if (bytes <= 0) break; // Check for end of file
                    socketOut.write(buffer); // Write bytes to socket
                }
            }
            
//             Read file contents from server
            if(!write){
                String[] names = filename.split("\\.");
                String binary = names[0]+".binary";
                File file = new File(filename);
                File bin = new File(binary);
                String ifErr="";
                if (!file.exists() || !file.isFile()){
                    file.createNewFile();
                    bin.createNewFile();
//                    Path path = Paths.get(filename);
                    OutputStream outStream = new FileOutputStream(file);
                    OutputStream outBinary = new FileOutputStream(bin);
                    while (true) {
                        bytes = socketIn.read(buffer, 0, BUFFER_SIZE); // Read from socket
                        if (bytes <= 0) break; // Check for end of file
//                                System.out.println(toBinary(new String(buffer, StandardCharsets.UTF_8).trim()));
                        ifErr = new String(buffer, StandardCharsets.UTF_8).trim().split(" ")[0];
                        if(ifErr.equals("Error")){
                            System.out.println(new String(buffer, StandardCharsets.UTF_8).trim());
                            break;
                        }
                        outStream.write(new String(buffer, StandardCharsets.UTF_8).trim().getBytes());
                        outBinary.write(toBinary(new String(buffer, StandardCharsets.UTF_8).trim()).getBytes());
                    }
                    outStream.close();
                    outBinary.close();
                }
            }
            connection.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
    }

    public static void main(String[] args) {
        if(args.length<=1){
            System.out.println("Please provide minimum required arguments");
            System.out.println("For example: client.java localhost file.txt =>this is for downloading file.txt from server ");
            System.out.println("For example: client.java localhost [-w] file.txt =>this is for uploading file.txt to server ");
            System.out.println("For example: client.java localhost file.txt -s 100 -e 1000");
            System.out.println("This is for reading a chunk from server where -s mean start byte and -e mean end byte");
        }
        String[] ar =args;
        String host = "";
        String fileName ="";
        if(args.length>0){
            for (String arg : args)
            {
                if (arg.equals("[-w]")){
                    write=true;
                }
                if (arg.equals("-s")){
                    for(int i=0;i<ar.length;i++){
                        if(ar[i].equals("-s")){
                            startByte = Integer.parseInt(ar[i+1]);
                        }
                    }
                }
                if (arg.equals("-e")){
                    for(int i=0;i<ar.length;i++){
                        if(ar[i].equals("-e")){
                            endByte = Integer.parseInt(ar[i+1]);
                        }
                    }
                }
            }
        }
        if(args[1].equals("[-w]")){
            fileName = args[2];
        }else {
            fileName = args[1];
        }
        host = args[0];
        host = host.replace('<',' ');
        host = host.replace('>',' ');
        host = host.trim();
        fileName = fileName.replace('<',' ');
        fileName = fileName.replace('>',' ');
        fileName = fileName.trim();
        if(host.equals("localhost")){
            Client client = new Client("127.0.0.1", 5000, fileName);
        }else {
            System.out.println("Please provide localhost as server name");
        }
    }
}
