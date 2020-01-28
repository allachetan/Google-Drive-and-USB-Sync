import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import com.google.api.services.drive.Drive;

public class SyncThread extends Thread{

	String path;
	String folderId;
	
	public SyncThread(String p, String f) {
		path = p;
		folderId = f;
	}
	
	public void run() {
		try {
			Drive service = DriveQuickstart.setUp();
			DriveQuickstart.syncDirectory(service, path, folderId);
			UpdateStatus us = new UpdateStatus("Success!");
			new Thread(us).start();
			if(DriveQuickstart.changed == true)
				MainWindow.showPopUp("Eject and reinsert the USB and run this utility again to verify the copy is successful");
			else
				MainWindow.showPopUp("USB is in sync with google drive");
			DriveQuickstart.changed = false;
		} catch (IOException | GeneralSecurityException | ParseException e) {
			UpdateStatus us = new UpdateStatus("Error!");
			new Thread(us).start();
			e.printStackTrace();
		}
	}
	
}
