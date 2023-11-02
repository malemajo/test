package Assignment3Starter;

import java.net.*;
import java.io.*;
import org.json.*;
import java.util.Random;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
    static JSONObject hints;

    static {
        try {
            hints = readHints();
        } catch (FileNotFoundException e) {
            hints = new JSONObject();
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        Socket sock;
        try {
            //open socket
            ServerSocket serv = new ServerSocket(8888, 1); // hard coded you should make sure that this is taken from Gradle
            System.out.println("Server ready for connetion");

            String name = "";

            // This is just a very simpe start with the project that establishes a basic client server connection and asks for a name
            // You can make any changes you like
            while (true) {
                sock = serv.accept(); // blocking wait

                // setup the object reading channel
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                OutputStream out = sock.getOutputStream();
                String s = (String) in.readObject();
                JSONObject json = new JSONObject(s); // assume message is json
                JSONObject obj = new JSONObject(); // resp object


                if (json.getString("type").equals("start")) { // start request was received and we ask for a name
                    System.out.println("New connection");
                    obj.put("type", "hello");
                    obj.put("value", "Hello, please tell me your name.");
                    obj = sendImg("img/hi.png", obj);

                } else if (json.getString("type").equals("name")) { // name is received
                    System.out.println("- Got a name");
                    name = json.getString("value");
                    obj.put("type", "hello");
                    obj.put("value", "Hello " + name + ", please chose a category animals (a), cities (c), or leader board (l)"); // menu options send
                } else { // if the request is not recognized.
                    System.out.println("not sure what you meant");
                    obj.put("type", "error");
                    obj.put("value", "unknown request");
                }
                PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
                outWrite.println(obj.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that gets a random filename from a file in a specific directory (e.g. img/animals).
     *
     * @param f which specifies the name of the directory
     * @return string of the image file
     */
    public static String getImage(File f) throws Exception {
        String[] list = f.list();

        Random rand = new Random(); //instance of random class
        int int_random = rand.nextInt(list.length - 1);

        String image = list[int_random + 1];
        System.out.println(image);

        if (image.equals(".DS_Store")) { // since Mac always has that
            image = getImage(f);
        }
        return image;
    }

    /**
     * Method that reads the hint list and returns it as JSONObject
     *
     * @return JSONObject including all the hints for the current game
     */
    public static JSONObject readHints() throws FileNotFoundException {
        FileInputStream in = new FileInputStream("img/hints.txt");
        JSONObject obj = new JSONObject(new JSONTokener(in));
        return obj;
    }

    /**
     * In my implementation this method gets a specific file name, opens it, manipulates it to be send over the network
     * and adds that manipulated image to the given obj which is basically my response to the client. You can do it differently of course
     *
     * @param filename with the image to open
     * @param obj      the current response that the server is creating to be send back to the client
     * @return json object that will be sent back to the client which includes the image
     */
    public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
        File file = new File(filename);

        if (file.exists()) {
            // import image
            // I did not use the Advanced Custom protocol
            // I read in the image and translated it into basically into a string and send it back to the client where I then decoded again
            obj.put("image", "Pretend I am this image: " + filename);
        }
        return obj;
    }
}
