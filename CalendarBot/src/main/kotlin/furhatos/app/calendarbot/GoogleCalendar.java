package furhatos.app.calendarbot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import com.google.api.services.calendar.model.EventDateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleCalendar {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    Calendar service;
    public GoogleCalendar() throws  IOException, GeneralSecurityException{
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Method for inserting a given EventObject into the google Calendar.
     * @param ev, The event object that is generated from furhat to be inserted .
     * @return An string with information if the insert was successful or not.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private String InsertEvent(EventObject ev) {
        try {
            String startTime = ev.date + "T" + ev.startTime + "-00:00";
            String endTime = ev.date + "T" + ev.endTime + "-00:00";

            Event event = new Event()
                    .setSummary(ev.name)
                    .setId(ev.getID());

            DateTime startDateTime = new DateTime(startTime);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime);

            event.setStart(start);

            DateTime endDateTime = new DateTime(endTime);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime);

            event.setEnd(end);
            System.out.println(startTime);
            System.out.println(endTime);

            String calendarId = "primary";
            event = service.events().insert(calendarId, event).execute();

            System.out.printf("EventObject created: %s\n", event.getHtmlLink());

            return "Successfully inserted event.";
        }catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Unsuccessful of inserting the event");
            return "Unsuccessful of inserting the event";
        }
    }

    /**
     * Method for removing a given EventObject from the google Calendar.
     * @param ev, The event object that is generated from furhat to be deleted.
     * @return An string with information if the remove was successful or not.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private String RemoveEvent(EventObject ev) {
        try {
            // Delete an event
            service.events().delete("primary", ev.getID()).execute();
            System.out.printf("EventObject Successfully removed: %s\n", ev.getID());

            return "Successfully remove event.";
        } catch (Exception e){
            System.out.printf("EventObject could not be removed %s\n", ev.getID());
            return "Successfully remove event.";
        }
    }

    /**
     * Method for getting an EventObject in a google Calendar.
     * @param ev, The event object that is generated from furhat to be listed.
     * @return An EventObject from google calendar api.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Event getEvent(EventObject ev) {
        try{
            Event event;
            event = service.events().get("primary", ev.getID()).execute();
            DateTime start = event.getStart().getDateTime();
            //System.out.printf("%s (%s)\n", event.getSummary(), start);

            System.out.printf("Successful get of EventObject: %s\n", ev.getID());
            return event;
        }catch (Exception e){
            System.out.printf("Unsuccessful get, not able to get EventObject: %s\n", ev.getID());
            return null;
        }

    }

    /**
     * Method for listing EventObject in a google Calendar.
     * @param ev, The event object that is generated from furhat to be deleted.
     * @return An string with information if the remove was successful or not.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private List<Event> ListEvents(EventObject ev) {
        try {
            Events events;
            DateTime nowEnd;
            DateTime nowStart;

            if (ev.startTime == null) {
                nowStart = new DateTime(ev.date + "T00:00:00.000+00:00");
                if(ev.dateTo == null){
                    nowEnd = new DateTime(ev.date + "T23:59:59.999+00:00");
                }else{
                    nowEnd = new DateTime(ev.dateTo + "T23:59:59.999+00:00");
                }
            } else{
                nowStart = new DateTime(ev.date + "T" + ev.startTime + "+00:00");
                if (ev.dateTo == null) {
                    nowEnd = new DateTime(ev.date + "T" + ev.endTime + "+00:00");
                } else{
                    nowEnd = new DateTime(ev.dateTo + "T" + ev.endTime + "+00:00");
                }
            }

            events = service.events().list("primary")
                    .setMaxResults(200)
                    .setTimeMin(nowStart)
                    .setTimeMax(nowEnd)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    //.setShowDeleted(true)
                    .execute();
            System.out.println(nowStart);
            System.out.println(nowEnd);

            List<Event> items = events.getItems();

            if (items.isEmpty()) {
                System.out.println("No upcoming events found.");
            } else {
                System.out.println("Upcoming events");
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    System.out.printf("%s (%s)\n", event.getSummary(), start);
                }
            }
            System.out.printf("Successful listing of EventObject on date: %s\n", ev.date);
            return items;

        } catch (Exception e) {
            System.out.println("Execution " + e);

            System.out.printf("Unsuccessful listing of Events on date: %s\n", ev.date);

            return null;
        }
    }

    /**
     * Method for choosing action to take in google Calendar based on a intent.
     * @param ev, The event object that is generated from furhat.
     * @return An string with information if the remove was successful or not.
     * @throws IOException If the credentials.json file cannot be found.
     * @exception Exception if the user dont specify an Intent as ADD,REMOVE OR LIST.
     */
    public void TakeAction(EventObject ev) {
        if (ev.intent.equals(Constants.ADD_INTENT)){
            InsertEvent(ev);
        }
        else if(ev.intent.equals(Constants.REMOVE_INTENT)){
            RemoveEvent(ev);
        }
        else if(ev.intent.equals(Constants.LIST_INTENT)){
            ListEvents(ev);
        }
        else if(ev.intent.equals(Constants.GET_INTENT)){
            getEvent(ev);
        }
    }

}