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
import ai.grakn.graql.Var;
import mjson.Json;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ai.grakn.graql.Graql.var;

/**
 * Handle a user search query
 */
public class MovieSearchIntent implements Intent {

    private final String NEED_MORE_INFO = "Im going to need more information to execute that query";
    private final String NO_SEARCH_RESULTS = "Sorry, I couldn't find anything for that search!";

    private GraknGraph graph;
    private Json parameters;

    private Set<Pattern> queryPatterns;

    public MovieSearchIntent(Json parameters, GraknGraph graph){
        this.graph = graph;
        this.parameters = parameters;

        this.queryPatterns = new HashSet<>();
    }

    //TODO - Degrees must have been computed
    public String execute() {
        addPatternsFromParameters(parameters);

        MatchQuery query = graph.graql().match(queryPatterns);

        if(queryPatterns.size() == 1){
            return NEED_MORE_INFO;
        }

        query = query.distinct();
        query = limit(query, parameters);

        System.out.println(query);

        List<Map<String, Concept>> results =  query.execute();
        if(results.isEmpty()){
            return NO_SEARCH_RESULTS;
        }

        return print(results);
    }

    public void addPatternsFromParameters(Json parameters) {
        Json searchFor = parameters.at("search");

        // Get the type of what the user searches for, otherwise use "movie"
        String entityType = !parameters.at("entity").asString().isEmpty() ? parameters.at("entity").asString() : "movie";

        // Get the resource type used to identify what the user searches for
        String identifierType = getPrimaryKeyType(graph.getType(entityType));

        // Create the variable for the main entity to fetch
        Var entity = var("entity").isa(entityType).has(identifierType, var("identifier"));

        for (Json searchForElement : searchFor.asJsonList()) {
            Map<String, Json> searchForMap = searchForElement.asJsonMap();

            String resourceValue = searchForMap.get("resource").asString();
            String instanceRole = searchForMap.containsKey("role") ? searchForMap.get("role").asString() : null;

            String relatedToIdentifier = Integer.toString(searchForElement.hashCode());

            // Each thing you get will be a resource value
            Var resource = var(resourceValue).value(resourceValue);

            // ... related to something with that value
            Var resourceRelation = var().rel(var(relatedToIdentifier)).rel(var(resourceValue));

            // related to the searchedFor instance
            Var entityRelation = var().rel(var(relatedToIdentifier)).rel(var("entity"));
            if(instanceRole != null){
                entityRelation = var().rel(instanceRole, var(relatedToIdentifier)).rel(var("entity"));
            }

            queryPatterns.add(resource);
            queryPatterns.add(resourceRelation);
            queryPatterns.add(entityRelation);
        }

        queryPatterns.add(entity);
    }
}
