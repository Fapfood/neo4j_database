package fapfood.dragon_age_neo4j.repository;

import fapfood.dragon_age_neo4j.model.node.Gloves;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlovesRepository extends Neo4jRepository<Gloves, Long> {
    Gloves findByName(String name);
}