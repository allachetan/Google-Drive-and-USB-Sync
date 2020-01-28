
public class UpdateStatus extends Thread {

	String status;
	
	public UpdateStatus(String s) {
		status = s;
		
	}
	
	public void run() {
		MainWindow.lblStatusIdle.setText("Status: " + status);
	}
	
}
