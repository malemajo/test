package Assignment3Starter;

import java.awt.Dimension;

import org.json.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements Assignment3Starter.OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	String currentMessage;
	Socket sock;
	OutputStream out;
	ObjectOutputStream os;
	BufferedReader bufferedReader;

	String host = "localhost";
	int port = 9000;

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui(String host, int port, String id) throws IOException {
		this.host = host;
		this.port = port;

		// ---- GUI things you do not have to change/touch them ----
		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);

		picturePanel.newGame(1);

		// ---- GUI things end ----

		open(); // open connection to server
		currentMessage = "{'type': 'start'}"; // sending a start request to the server
		try {
			os.writeObject(currentMessage); // send to server
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String string = this.bufferedReader.readLine(); // wait for answer
		JSONObject json = new JSONObject(string); // assumes answer is a JSON
		outputPanel.appendOutput(json.getString("value")); // write output value to output panel

		try {
			picturePanel.insertImage("img/hi.png", 0, 0); // hard coded to open this image -- image (not path) should be read from server message
		} catch (Exception e){
			System.out.println(e);
		}
		close(); // close connection to server
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Submit button handling
	 * 
	 * Change this to whatever you need, this is where the action happens. Tip outsource things to methods though so this method
	 * does not get too long
	 */
	@Override
	public void submitClicked() {
		try {
			open();
			System.out.println("submit clicked"); // server connection opened
			String input = outputPanel.getInputText(); // Pulls the input box text

				if (input.length() > 0) {
					outputPanel.appendOutput(input); // append input to the output panel
					outputPanel.setInputText(""); // clear input text box
				}

				JSONObject obj = new JSONObject();
				obj.put("type", "name");
				obj.put("value", input);
				try {
					os.writeObject(obj.toString());// sending the current message to the server
				} catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}

			try {
				System.out.println("Waiting on response");
				String string = this.bufferedReader.readLine();
				JSONObject json = new JSONObject(string);

				System.out.println("Got a response");
				System.out.println(json);

			} catch (Exception e) {
				e.printStackTrace();
			}

			outputPanel.setTask("read the requirements");
			outputPanel.setPoints(100);
			outputPanel.setInputText("");
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void allInClicked(){
		System.out.println("Handle that the user clicked all in button. Send all in request with answer to server");
		String input = outputPanel.getInputText();
		outputPanel.appendOutput(input);
		outputPanel.setInputText("");
	}


	public void open() throws UnknownHostException, IOException {
		this.sock = new Socket(host, port); // connect to host and socket on port 8888

		// get output channel
		this.out = sock.getOutputStream();
		// create an object output writer (Java only)
		this.os = new ObjectOutputStream(out);
		this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

	}
	
	public void close() {
		try {
			if (out != null)  out.close();
			if (bufferedReader != null)   bufferedReader.close(); 
			if (sock != null) sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// create the frame

		try { // hard coded you should make sure that this is taken from Gradle
			String host = "localhost";
			int port = 8888;


			ClientGui main = new ClientGui(host, port, args[0]);
			main.show(true);


		} catch (Exception e) {e.printStackTrace();}



	}
}
