package fapfood.dragon_age_neo4j.model.node;

import fapfood.dragon_age_neo4j.model.relation.FindIn;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.Set;

@Data
@NoArgsConstructor
@NodeEntity
public abstract class MetaEquipment {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String type;

    @Property(name = "silver_value")
    private Double silverValue;

    @Relationship(type = "FIND_IN")
    private Set<FindIn> locations;
}
