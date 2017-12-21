package fapfood.dragon_age_neo4j.repository;

import fapfood.dragon_age_neo4j.model.node.Companion;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanionRepository extends Neo4jRepository<Companion, Long> {
    Companion findByName(String name);
}
