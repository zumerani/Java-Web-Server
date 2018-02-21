import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class WebServer {
    public static void main (String[] args) {
        
        /* Check if number of arguments is four - necessary to run/start server. */
        if (args.length != 4) {
            System.out.println("Correct command: java WebServer -document_root / -port <port_number>");
        }
        
        int port = Integer.parseInt(args[3]); // Grab the port number to listen to.
        
        try {
            ServerSocket socket = new ServerSocket(port);
            while (true) {
                Socket s = socket.accept();
                HttpRequest request = new HttpRequest(s, args[1]);
                Thread thread = new Thread(request); // Handle multi-threading.
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something went wrong in creating the socket.");
        }
    }
}

/* Implement Runnable because the following class will have instances run by a thread. */
class HttpRequest implements Runnable {
    Socket socket;
    String path;
    
    public HttpRequest (Socket socket, String path) {
        this.socket = socket;
        this.path = path;
    }
    
    public void run() {
        try {
            handleRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void handleRequest() throws Exception {
        InputStreamReader socketInputStream = new InputStreamReader(socket.getInputStream());
        BufferedReader socketInput = new BufferedReader(socketInputStream);
        
        BufferedOutputStream socketOutput = new BufferedOutputStream(socket.getOutputStream());
        PrintStream socketOutputStream = new PrintStream(socketOutput);
        
        String command = socketInput.readLine();
        System.out.println(command);
        
        String fileName = "";
        
        StringTokenizer tokenizedCommand = new StringTokenizer(command);
        File file = null;
        
        try {
            /* Check to see if we can grab the file, if not we throw 'FileNotFoundException'. */
            if (tokenizedCommand.hasMoreElements() && tokenizedCommand.nextToken().equals("GET")) {
                
                String fileRequested = tokenizedCommand.nextToken(); // This is the file after the "GET" in the request.
                
                /*
                 If the URL is entered, simply return index.html, otherwise, return the name of the file being
                 requested.
                 */
                if (fileRequested.equals("/")) {
                    fileName = "index.html";
                } else {
                    fileName = fileRequested;
                }
                
                /* Check to see if document_root is our local dir, or non-local. */
                if (this.path.equals("/")) {
                    /* Add a '.' only to a file path (non-index.html) so that when we check whether the file exists
                     it is a valid file.
                     */
                    if (!(fileName.equals("index.html"))) {
                        fileName = "." + fileName;
                    }
                } else {
                    /*
                     If this condition passes, we remove the first '/' from the requested file because the path (document_root)
                     will already have a '/' at the end.
                     */
                    if (!(fileName.equals("index.html"))) {
                        fileName = fileName.substring(1);
                    }
                    fileName = this.path + fileName;
                }
                
                file = new File(fileName);
                
                if (!(file.exists())) {
                    socketOutputStream.print("HTTP/1.0 404 Not Found\r\n" + "Content-type: text/html\r\n\r\n" +
                                             "<html><head></head><body>HTTP/1.0 404 Not Found<br>" + fileName + " not found</body></html>\n");
                    socketOutputStream.close();
                    socketInputStream.close();
                    socket.close();
                }
                
                /* Check to see if we can read the file. Check to see if we can access the file (permissions). */
                if (!(file.canRead())) {
                    socketOutputStream.print("HTTP/1.0 403 Forbidden\n " + "Bad File: /" + fileName + "/");
                    socketOutputStream.close();
                    return;
                }
                
                FileInputStream fileInputStream = new FileInputStream(file);
                
                /* Below, we determine the content of the file. */
                String content = "";
                if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
                    content = "text/html";
                }
                else if (fileName.endsWith(".png")) {
                    content = "image/png";
                }
                else if (fileName.endsWith(".gif")) {
                    content = "image/gif";
                }
                else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
                    content = "image/jpeg";
                }
                else if (fileName.endsWith(".css") || fileName.endsWith(".min.css")) {
                    content = "text/css";
                }
                else if (fileName.endsWith(".js")  || fileName.endsWith(".min.js")) {
                    content = "text/javascript";
                }
                else {
                    content = "text/plain";
                }
                
                /* Once we hit this point, the file does in fact exist, so we return 200. */
                socketOutputStream.print("HTTP/1.0 200 OK\r\n " + "Content-type: " + content + "\r\n\r\n");
                
                byte buffer[] = new byte[4096];
                int num;
                
                /* Write the contents of the file into a buffer. */
                while ((num = fileInputStream.read(buffer)) > 0) {
                    socketOutputStream.write(buffer, 0, num);
                }
                
                socketOutputStream.close();
                socket.close();
                return;
                
            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            socketOutputStream.print("HTTP/1.0 404 Not Found\r\n" + "Content-type: text/html\r\n\r\n" +
                                     "<html><head></head><body>HTTP/1.0 404 Not Found<br>" + fileName + " not found</body></html>\n");
            socketOutputStream.close();
            socketInputStream.close();
            socket.close();
        }
    }
}
