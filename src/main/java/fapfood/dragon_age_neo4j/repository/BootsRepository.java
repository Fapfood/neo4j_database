package fapfood.dragon_age_neo4j.repository;

import fapfood.dragon_age_neo4j.model.node.Boots;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BootsRepository extends Neo4jRepository<Boots, Long> {
    Boots findByName(String name);
}