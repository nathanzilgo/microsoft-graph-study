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


    private static void ensureGraphApiClient(String credential){
        authProvider = new SimpleAuthProvider(credential);

        DefaultLogger logger = new DefaultLogger();

        logger.setLoggingLevel(LoggerLevel.DEBUG);

        graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .logger(logger)
                .buildClient();
    }

    private static void ensureGraphClient(String accessToken) {
        if (graphClient == null) {
            // Create the auth provider
            authProvider = new SimpleAuthProvider(accessToken);

            // Create default logger to only log errors
            DefaultLogger logger = new DefaultLogger();
            logger.setLoggingLevel(LoggerLevel.DEBUG);

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
        //final String credential = "nupl9.C5rb]aO5:yvT:3L.TKcH7tB1Im";
        ensureGraphClient(token);

        LinkedList<Option> requestOptions = new LinkedList<Option>();
        //requestOptions.add(new HeaderOption("Authorization", "Bearer nupl9.C5rb]aO5:yvT:3L.TKcH7tB1Im" ));

        Event event = new Event();
        event.subject = "Mentoria com fulano";

        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "Mentoria sobre SCRUM";
        event.body = body;

        DateTimeTimeZone start = new DateTimeTimeZone();
        start.dateTime = "2020-03-20T12:00:00";
        start.timeZone = "Pacific Standard Time";
        event.start = start;

        DateTimeTimeZone end = new DateTimeTimeZone();
        end.dateTime = "2020-03-22T14:00:00";
        end.timeZone = "Pacific Standard Time";
        event.end = end;

        Location location = new Location();
        location.displayName = "Stefanini Campina Grande";
        event.location = location;

        LinkedList<Attendee> attendeesList = new LinkedList<Attendee>();
        Attendee attendees = new Attendee();

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = Graph.getUser(token).mail;
        emailAddress.name = "Nathan Fernandes";
        attendees.emailAddress = emailAddress;

        attendees.type = AttendeeType.REQUIRED;
        attendeesList.add(attendees);

        event.attendees = attendeesList;

        try {
            graphClient.me().calendar().events()
                    .buildRequest()
                    .post(event);
        }catch(Exception e) {

            System.out.println("Deu águia:   ");
            e.printStackTrace();
        }
    }
}