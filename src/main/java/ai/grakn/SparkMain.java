/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn;

import ai.grakn.intents.Intent;
import ai.grakn.intents.MovieInformationIntent;
import ai.grakn.intents.MovieSearchIntent;
import ai.grakn.intents.RandomIntent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import mjson.Json;
import spark.Request;
import spark.Response;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

public class SparkMain {

    private static final String MOVIE_KEYSPACE = "movie";

    private static GraknGraph graph;

    public static void main(String[] args){
        disableInternalLogs();

        graph = Grakn.factory(Grakn.DEFAULT_URI, MOVIE_KEYSPACE).getGraph();

        port(1234);
        get("/running", SparkMain::running);
        post("/query", SparkMain::query);
    }

    private static String running(Request request, Response response){
        return "Query engine running";
    }

    private static String query(Request request, Response response){
        Json result = Json.read(request.body()).at("result");

        System.out.println(result);

        Intent intent = determineIntent(result);
        String displayText = intent.execute();

        System.out.println(displayText);

        return createResponse(displayText, response);
    }

    /**
     * Determine which type of query to execute based on the api.ai "action"
     */
    private static Intent determineIntent(Json result){
        String action = result.at("action").asString();

        switch (action){
            case "search":
                return new MovieSearchIntent(result.at("parameters"), graph);
            case "information":
                return new MovieInformationIntent(result.at("parameters"), graph);
            case "random":
                return new RandomIntent(graph);
        }

        throw new RuntimeException("Can not handle this type of query");
    }

    /**
     * Create the response to return to api.ai
     */
    private static String createResponse(String displayText, Response response){
        Json slackMessage = Json.object().set("text", displayText);
        Json data = Json.object().set("slack", slackMessage);

        Json responseBody = Json.object().set("data", data);

        response.body(responseBody.toString());
        response.header("Content-type", "application/json");

        return responseBody.toString();
    }

    public static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}
