package fapfood.dragon_age_neo4j.repository;

import fapfood.dragon_age_neo4j.model.node.Armor;
import fapfood.dragon_age_neo4j.model.node.Location;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArmorRepository extends Neo4jRepository<Armor, Long> {
    Armor findByName(String name);
}