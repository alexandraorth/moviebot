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

package ai.grakn.intents;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;
import mjson.Json;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static ai.grakn.SparkMain.disableInternalLogs;

public class MovieInformationIntentTest {

    private static GraknGraph graph;

    @BeforeClass
    public static void open(){
        disableInternalLogs();

        graph = Grakn.factory(Grakn.DEFAULT_URI, "movie").getGraph();
    }

    @AfterClass
    public static void close(){
        graph.close();
    }

    @Test
    public void informationIntentTypeRole(){
        Json parameters = Json.read("{\"movie\":\"15704216\",\"information\":[{\"role\":\"director\",\"type\":\"person\"}]}");

        System.out.println(new MovieInformationIntent(parameters, graph).execute());
    }

    @Test
    public void informationIntentType(){
        Json parameters = Json.read("{\"movie\":\"15704216\",\"information\":[{\"type\":\"genre\"}]}");

        System.out.println(new MovieInformationIntent(parameters, graph).execute());
    }

    @Test
    public void informationIntentRole(){
        Json parameters = Json.read("{\"movie\":\"15704216\",\"information\":[{\"role\":\"actor\"}]}");

        System.out.println(new MovieInformationIntent(parameters, graph).execute());
    }

    @Test
    public void informationIntentTypeLimit(){
        Json parameters = Json.read("{\"movie\":\"15704216\",\"limit\":1,\"information\":[{\"type\":\"person\"}]}");

        System.out.println(new MovieInformationIntent(parameters, graph).execute());
    }

    @Test
    public void informationIntentResourceType(){
        Json parameters = Json.read("{\"movie\":\"15704216\",\"information\":[{\"type\":\"release-date\"}]}");

        System.out.println(new MovieInformationIntent(parameters, graph).execute());
    }

}
