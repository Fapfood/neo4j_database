package fapfood.dragon_age_neo4j.model.node;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;


@Data
@NoArgsConstructor
@NodeEntity
public class Companion {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Property(name = "initial_strength")
    private Double strength;

    @Property(name = "initial_dexterity")
    private Double dexterity;

    @Property(name = "initial_willpower")
    private Double willpower;

    @Property(name = "initial_magic")
    private Double magic;

    @Property(name = "initial_cunning")
    private Double cunning;

    @Property(name = "initial_constitution")
    private Double constitution;

    @Relationship(type = "MEET_IN")
    private Location location;

    @Relationship(type = "WEAR")
    private Armor armor;

    @Relationship(type = "WEAR")
    private Gloves gloves;

    @Relationship(type = "WEAR")
    private Boots boots;

    @Relationship(type = "HOLD")
    private Weapon weapon;
}
