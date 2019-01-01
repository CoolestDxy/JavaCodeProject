import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Myframe extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private Panel week, month, show;
	private JButton[] day;
	private JTextField tField;
	private JLabel w[];
	private BufferedWriter out;
	private BufferedReader in;
	private String filename = "rmde.txt";
	private int iy, im, lastbutton;// last 保存上一次按钮
	boolean connect;// 是否连接服务器

	public Myframe(boolean l) {
		// TODO Auto-generated constructor stub
		super("MyCalendar");
		connect = l;
		setLayout(null);
		week = new Panel();
		month = new Panel();
		week.setBounds(0, 0, 700, 20);
		w = new JLabel[7];
		for (int i = 0; i < 7; i++)
			w[i] = new JLabel();
		w[0].setText("   Monday");
		w[1].setText("   Tuesday");
		w[2].setText("  Wednesday");
		w[3].setText("  Thursday");
		w[4].setText("   Friday");
		w[5].setText("  Saturday");
		w[6].setText("   Sunday");
		week.setLayout(new GridLayout(1, 7));
		for (JLabel i : w) {
			i.setSize(100, 20);
			week.add(i);
		}
		month.setBounds(0, 0, 700, 600);
		month.setLayout(new GridLayout(6, 7));
		Calendar ca = Calendar.getInstance();
		iy = ca.get(Calendar.YEAR);
		im = ca.get(Calendar.MONTH) + 1;
		ca.add(Calendar.MONTH, 0);
		ca.set(Calendar.DAY_OF_MONTH, 1);
		int d1 = ca.get(Calendar.DAY_OF_WEEK) - 1;// 第一天星期几
		ca = Calendar.getInstance();
		ca.add(Calendar.MONTH, 1);
		ca.set(Calendar.DAY_OF_MONTH, 0);
		int dn = ca.get(Calendar.DAY_OF_MONTH);// 本月多少天
		int t = 1, t1 = d1 - 1;
		// System.out.println(d1+" "+dn+" "+t1+" ");

		ca = Calendar.getInstance();
		int today = ca.get(Calendar.DAY_OF_MONTH);//添加日期按钮，检查当天日期并判断是否有提醒事项
		try {
			in = new BufferedReader(new FileReader("rmddate.txt"));
			String rd = in.readLine();
			String today1 = iy + String.format("%02d", im) + String.format("%02d", today);
			while (rd != null) {
				if (rd.length() >= 8) {
					if (rd.substring(0, 8).equals(today1)) {
						JOptionPane.showConfirmDialog(null, "请查看" + rd.substring(9, 13) + "/" + rd.substring(13, 15)
								+ "/" + rd.substring(15) + "当日备份", "了解", JOptionPane.YES_NO_OPTION);
					}
				}
				rd = in.readLine();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		lastbutton = today;
		day = new JButton[42];
		for (int i = 0; i < 42; i++)
			day[i] = new JButton();
		for (int i = 0; i < dn; i++) {
			day[t1].setText("" + t);
			day[t1].addActionListener(this);
			day[t1].addMouseListener(new MouseListener() {//日期事件添加鼠标监听事件，鼠标右键点击时添加提醒
				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {//根据有无联网实现从本地或者服务器端获取数据
					// TODO Auto-generated method stub
					if (e.getButton() == MouseEvent.BUTTON3) {
						String inputValue = JOptionPane.showInputDialog("input the remind date:");
						if (connect == false) {
							try {
								out = new BufferedWriter(new FileWriter("rmddate.txt", true));
								out.write(inputValue + " " + iy + String.format("%02d", im)
										+ String.format("%02d", lastbutton));
								out.newLine();
							} catch (IOException ex) {
								ex.printStackTrace();
							} finally {
								try {
									out.close();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
						} else {
							Socket client;
							try {
								client = new Socket("localhost", 8888);
								PrintWriter out = new PrintWriter(client.getOutputStream());
								out.flush(); // need to flush a short message
								out.println(4 + "" + inputValue + " " + iy + String.format("%02d", im)
										+ String.format("%02d", lastbutton));
								out.flush();
								client.shutdownOutput();
								out.close();
								client.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			});
			if (t == today) {
				day[t1].setBackground(Color.YELLOW);
			}
			t++;
			t1++;
		}
		for (JButton i : day) {
			month.add(i);
		}

		show = new Panel();
		show.setBounds(0, 20, 700, 780);
		show.setLayout(null);
		tField = new JTextField();
		tField.setBounds(0, 600, 700, 180);
		tField.setBackground(Color.white);
		tField.addActionListener(new ActionListener() {//添加文本框监听，根据有无网络连接实现更改本地

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String rmdr = tField.getText();

				if (connect == false) {
					try {
						out = new BufferedWriter(new FileWriter(filename, true));
						out.write(iy + String.format("%02d", im) + String.format("%02d", lastbutton) + " " + rmdr);
						out.newLine();
					} catch (IOException ex) {
						ex.printStackTrace();
					} finally {
						try {
							out.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				} else {
					Socket client;
					try {
						client = new Socket("localhost", 8888);
						PrintWriter out = new PrintWriter(client.getOutputStream());
						out.flush(); // need to flush a short message
						out.println(3 + "" + iy + String.format("%02d", im) + String.format("%02d", lastbutton) + " "
								+ rmdr);
						out.flush();
						client.shutdownOutput();
						out.close();
						client.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}
		});
		show.add(month);
		show.add(tField);
		setSize(723, 800);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {//关闭窗口时进行数据同步
				// TODO Auto-generated method stub
				super.windowClosing(e);
				if (connect) {
					Socket client;
					try {
						client = new Socket("localhost", 8888);
						PrintWriter pout = new PrintWriter(client.getOutputStream());
						pout.flush(); // need to flush a short message
						pout.println(5 + "");
						pout.flush();
						client.shutdownOutput();

						BufferedReader bReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
						String info = null;

						while ((info = bReader.readLine()) != null) {
							try {
								out = new BufferedWriter(new FileWriter(filename, true));
								out.write(info);
								out.newLine();
							} catch (IOException ex) {
								ex.printStackTrace();
							} finally {
								try {
									out.close();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
						}
						client = new Socket("localhost", 8888);
						pout = new PrintWriter(client.getOutputStream());
						pout.flush(); // need to flush a short message
						pout.println(6 + "");
						pout.flush();
						client.shutdownOutput();

						bReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
						info = null;

						while ((info = bReader.readLine()) != null) {
							try {
								out = new BufferedWriter(new FileWriter("rmddate.txt", true));
								out.write(info);
								out.newLine();
							} catch (IOException ex) {
								ex.printStackTrace();
							} finally {
								try {
									out.close();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
						}
						client.close();
						pout.close();
						bReader.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		setVisible(true);
		this.add(week);
		this.add(show);

	}

	@Override
	public void actionPerformed(ActionEvent e) {//点击按钮时判断是否连接从文本或服务器端获取数据
		// TODO Auto-generated method stub
		tField.setText("");
		int day = Integer.valueOf(e.getActionCommand()).intValue();
		String line, date = iy + String.format("%02d", im) + String.format("%02d", day);
		System.out.println(date);
		if (connect == false) {// 本地读取文件
			try {
				in = new BufferedReader(new FileReader(filename));
				line = in.readLine();
				while (line != null) {
					if (line.length() >= 8) {
						if (line.substring(0, 8).equals(date)) {
							tField.setText(line.substring(9));
							System.out.println(line);
						}
					}
					line = in.readLine();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			lastbutton = day;
		} else {
			Socket client;
			try {
				client = new Socket("localhost", 8888);
				PrintWriter out = new PrintWriter(client.getOutputStream());
				out.flush(); // need to flush a short message
				out.println(2 + date);
				out.flush();
				client.shutdownOutput();

				BufferedReader bReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String info = null;

				info = bReader.readLine();
				System.out.println("rmd:" + info);
				tField.setText(info);
				client.close();
				out.close();
				bReader.close();
				lastbutton = day;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}
}
