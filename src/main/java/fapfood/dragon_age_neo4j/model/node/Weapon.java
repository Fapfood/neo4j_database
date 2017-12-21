package fapfood.dragon_age_neo4j.model.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NoArgsConstructor
@NodeEntity
public class Weapon extends MetaEquipment {

    private Double damage;

    @Property(name = "critical_chance")
    private Double criticalChance;

    @Property(name = "armor_penetration")
    private Double armorPenetration;
}
