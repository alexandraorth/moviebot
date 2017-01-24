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

public class MovieSearchIntentTest {

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
    public void searchIntentIdRole(){
        Json parameters = Json.read("{\"search\":[{\"role\":\"actor\",\"id\":\"228905128\"}]}");

        new MovieSearchIntent(parameters, graph).execute();
    }

    @Test
    public void searchIntentId(){
        Json parameters = Json.read("{\"search\":[{\"id\":\"228905128\"}]}");

        new MovieSearchIntent(parameters, graph).execute();
    }

    @Test
    public void searchIntentIdLimit(){
        Json parameters = Json.read("{\"limit\":1,\"search\":[{\"id\":\"228905128\"}]}");

        new MovieSearchIntent(parameters, graph).execute();
    }
}
