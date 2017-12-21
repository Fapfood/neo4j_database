package fapfood.dragon_age_neo4j.model.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NoArgsConstructor
@NodeEntity
public class Location {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String type;
}
