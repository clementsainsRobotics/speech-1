
/**
 * The file server implements a simple protocol to handle requiests for
 * files. Agents may require entire files to be transferred from the
 * control of one agent to another. The FileServer is initialized with
 * the name of such a file. After that a request to transforer the
 * file is handled in the run () function. 
 */

package com.jaivox.protocol;

import java.io.*;
import java.net.*;

public class FileServer extends Thread {
	String fileName;
	String serverHost;
	int listenPort;
	ServerSocket server;
	
	static final int packetSize = 1024;
	// later add time out to socket so that we have chance to turn off
	// boolean turnOff = false;

	/**
	 * Creates the FileServer. The port to use for requests and the
	 * specific file to be served are specified when the server is
	 * created. The run method of this thread returns after the file
	 * is served.
	 @param file to be served
	 @param port for socket to receive request
	 */
	
	public FileServer (String file, int port) {
		fileName = file;
		listenPort = port;
		serverHost = "localhost";
		try {
			// normally create the server with port 0, it
			// will be changed to an available port
			server = new ServerSocket (port);
			listenPort = server.getLocalPort ();
			serverHost = server.getInetAddress ().getHostName ();
			Debug ("fileServer:", serverHost+" listening on "+listenPort);
		}
		catch (Exception e) {
			Debug ("fileServer:fileServer", e.toString ());
			e.printStackTrace ();
		}
		start ();
	}

	void Debug (String id, String s) {
		System.out.println ("[FileServer]"+id+" "+s);
	}

	/**
	 * The run is terminated after a file is served. The file to be
	 * served is determined when this class is created. Request for the
	 * file is received through the socket connection of the server.
	 */
	
	public void run () {
		// blocks until interrupted
		try {
			Socket dataLink = server.accept ();
			// open the file and sends it through this port
			FileInputStream in = new FileInputStream (fileName);
			OutputStream out = dataLink.getOutputStream ();
			byte b[] = new byte [packetSize];
			while (true) {
				int bytesRead = in.read(b);
				if (bytesRead < 0) break;
				out.write (b, 0, bytesRead);
			}
			in.close ();
			out.close ();
			dataLink.close ();
			server.close ();
			return;
		}
		catch (Exception e) {
			Debug ("fileServer:run", e.toString ());
			e.printStackTrace ();
		}
	}

	public String getFileName () {
		return fileName;
	}

	public void setFileName (String fileName) {
		this.fileName = fileName;
	}

	public String getServerHost () {
		return serverHost;
	}

	public void setServerHost (String serverHost) {
		this.serverHost = serverHost;
	}

	public int getListenPort () {
		return listenPort;
	}

	public void setListenPort (int listenPort) {
		this.listenPort = listenPort;
	}

	public ServerSocket getServer () {
		return server;
	}

	public void setServer (ServerSocket server) {
		this.server = server;
	}

}
