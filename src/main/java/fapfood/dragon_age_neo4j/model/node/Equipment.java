package fapfood.dragon_age_neo4j.model.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NoArgsConstructor
@NodeEntity
public abstract class Equipment extends MetaEquipment {

    private Double armor;

    private Double fatigue;
}
