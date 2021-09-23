import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Server {
    private final int BUFFER_SIZE = 4096;
    private Socket connection;
    private ServerSocket socket;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;
    private FileInputStream fileIn;
    private String filename;
    private int bytes;
    private int percent=0;
    private int tenPercent;
    private int total=0;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int fileSize = 0;
    private int startByte = 0;
    private int endByte = 0;
    private boolean write=false;
    private byte[] fileNotFound = "Error : Requested File Does Not Exist At Server".getBytes();
    private byte[] wrongByteRange = "Error : Invalid Byte Range".getBytes();
    private byte[] fileFound = "Error : File Already Exist at server".getBytes();

    public String toBinary(String text) {
        StringBuilder sb = new StringBuilder();

        for (char character : text.toCharArray()) {
            sb.append(Integer.toBinaryString(character) + "\n");
        }

        return sb.toString();

    }

    public Server(int port,int DEBUG) {
        try {
            socket = new ServerSocket(port);
            // Wait for connection and process it
            while (true) {
                try {
                    connection = socket.accept(); // Block for connection request
                    String client_ip=connection.getInetAddress().getHostAddress();

                    socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
                    socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client

                    filename = socketIn.readUTF(); // Read filename from client
                    startByte = socketIn.readInt();//Read Start Byte from client
                    endByte = socketIn.readInt();//Read End Byte from client
                    write = socketIn.readBoolean();//check if user want to write to server
//                    System.out.println("Start byte: "+startByte+" end byte : "+endByte);
                    File file = new File(filename);
                    if(write){
                        if (!file.exists() || !file.isFile()){
                            file.createNewFile();
//                            Path path = Paths.get(filename);
                            OutputStream outStream = new FileOutputStream(file);
                            while (true) {
                                bytes = socketIn.read(buffer, 0, BUFFER_SIZE); // Read from socket
                                if (bytes <= 0) break; // Check for end of file
//                                System.out.println(toBinary(new String(buffer, StandardCharsets.UTF_8).trim()));
                                outStream.write(new String(buffer, StandardCharsets.UTF_8).trim().getBytes());
                            }
                            outStream.close();
                        }else {
                            socketOut.write(fileFound);
                        }
                    }
                    if(!write){
                        fileIn = new FileInputStream(filename);
                        if(DEBUG==1){
                            System.out.println("Sending <"+filename.toString()+"> "+"to <"+client_ip+">");
                        }
                        if (!file.exists() || !file.isFile()){
                            socketOut.write(fileNotFound);
                        }else {
                            fileSize = (int)file.length();
//                            System.out.println("File Size : "+fileSize);
                        }
                        if(startByte==0 && endByte==0){ //no byte range given
                            total=0;
                            tenPercent = (int) Math.ceil(fileSize/10);
                            byte[] buf = new byte[tenPercent];
                            // Write file contents to client
                            while (true) {
                                bytes = fileIn.read(buf, 0, tenPercent); // Read from file
                                if (bytes <= 0) break; // Check for end of file
                                socketOut.write(buf); // Write bytes to socket
//                                total = total+BUFFER_SIZE;
                                if(DEBUG==1 && percent<=90){
                                    percent = percent+10;
                                    System.out.println("Sent "+percent+"% of <"+filename+">");
//                                    total=0;
                                }

                            }
                            if(DEBUG==1){
                                System.out.println("Finished Sending <"+filename+"> to <"+client_ip+">");
                            }
                        }else if(startByte>0 && endByte<=fileSize && startByte<endByte){// given byte range
                            total=0;
                            percent=0;
                            bytes = fileIn.read(buffer, 0, endByte); // Read from file
                            byte[] buffer1 = Arrays.copyOfRange(buffer,startByte,endByte);
//                            System.out.println(buffer1.length);
//                            System.out.print(new String(buffer1, StandardCharsets.UTF_8).trim());
                            tenPercent = (int) Math.ceil((buffer1.length)/10);
                            while (buffer1.length>=total) {
                                byte[] buffer2 = Arrays.copyOfRange(buffer1,total,tenPercent+total);
                                socketOut.write(buffer2); // Write bytes to socket
                                total = total+tenPercent;
                                if(DEBUG==1 && percent<=90){
                                    percent = percent+10;
                                    System.out.println("Sent "+percent+"% of <"+filename+">");
                                }

                            }
                            if(DEBUG==1){
                                System.out.println("Finished Sending <"+filename+"> to <"+client_ip+">");
                            }
                        }else { //wrong byte range given
                            socketOut.write(wrongByteRange);
                        }
                    }
                }
                catch (FileNotFoundException ex) {
                    socketOut.write(fileNotFound);
                }
                catch (Exception ex) {
                    System.out.println("Error: " + ex);
                } finally {
                    // Clean up socket and file streams
                    if (connection != null) {
                        connection.close();
                    }

                    if (fileIn != null) {
                        fileIn.close();
                    }
                }
            }
        } catch (IOException i) {
            System.out.println("Error: " + i);
        }
    }

    public static void main(String[] args) {
        int DEBUG = 1;
        if(args.length>0){
            for (String arg : args)
            {
                if (arg.equals("DEBUG=1")) DEBUG=1;
            }
        }
        Server server = new Server(5000,DEBUG);
    }
}
