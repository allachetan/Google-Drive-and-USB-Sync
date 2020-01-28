import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveQuickstart {
	private static final String APPLICATION_NAME = "usbsyncdrive";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	public static boolean changed;
	
	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT
	 *            The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException
	 *             If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static Drive setUp() throws IOException, GeneralSecurityException, ParseException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		changed = false;
		
		return service;
		
	}

	public static void syncDirectory(Drive service, String destination, String folderId) throws IOException, ParseException {
		String pageToken = null;
		do {
			FileList result = service.files().list().setQ("'" + folderId + "' in parents")
					.setSpaces("drive").setFields("nextPageToken, files(id, name, modifiedTime, mimeType)")
					.setPageToken(pageToken).execute();
			for (File file : result.getFiles()) {
				Path path = Paths.get(destination + file.getName());
				String fileId = file.getId();
				//if file is a folder
				if(file.getMimeType().equals("application/vnd.google-apps.folder")) {
					if(!Files.exists(Paths.get(destination + file.getName() + "\\"))) {
						new java.io.File(destination + file.getName() + "\\").mkdir();
						changed = true;
					}
					syncDirectory(service, destination + file.getName() + "\\", file.getId());
				}
				//if file already exists
				else if (Files.isReadable(path)) {
					java.io.File f = new java.io.File(destination + file.getName());
					Date d1 = new Date(f.lastModified());
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
					format.setTimeZone(TimeZone.getTimeZone("UTC"));
					Date d2 = format.parse(file.getModifiedTime().toString());
					if (d1.compareTo(d2) < 0) {
						f.delete();
						UpdateStatus us = new UpdateStatus("Downloading " + file.getName());
						new Thread(us).start();
						OutputStream outputStream = new FileOutputStream(destination + file.getName());
						service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
						outputStream.close();
						changed = true;
					}
				//file doesn't exist
				}else {
					UpdateStatus us = new UpdateStatus("Downloading " + file.getName());
					new Thread(us).start();
					OutputStream outputStream = new FileOutputStream(
							destination + file.getName());
					service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
					outputStream.close();
					changed = true;
				}
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
		
	}
}