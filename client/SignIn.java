
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

class SignIn extends JFrame {// 用户登录界面，向服务器查询用户名比对密码
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	JFrame f;
	JLabel lb1;
	JLabel lb2;
	JTextField tf;
	JPasswordField pwd;
	JButton b1;
	JButton b2;
	JPanel p1;
	JPanel p2;
	String use;
	char passw[] = new char[10];
	ResultSet rs;
	String k1, k2;
	boolean ifconnect = false;
	boolean go = false;

	public void init() {
		lb1 = new JLabel("用户名:");
		lb2 = new JLabel(" 密 码:");
		tf = new JTextField(10);
		pwd = new JPasswordField(10);
		b1 = new JButton("登陆");
		b2 = new JButton("取消");
		p1 = new JPanel();
		p2 = new JPanel();
		p1.add(lb1);
		p1.add(tf);
		p1.add(lb2);
		p1.add(pwd);
		p2.add(b1);
		p2.add(b2);
		f.add(p1);
		f.add(p2);
	}

	SignIn() {
		f = new JFrame("登陆界面");
		init();
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ei) {
				try {
					use = tf.getText();
					passw = pwd.getPassword();
					String passww = new String(passw);
					Socket client = new Socket("localhost", 8888);
					PrintWriter out = new PrintWriter(client.getOutputStream());
					out.flush(); // need to flush a short message
					out.println(1 + use);
					out.flush();
					client.shutdownOutput();

					BufferedReader bReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
					String info = null;

					info = bReader.readLine();
					System.out.println("client read password:" + info);
					client.close();
					out.close();
					bReader.close();
					if (info.equals(passww)) {
						JOptionPane.showMessageDialog(null, "用户登陆成功");// 登陆成功
						f.setVisible(false);
						new Myframe(true);
					} else {
						JOptionPane.showMessageDialog(null, "请检查用户信息，登陆失败");// 登录失败
					}
				} catch (Exception ey) {
					JOptionPane.showMessageDialog(null, "请检查用户信息，登陆失败");
				}
			}
		});
		f.setLayout(new GridLayout(2, 1, 10, 5));
		f.setVisible(true);
		f.setSize(210, 150);
		f.setLocation(550, 300);
		f.setResizable(false);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		b2.addActionListener(new ActionListener() {// 58
			public void actionPerformed(ActionEvent e) {
				f.setVisible(false);
				new Myframe(false);
			}
		});
	}

	public static void main(String[] args) {
		new SignIn();
	}

}