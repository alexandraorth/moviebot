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

import ai.grakn.GraknGraph;
import ai.grakn.concept.Concept;
import ai.grakn.graql.MatchQuery;
import ai.grakn.graql.Pattern;
import mjson.Json;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static ai.grakn.graql.Graql.var;

public class RandomIntent implements Intent {

    private GraknGraph graph;

    private Set<Pattern> queryPatterns;
    private static final Integer LIMIT = 100;

    public RandomIntent(GraknGraph graph){
        this.graph = graph;
        this.queryPatterns = new HashSet<>();
    }

    @Override
    public String execute() {

        queryPatterns.add(var("movie").isa("movie"));
        queryPatterns.add(var("movie").has("title", var("title")));
        queryPatterns.add(var("movie").has("overview", var("overview")));
        queryPatterns.add(var("movie").has("youtube-trailer", var("youtube-trailer")));

        MatchQuery match = graph.graql()
                .match(queryPatterns);

        match = match.distinct();
        match = match.limit(LIMIT);

        System.out.println(match);

        Map<String, Concept> result = match.execute().get(new Random().nextInt(LIMIT));

        return  result.get("title").asResource().getValue() + "\n" +
                result.get("overview").asResource().getValue() + "\n" +
                format(result.get("youtube-trailer").asResource().getValue().toString()) + "\n";
    }
}
