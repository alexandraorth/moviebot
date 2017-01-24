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

import ai.grakn.concept.Concept;
import ai.grakn.concept.Resource;
import ai.grakn.concept.Type;
import ai.grakn.graql.MatchQuery;
import ai.grakn.graql.Order;
import mjson.Json;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public interface Intent {

    public String execute();

    /**
     * Limit the number of results
     */
    public default MatchQuery limit(MatchQuery query, Json parameters){
        if(parameters.at("limit").asString().equals("")){
            return query;
        }

        query = query.limit(parameters.at("limit").asInteger());

        return query;
    }

    /**
     * Order by degree
     */
    public default MatchQuery order(MatchQuery query, Json parameters){
        if(parameters.at("order").asString().equals("")){
            return query;
        }

        switch (parameters.at("order").asString()){
            case "asc":
                query = query.orderBy("degree", Order.asc);
                break;
            case "desc":
                query = query.orderBy("degree" , Order.desc);
                break;
        }

        return query;
    }

    public default String format(String result){
        if(result.startsWith("www.youtube")){
            return "https://" + result;
        }
        return result;
    }

    public default String print(Collection<Map<String, Concept>> results){
        return results.stream()
                .filter(m -> m.containsKey("identifier"))
                .map(m -> m.get("identifier"))
                .map(Concept::asResource)
                .map(Resource::getValue)
                .map(Object::toString)
                .map(this::format)
                .distinct()
                .collect(joining("\n"));
    }

    /**
     * Returns the types that contains the primary information about the given type.
     * For example, the type "person" is best described by the resource type "name".
     */
    public default String getPrimaryKeyType(Type type){
        if(type.isRoleType()){
            type = type.asRoleType().playedByTypes().stream().findFirst().get();
        }

        switch (type.getName()){
            case "person":
                return "name";
            case "genre":
                return "description";
            case "location":
                return "name";
            case "language":
                return "name";
            case "keyword":
                return "name";
            case "movie":
                return "title";
            case "character":
                return "name";
        }

        throw new RuntimeException("Unsupported type");
    }
}
