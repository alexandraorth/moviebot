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
import ai.grakn.concept.Resource;
import ai.grakn.concept.Type;
import ai.grakn.graql.MatchQuery;
import ai.grakn.graql.Pattern;
import ai.grakn.graql.Var;
import mjson.Json;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ai.grakn.graql.Graql.var;

/**
 * Handle a query for information about a movie
 */
public class MovieInformationIntent implements Intent {

    private static final String NO_MOVIE_MESSAGE = "I don't know anything about that movie! Try with another.";
    private static final String NO_SEARCH_RESULTS = "I don't know that about this thing.";

    private GraknGraph graph;
    private Json parameters;

    private Set<Pattern> queryPatterns;
    private Set<Map<String, Concept>> results;

    public MovieInformationIntent(Json parameters, GraknGraph graph){
        this.graph = graph;
        this.parameters = parameters;

        this.queryPatterns = new HashSet<>();
        this.results = new HashSet<>();
    }

    @Override
    public String execute() {
        String primaryResourceIdOfEntity = parameters.at("entity").asString();

        Collection<Resource<String>> resources = graph.getResourcesByValue(primaryResourceIdOfEntity);
        if(resources.isEmpty()){
            return NO_MOVIE_MESSAGE;
        }

        for(Resource<String> resource:resources) {
            String resourceType = resource.type().getName();

            // looking for information about an entity
            queryPatterns.clear();
            queryPatterns.add(var("entity")
                    .has(resourceType, primaryResourceIdOfEntity));

            addPatternsFromParameters();

            MatchQuery match = graph.graql().match(queryPatterns);
            match = match.distinct();
            match = limit(match, parameters);

            results.addAll(match.execute());
        }

        if (results.isEmpty()) {
            return NO_SEARCH_RESULTS;
        }

        return print(results);
    }

    private void addPatternsFromParameters(){
        String information = parameters.at("information").asString();
        Type informationType = graph.getType(information);

        if(informationType.isResourceType()) {

            Var resource = var("identifier").isa(information);
            Var relation = var().rel(var("entity")).rel(var("identifier"));

            queryPatterns.add(resource);
            queryPatterns.add(relation);
        }

        else if(informationType.isRoleType()){
            String primaryKeyType = getPrimaryKeyType(informationType);

            // relate the entity to an instance playing the role
            Var relation = var().rel(var("entity")).rel(information, var("playing-role"));

            // select the resource identifier of the entity playing the role
            Var playingRoleIdentifier = var("playing-role").has(primaryKeyType, var("identifier"));

            queryPatterns.add(playingRoleIdentifier);
            queryPatterns.add(relation);
        }

        else if(informationType.isEntityType()){
            String primaryKeyType = getPrimaryKeyType(informationType);

            // relate the entity to an instance playing the role
            Var relation = var().rel(var("entity")).rel(var("other-entity"));

            // select the resource identifier of the entity playing the role
            Var playingRoleIdentifier = var("other-entity")
                    .isa(information)
                    .has(primaryKeyType, var("identifier"));

            queryPatterns.add(playingRoleIdentifier);
            queryPatterns.add(relation);
        }
    }
}
