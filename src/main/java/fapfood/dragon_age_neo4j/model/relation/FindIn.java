package fapfood.dragon_age_neo4j.model.relation;

import fapfood.dragon_age_neo4j.model.node.Location;
import fapfood.dragon_age_neo4j.model.node.MetaEquipment;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

@Data
@NoArgsConstructor
@RelationshipEntity(type = "FIND_IN")
public class FindIn {

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private MetaEquipment equipment;

    @EndNode
    private Location location;

    @Property(name = "found_in_chest")
    private Boolean foundInChest;

    @Property(name = "sold_by_merchant")
    private Boolean soldByMerchant;

    @Property(name = "dropped_from_opponent")
    private Boolean droppedFromOpponent;

    public FindIn(MetaEquipment equipment, Location location,
                  Boolean foundInChest, Boolean soldByMerchant, Boolean droppedFromOpponent) {
        this.equipment = equipment;
        this.location = location;
        this.foundInChest = foundInChest;
        this.soldByMerchant = soldByMerchant;
        this.droppedFromOpponent = droppedFromOpponent;
    }
}
