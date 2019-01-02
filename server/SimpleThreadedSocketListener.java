import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A simple server socket listener that listens to port number 8888, and prints
 * whatever received to the console. It starts a thread for each connection to
 * perform IO operations.
 */
public class SimpleThreadedSocketListener {

	ServerSocket server;
	int serverPort = 8888;

	// Constructor to allocate a ServerSocket listening at the given port.
	public SimpleThreadedSocketListener() {
		try {
			server = new ServerSocket(serverPort);
			System.out.println("ServerSocket: " + server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Start listening.
	private void listen() {
		while (true) { // run until you terminate the program
			try {
				// Wait for connection. Block until a connection is made.
				Socket socket = server.accept();
				System.out.println("Socket: " + socket);
				// Start a new thread for each client to perform block-IO operations.
				new ClientThread(socket).start();
			} catch (BindException e) {
				e.printStackTrace();
				break; // Port already in use
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new SimpleThreadedSocketListener().listen();
	}

	// Fork out a thread for each connected client to perform block-IO
	class ClientThread extends Thread {

		Socket socket;

		public ClientThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = socket.getInputStream();
				out = socket.getOutputStream();
				String passw = "";
				String serverout = "";
				BufferedReader rd = new BufferedReader(new InputStreamReader(in));
				String line;
				line = rd.readLine();
				switch (line.substring(0, 1)) {// 判断客户端请求选择返回内容
				case "1":// 登陆是连接到account账户进行验证，返回密码进行比对
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection con;
					Statement stmt;
					String use = line.substring(1);
					con = DriverManager.getConnection("jdbc:mysql://localhost:3306/account?serverTimezone=GMT%2B8",
							"root", "dxy19990915");
					stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("select * from accounts where user='" + use + "'");
					while (rs.next())
						passw = rs.getString("password");
					System.out.println(passw);
					serverout = passw;
					break;
				case "2":// 日历客户端向服务器发送请求获取当日备忘，服务器向数据库发送请求查询
					// System.out.println("the 2 method!"+line.substring(1));
					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mycalendar?serverTimezone=GMT%2B8",
							"root", "dxy19990915");
					stmt = con.createStatement();
					int date = Integer.valueOf(line.substring(1, 9)).intValue();
					String rmd = "";
					rs = stmt.executeQuery("select * from data where date='" + date + "'");
					while (rs.next())
						rmd = rs.getString("rmd");
					System.out.println(rmd);
					serverout = rmd;
					break;
				case "3"://日历客户端更改或增添备忘
					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mycalendar?serverTimezone=GMT%2B8",
							"root", "dxy19990915");
					stmt = con.createStatement();
					date = Integer.valueOf(line.substring(1, 9)).intValue();
					rmd = line.substring(9);
					int result = stmt
							.executeUpdate("update data set rmd='" + line.substring(9) + "'where date='" + date + "'");
					if (result == 0) {
						result = stmt.executeUpdate(
								"insert into data values ('" + line.substring(1, 9) + "','" + line.substring(9) + "')");
						System.out.println("insert!!!!!!!");
					}
					System.out.println(rmd);
					serverout = rmd;
					break;
				case "4"://日历客户端增添提醒事项
					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mycalendar?serverTimezone=GMT%2B8",
							"root", "dxy19990915");
					stmt = con.createStatement();
					int rmddate = Integer.valueOf(line.substring(1, 9)).intValue();
					int tormd = Integer.valueOf(line.substring(10)).intValue();
					result = stmt
							.executeUpdate("update remdate set rmddate='" + rmddate + "'where tormd='" + tormd + "'");
					if (result == 0) {
						result = stmt.executeUpdate("insert into remdate values ('" + rmddate + "','" + tormd + "')");
						System.out.println("insert!!!!!!!");
					}

					break;
				case "5"://日历关闭时，同步数据库端备忘数据到本地
					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mycalendar?serverTimezone=GMT%2B8",
							"root", "dxy19990915");
					stmt = con.createStatement();
					rmd = "";
					rs = stmt.executeQuery("select * from data");
					while (rs.next())
						rmd += rs.getString("date") + " " + rs.getString("rmd") + "\n";
					System.out.println(rmd);
					serverout = rmd;
					break;
				case "6"://日历关闭时，同步数据库提醒日期数据到本地
					Class.forName("com.mysql.cj.jdbc.Driver");
					con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mycalendar?serverTimezone=GMT%2B8",
							"root", "dxy19990915");
					stmt = con.createStatement();
					rmd = "";
					rs = stmt.executeQuery("select * from remdate");
					while (rs.next())
						rmd += rs.getString("rmddate") + " " + rs.getString("tormd") + "\n";
					System.out.println(rmd);
					serverout = rmd;
					break;
				default:
					break;
				}

				socket.shutdownInput();

				System.out.println("server begin write");

				PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(out));
				pWriter.println(serverout);
				pWriter.flush();

				pWriter.close();
				out.close();
				in.close();

				System.out.println("Close Socket: " + socket);

			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
