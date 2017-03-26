import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.sql.*;
import java.net.InetAddress;
/**
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 *
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */



public class ManhuntServer {


   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost";

    //Database credentials
    static final String USER = "root";   //the user name;
    static final String PASS = "root";   //the password;
    static Connection conn = null;
    static Statement stmt = null;
    static String sql; 

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 9898.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The manhunt server is running.");
	try{
	    Class.forName("com.mysql.jdbc.Driver");
	    conn = DriverManager.getConnection(DB_URL, USER, PASS);
	    stmt = conn.createStatement();
	    sql = "use manhunt;";
	     stmt.executeUpdate(sql);
	}catch (Exception e){
	    
	}
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new Capitalizer(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Capitalizer extends Thread {
        private Socket socket;
        private int clientNumber;
        private String user;
        private String lKey;

        public Capitalizer(Socket socket, int clientNumber) {
            this.socket = socket;
            //sockets.add(socket);
            //log(Integer.toString(sockets.size()));
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line starting with #exit to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
		    String content[] = input.split("#");
                    if ( input.startsWith("#exit") ) {
                      	/*#exit#uid*/
			try{
			    sql ="delete from users where uid = '"+content[2]+ "';";
			    stmt.executeUpdate(sql);
			}catch(Exception e){
			    e.printStackTrace();
			}
			    out.println("");
                        break;
			
                    } else if ( input.startsWith("#init") ) {
			/*format #init#lcode#name#uid#TLLAT#TLLONG#TRLAT#TRLONG#BLLAT#BLLONG#BRLAT#BRLONG#tname*/
			try{
			    sql = "insert into lobby values(" + "'" + content[2] + "','" + content[3] + "','" +
			         content[5] + "','" + content[6] + "','" + content[7] + "','" +
				content[8] + "','" + content[9] + "','" + content[10] + "','" + content[11] + "','" +
				content[12] + "');";
			    stmt.executeUpdate(sql);

			    sql = "insert into users values ('" + content[4] + "','" + socket.getPort() + "','" + socket.getInetAddress() + "');";
			    
			    stmt.executeUpdate(sql);
			    sql = "insert into memberof values ('" + content[2] + "','" + content[4] + "');";
			    stmt.executeUpdate(sql);

			     sql = "insert into team values ('" + content[13] + "','" + content[2] + "','" + content[4] + "');";
			    stmt.executeUpdate(sql);
			    
			}catch (Exception e){
			    e.printStackTrace();
			}
			   
			 
                        out.println("Creating a Lobby");
                    } else if ( input.startsWith("#join") ) {
			/*#join#lcode#lname#uid#tname*/
			try {
			   Statement s = conn.createStatement ();
			   System.out.println("SELECT * from lobby WHERE lcode = '" + content[2] + " 'AND name = '" + content[3] + "';");
			   s.executeQuery ( "SELECT * from lobby WHERE lcode = '" + content[2] + "' AND name = '" + content[3] + "';");
			   int count = 0;
			   ResultSet rs = s.getResultSet ();
			   while (rs.next ())
			       {
				   ++count;
			       }
			   if(count != 0){
			       sql = "insert into users values ('" + content[4] + "','" + socket.getPort() + "','" + socket.getInetAddress() + "');";
			       stmt.executeUpdate(sql);
			       sql = "insert into memberof values('" + content[2]+ "','" + content[4] + "');";
			       stmt.executeUpdate(sql);
			       sql = "insert into team values ('" + content[5] + "','" + content[2] + "','" + content[4] + "');";
			    stmt.executeUpdate(sql);
			       out.println("#succ");
			   }else{
			       out.println("#fail");
			   }
			}catch (Exception e){
			    e.printStackTrace();
			}
                        out.println("Joining a Lobby");
                    } else if ( input.startsWith("#new") ) {
                        out.println("Creating Team");
                    } else if ( input.startsWith("#refresh") ) {
			/*#refresh#lcode#t1name#t2name*/
			String res = "";
			try{
			    Statement s = conn.createStatement ();
			    // System.out.println("SELECT uid FROM team WHERE lcode = '" + content[2] + "' AND tname = '" + content[3] + "' ;");
			    s.executeQuery("SELECT uid FROM team WHERE lcode = '" + content[2] + "' AND tname = '" + content[3] + "' ;");
			    res = "#" + content[3];
			    ResultSet rs = s.getResultSet();
			  
			    while(rs.next()){
				res += "," + rs.getString("uid");
				System.out.println(res);
			    }
			    res += "#" + content[4];
			    s = conn.createStatement ();
			    s.executeQuery("SELECT uid FROM team WHERE lcode = '" + content[2] + "' AND tname = '" + content[4] + "' ;");
			    
			    rs = s.getResultSet();
			    while(rs.next()){
				res += "," + rs.getString("uid");
			    }
			}catch (Exception e){
			    e.printStackTrace();
			}
                        out.println(res);
                    } else if ( input.startsWith("#display") ) {
                        out.println("Displaying teamnames");
                    } else if ( input.startsWith("#start") ) {
			 ArrayList<Socket> members = new ArrayList<Socket>();
			try{
			   
			    /*#start#lcode*/
			    Statement s = conn.createStatement ();
			    // System.out.println("SELECT uid FROM team WHERE lcode = '" + content[2] + "' AND tname = '" + content[3] + "' ;");
			    s.executeQuery("SELECT port, addr FROM users u, memberof m  WHERE m.uid= u.uid AND lcode = '" + content[2] + "';");
			    ResultSet rs = s.getResultSet();
			    
			    while(rs.next()){
				members.add(new Socket(InetAddress.getByName(rs.getString("addr").substring(1)), rs.getInt("port")));
			    }
			    System.out.println(Arrays.toString(members.toArray()));
			}catch(Exception e){
			     e.printStackTrace();
			}
			    sendToAll("#start",members);
			
                        out.println("Starting a match");
                    } else if ( input.startsWith("#loc") ) {
                        out.println("You have a location");
                    }else if ( input.startsWith("#end") ) {
			/*#end#lcode*/
			try{
			    sql ="delete from lobby where lcode = '"+content[2]+ "';";
			    stmt.executeUpdate(sql);
			}catch(Exception e){
			    e.printStackTrace();
			}
                        out.println("end");
                    } else {
                        out.println("#error#");
                    }
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
		    /*  for ( int i = 0; i < sockets.size(); i++ ) {
                        if ( socket.equals(sockets.get(i)) ) {
                            sockets.remove(i);
                        }
			}*/
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

        public void sendToAll ( String message , ArrayList<Socket> sockets) {
            try {
                PrintWriter out;
                for ( Socket s : sockets ) {
                    out = new PrintWriter(s.getOutputStream(), true);
                    out.println(message);
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }

}
