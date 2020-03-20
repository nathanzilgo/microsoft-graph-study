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

        // Participantes:
        LinkedList<Attendee> attendeesList = new LinkedList<Attendee>();
        Attendee mentor = new Attendee();
        Attendee mentorado = new Attendee();

        EmailAddress mentorMail = new EmailAddress();
        mentorMail.address = "dwlima@stefanini.com";
        mentorMail.name = "Daniell Wagner";
        mentor.emailAddress = mentorMail;

        EmailAddress mentoradoMail = new EmailAddress();
        mentoradoMail.address = "gmcarneiro@stefanini.com";
        mentoradoMail.name = "Guilherme Carneiro";
        mentorado.emailAddress = mentoradoMail;

        mentor.type = AttendeeType.REQUIRED;
        mentorado.type = AttendeeType.REQUIRED;

        attendeesList.add(mentor);
        attendeesList.add(mentorado);

        // Evento:
        Event event = new Event();
        event.subject = "Mentoria com " + mentor.emailAddress.name;

        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "" +
                "<b>Mentoria sobre SCRUM</b> <br>" +
                "Olá, " + mentorado.emailAddress.name + " <br> " +
                "Você tem uma mentoria marcada com o mentor "
                + mentor.emailAddress.name + "!!";

        event.body = body;

        DateTimeTimeZone start = new DateTimeTimeZone();
        start.dateTime = "2020-03-26T16:00:00";
        start.timeZone = "Bahia Standard Time";
        event.start = start;

        DateTimeTimeZone end = new DateTimeTimeZone();
        end.dateTime = "2020-03-26T18:00:00";
        end.timeZone = "Bahia Standard Time";
        event.end = end;

        Location location = new Location();
        location.displayName = "Stefanini Campina Grande";
        event.location = location;

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