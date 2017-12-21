package fapfood.dragon_age_neo4j.repository;

import fapfood.dragon_age_neo4j.model.node.Location;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends Neo4jRepository<Location, Long> {
    Location findByName(String name);
}
