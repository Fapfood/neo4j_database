package fapfood.dragon_age_neo4j.repository;

import fapfood.dragon_age_neo4j.model.node.Location;
import fapfood.dragon_age_neo4j.model.node.Weapon;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeaponRepository extends Neo4jRepository<Weapon, Long> {
    Weapon findByName(String name);

    @Query("MATCH (w:Weapon)-[f:FIND_IN{sold_by_merchant: true}]->(l:Location) RETURN w")
    List<Weapon> findAllSoldInLocation(Location location);
}
