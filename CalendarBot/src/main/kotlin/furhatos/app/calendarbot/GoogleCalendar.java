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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GoogleCalendar {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    Calendar service;
    public GoogleCalendar() throws  IOException, GeneralSecurityException{
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

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

    public boolean insertEvent(EventObject ev) {
        try {
            String startTime = ev.date + "T" + ev.startTime + "+02:00";
            String endTime = ev.date + "T" + ev.endTime + "+02:00";

            Event event = new Event()
                    .setSummary(ev.bookStatement + "->" + ev.name)
                    .setId(ev.getID());

            DateTime startDateTime = new DateTime(startTime);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Europe/Stockholm");

            event.setStart(start);

            DateTime endDateTime = new DateTime(endTime);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Europe/Stockholm");;

            event.setEnd(end);

            String calendarId = "primary";
            event = service.events().insert(calendarId, event).execute();

            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeEvent(EventObject ev) {
        try {
            service.events().delete("primary", ev.getID()).execute();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public List<HashMap<String, String>> getEvent(EventObject ev) {
        try {
            Event event;
            event = service.events().get("primary", ev.getID()).execute();
            DateTime start = event.getStart().getDateTime();
            //System.out.printf("%s (%s)\n", event.getSummary(), start);

            System.out.printf("Successful get of EventObject: %s\n", ev.getID());
            List<Event> items = new ArrayList<>();
            items.add(event);
            return Tools.PrettifyItemList(items);
        } catch (Exception e){
            System.out.printf("Unsuccessful get, not able to get EventObject: %s\n", ev.getID());
            return null;
        }

    }

    public List<HashMap<String, String>> listEvents(EventObject ev) {
        try {
            Events events;
            DateTime nowEnd;
            DateTime nowStart;

            if (ev.startTime == null) {
                nowStart = new DateTime(ev.date + "T00:00:00.000+01:00");
                if(ev.dateTo == null){
                    nowEnd = new DateTime(ev.date + "T23:59:59.999+01:00");
                }else{
                    nowEnd = new DateTime(ev.dateTo + "T23:59:59.999+01:00");
                }
            } else{
                nowStart = new DateTime(ev.date + "T" + ev.startTime + "+01:00");
                if (ev.dateTo == null) {
                    nowEnd = new DateTime(ev.date + "T" + ev.endTime + "+01:00");
                } else{
                    nowEnd = new DateTime(ev.dateTo + "T" + ev.endTime + "+01:00");
                }
            }

            events = service.events().list("primary")
                    .setMaxResults(200)
                    .setTimeMin(nowStart)
                    .setTimeMax(nowEnd)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setTimeZone("Europe/Stockholm")
                    //.setShowDeleted(true)
                    .execute();

            List<Event> items = events.getItems();

            if (items.isEmpty()) {
                return null;
            } else {
                return Tools.PrettifyItemList(items);
            }

        } catch (Exception e) {
            return null;
        }
    }

}