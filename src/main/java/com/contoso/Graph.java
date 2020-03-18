package com.contoso;

import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.models.generated.AttendeeType;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.options.*;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import java.util.LinkedList;
import java.util.List;

import java.util.LinkedList;
import java.util.List;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;

/**
 * Graph
 */
public class Graph {

    private static IGraphServiceClient graphClient = null;
    private static SimpleAuthProvider authProvider = null;

    private static void ensureGraphClient(String accessToken) {
        if (graphClient == null) {
            // Create the auth provider
            authProvider = new SimpleAuthProvider(accessToken);

            // Create default logger to only log errors
            DefaultLogger logger = new DefaultLogger();
            logger.setLoggingLevel(LoggerLevel.ERROR);

            // Build a Graph client
            graphClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .logger(logger)
                    .buildClient();
        }
    }

    public static User getUser(String accessToken) {
        ensureGraphClient(accessToken);

        // GET /me to get authenticated user
        User me = graphClient
                .me()
                .buildRequest()
                .get();

        return me;
    }

    /**
     * Retorna todos os eventos da Agenda do usuário do Token
     * @param accessToken
     * @return
     */
    public static List<Event> getEvents(String accessToken) {
        ensureGraphClient(accessToken);

        // Use QueryOption to specify the $orderby query parameter
        final List<Option> options = new LinkedList<Option>();
        // Sort results by createdDateTime, get newest first
        options.add(new QueryOption("orderby", "createdDateTime DESC"));

        // GET /me/events
        IEventCollectionPage eventPage = graphClient
                .me()
                .events()
                .buildRequest(options)
                .select("subject,organizer,start,end")
                .get();

        return eventPage.getCurrentPage();
    }

    public static void createEvent(String token){
        LinkedList<Option> requestOptions = new LinkedList<Option>();
        requestOptions.add(new HeaderOption("Prefer", "outlook.timezone=\"Pacific Standard Time\""));

        Event event = new Event();
        event.subject = "Mentoria com fulano";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "Mentoria sobre SCRUM";
        event.body = body;
        DateTimeTimeZone start = new DateTimeTimeZone();
        start.dateTime = "2020-03-18T12:00:00";
        start.timeZone = "Pacific Standard Time";
        event.start = start;
        DateTimeTimeZone end = new DateTimeTimeZone();
        end.dateTime = "2020-03-19T14:00:00";
        end.timeZone = "Pacific Standard Time";
        event.end = end;
        Location location = new Location();
        location.displayName = "Stefanini Campina Grande";
        event.location = location;
        LinkedList<Attendee> attendeesList = new LinkedList<Attendee>();
        Attendee attendees = new Attendee();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "nfpedroza@stefanini.com";
        emailAddress.name = "Nathan Fernandes";
        attendees.emailAddress = emailAddress;
        /*
        attendees.type = AttendeeType.REQUIRED;
        attendeesList.add(attendees);
        event.attendees = attendeesList;
        */
        graphClient.me().events()
                .buildRequest( requestOptions )
                .post(event);

    }
}