import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class MainWindow {

	private static JFrame frame;
	private JTextField usbDirectory;
	private JTextField driveFolderId;
	public static JLabel lblStatusIdle;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 879, 342);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("USB and Google Drive Sync");
		
		JLabel lblUsbDirectory = new JLabel("USB Directory:");
		lblUsbDirectory.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblUsbDirectory.setBounds(33, 39, 170, 66);
		frame.getContentPane().add(lblUsbDirectory);
		
		usbDirectory = new JTextField();
		usbDirectory.setBounds(202, 62, 493, 30);
		frame.getContentPane().add(usbDirectory);
		usbDirectory.setColumns(10);
		
		//browse button
		JButton browseDirectory = new JButton("Browse");
		browseDirectory.setBounds(707, 47, 142, 61);
		frame.getContentPane().add(browseDirectory);
		browseDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					usbDirectory.setText(PickMe());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		JLabel lblGoogleDriveFolder = new JLabel("Drive Folder ID:");
		lblGoogleDriveFolder.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblGoogleDriveFolder.setBounds(33, 147, 183, 43);
		frame.getContentPane().add(lblGoogleDriveFolder);
		
		driveFolderId = new JTextField();
		driveFolderId.setFont(new Font("Tahoma", Font.PLAIN, 15));
		driveFolderId.setColumns(10);
		driveFolderId.setBounds(512, 158, 337, 30);
		frame.getContentPane().add(driveFolderId);
		
		JLabel lblHttpsdrivegooglecomdriveufolders = new JLabel("https://drive.google.com/drive/u/0/folders/");
		lblHttpsdrivegooglecomdriveufolders.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblHttpsdrivegooglecomdriveufolders.setBounds(228, 156, 302, 32);
		frame.getContentPane().add(lblHttpsdrivegooglecomdriveufolders);
		
		lblStatusIdle = new JLabel("Status: Idle");
		lblStatusIdle.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblStatusIdle.setBounds(373, 211, 476, 66);
		frame.getContentPane().add(lblStatusIdle);
		
		//sync button
		JButton btnSync = new JButton("Sync");
		btnSync.setBounds(71, 214, 235, 66);
		frame.getContentPane().add(btnSync);
		btnSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String path = usbDirectory.getText();
				String folderId = driveFolderId.getText();
				if(!(path.endsWith("\\")))
					path += "\\";
				
				UpdateStatus us = new UpdateStatus("Connecting to Google Drive");
				new Thread(us).start();
				SyncThread st = new SyncThread(path, folderId);
				new Thread(st).start();
			}
			
		});
}

	//popup window
	public static void showPopUp(String text) {
		JOptionPane.showMessageDialog(frame, text);
	}
	
	//file Opener
	public String PickMe() throws Exception{
		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			return file.getAbsolutePath() + "\\";
		}else {
			return "";
		}
		
	}
}

