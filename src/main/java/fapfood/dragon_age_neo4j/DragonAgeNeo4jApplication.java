package fapfood.dragon_age_neo4j;

import fapfood.dragon_age_neo4j.model.node.*;
import fapfood.dragon_age_neo4j.model.relation.FindIn;
import fapfood.dragon_age_neo4j.repository.*;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.*;

import static org.neo4j.driver.v1.Values.parameters;

@org.springframework.context.annotation.Configuration
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan("fapfood.dragon_age_neo4j")
@EnableNeo4jRepositories("fapfood.dragon_age_neo4j.repository")
public class DragonAgeNeo4jApplication {
    private Session session;

    private final static Logger log = LoggerFactory.getLogger(DragonAgeNeo4jApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DragonAgeNeo4jApplication.class, args);
    }

    @Bean
    public SessionFactory getSessionFactory() {
        return new SessionFactory(configuration(), "fapfood.dragon_age_neo4j.model");
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(getSessionFactory());
    }

    @Bean
    public Configuration configuration() {
        Configuration embeddedConfiguration = new Configuration.Builder()
                .uri("file:///var/tmp/neo4j.db")
                .build(); //Embedded Driver
        Configuration httpConfiguration = new Configuration.Builder()
                .uri("http://neo4j:password@localhost:7474")
                .build(); //HTTP Driver
        Configuration boltConfiguration = new Configuration.Builder()
                .uri("bolt://neo4j:password@localhost:7687")
                .build(); //Bolt Driver
        return boltConfiguration;
    }

    @Bean
    CommandLineRunner demo(LocationRepository locationRepository,
                           WeaponRepository weaponRepository,
                           BootsRepository bootsRepository,
                           GlovesRepository glovesRepository,
                           ArmorRepository armorRepository,
                           CompanionRepository companionRepository) {
        session = getSessionFactory().openSession();

        return args -> {
            createData(locationRepository, weaponRepository, bootsRepository,
                    glovesRepository, armorRepository, companionRepository);

            Location denerim = locationRepository.findByName("Denerim");
            List<Weapon> weaponsSoldInDenerim = weaponRepository.findAllSoldInLocation(denerim);
            weaponsSoldInDenerim.forEach(weapon -> System.out.println(weapon.getName()));

            Location lothering = locationRepository.findByName("Lothering");
            findRelations(lothering.getName()).forEach(System.out::println);

            Armor ancientElvenArmor = armorRepository.findByName("Ancient Elven Armor");
            Armor chantryRobe = armorRepository.findByName("Chantry Robe");
            findShortestPath(ancientElvenArmor.getName(), chantryRobe.getName()).forEach(System.out::println);

            Weapon dagger = weaponRepository.findByName("Dagger");
            Armor clothing = armorRepository.findByName("Clothing");
            findAllPathShortenThen(clothing.getName(), dagger.getName(), 3).forEach(System.out::println);
        };
    }

    private Result findRelations(String name) {
        Transaction transaction = session.beginTransaction();
        Result result = session.query(
                String.format("MATCH (o1{name: '%s'})-[r]-(o2) RETURN r", name), new HashMap<>());
        transaction.commit();
        return result;
    }

    private Result findShortestPath(String name1, String name2) {
        Transaction transaction = session.beginTransaction();
        Result result = session.query(
                String.format("MATCH (o1 {name: '%s'}), (o2 {name: '%s'})," +
                        "p = shortestPath((o1)-[*]-(o2)) RETURN p", name1, name2), new HashMap<>());
        transaction.commit();
        return result;
    }

    private Result findAllPathShortenThen(String name1, String name2, int k) {
        Transaction transaction = session.beginTransaction();
        Result result = session.query(
                String.format("MATCH (o1{name: '%s'}), (o2{name: '%s'})," +
                        "p = (o1)-[*..%d]-(o2) RETURN p", name1, name2, k), new HashMap<>());
        transaction.commit();
        return result;
    }

    private void createData(LocationRepository locationRepository,
                            WeaponRepository weaponRepository,
                            BootsRepository bootsRepository,
                            GlovesRepository glovesRepository,
                            ArmorRepository armorRepository,
                            CompanionRepository companionRepository) {

        locationRepository.deleteAll();
        List<Location> locations = prepareLocations();
        locations.forEach(locationRepository::save);

        weaponRepository.deleteAll();
        List<Weapon> weapons = prepareWeapons();
        weapons.forEach(weaponRepository::save);

        bootsRepository.deleteAll();
        List<Boots> boots = prepareBoots();
        boots.forEach(bootsRepository::save);

        glovesRepository.deleteAll();
        List<Gloves> gloves = prepareGloves();
        gloves.forEach(glovesRepository::save);

        armorRepository.deleteAll();
        List<Armor> armors = prepareArmors();
        armors.forEach(armorRepository::save);

        companionRepository.deleteAll();
        List<Companion> companions = prepareCompanions();
        companions.forEach(companionRepository::save);

        updateWeapons(weaponRepository, locationRepository);
        updateBoots(bootsRepository, locationRepository);
        updateGloves(glovesRepository, locationRepository);
        updateArmors(armorRepository, locationRepository);
        updateCompanions(companionRepository, locationRepository, weaponRepository,
                bootsRepository, glovesRepository, armorRepository);
    }

    private List<Location> prepareLocations() {
        Location lothering = new Location();
        lothering.setName("Lothering");
        lothering.setType("village");

        Location korcariWilds = new Location();
        korcariWilds.setName("Korcari Wilds");
        korcariWilds.setType("forest");

        Location circleTower = new Location();
        circleTower.setName("Circle Tower");
        circleTower.setType("fortress");

        Location deepRoads = new Location();
        deepRoads.setName("Deep Roads");
        deepRoads.setType("cave");

        Location denerim = new Location();
        denerim.setName("Denerim");
        denerim.setType("fortress");

        Location orzammar = new Location();
        orzammar.setName("Orzammar");
        orzammar.setType("cave");

        Location brecilianForest = new Location();
        brecilianForest.setName("Brecilian Forest");
        brecilianForest.setType("forest");

        return Arrays.asList(lothering, korcariWilds, circleTower, deepRoads, denerim, orzammar, brecilianForest);
    }

    private List<Weapon> prepareWeapons() {
        Weapon battleaxe = new Weapon();
        battleaxe.setName("Battleaxe");
        battleaxe.setType("battleaxe");
        battleaxe.setDamage(10.00);
        battleaxe.setCriticalChance(0.03);
        battleaxe.setArmorPenetration(3.00);
        battleaxe.setSilverValue(13.33);

        Weapon wardensLongsword = new Weapon();
        wardensLongsword.setName("Warden's Longsword");
        wardensLongsword.setType("longsword");
        wardensLongsword.setDamage(7.00);
        wardensLongsword.setCriticalChance(0.02);
        wardensLongsword.setArmorPenetration(2.00);
        wardensLongsword.setSilverValue(15.00);

        Weapon enchantedDagger = new Weapon();
        enchantedDagger.setName("Enchanted Dagger");
        enchantedDagger.setType("dagger");
        enchantedDagger.setDamage(4.40);
        enchantedDagger.setCriticalChance(0.033);
        enchantedDagger.setArmorPenetration(4.60);
        enchantedDagger.setSilverValue(84.00);

        Weapon magicStaff = new Weapon();
        magicStaff.setName("Magic Staff");
        magicStaff.setType("staff");
        magicStaff.setDamage(4.00);
        magicStaff.setCriticalChance(0.0);
        magicStaff.setArmorPenetration(20.00);
        magicStaff.setSilverValue(65.00);

        Weapon enchantersStaff = new Weapon();
        enchantersStaff.setName("Enchanter's Staff");
        enchantersStaff.setType("staff");
        enchantersStaff.setDamage(4.00);
        enchantersStaff.setCriticalChance(0.0);
        enchantersStaff.setArmorPenetration(20.00);
        enchantersStaff.setSilverValue(290.00);

        Weapon acolytesStaff = new Weapon();
        acolytesStaff.setName("Acolyte's Staff");
        acolytesStaff.setType("staff");
        acolytesStaff.setDamage(4.00);
        acolytesStaff.setCriticalChance(0.0);
        acolytesStaff.setArmorPenetration(20.00);
        acolytesStaff.setSilverValue(55.00);

        Weapon barbarianAxe = new Weapon();
        barbarianAxe.setName("Barbarian Axe");
        barbarianAxe.setType("battleaxe");
        barbarianAxe.setDamage(10.00);
        barbarianAxe.setCriticalChance(0.03);
        barbarianAxe.setArmorPenetration(3.00);
        barbarianAxe.setSilverValue(13.00);

        Weapon dwarvenLongsword = new Weapon();
        dwarvenLongsword.setName("Dwarven Longsword");
        dwarvenLongsword.setType("longsword");
        dwarvenLongsword.setDamage(7.00);
        dwarvenLongsword.setCriticalChance(0.02);
        dwarvenLongsword.setArmorPenetration(2.00);
        dwarvenLongsword.setSilverValue(20.00);

        Weapon dagger = new Weapon();
        dagger.setName("Dagger");
        dagger.setType("dagger");
        dagger.setDamage(4.00);
        dagger.setCriticalChance(0.03);
        dagger.setArmorPenetration(4.00);
        dagger.setSilverValue(8.00);

        Weapon beastmansDagger = new Weapon();
        beastmansDagger.setName("Beastman's Dagger");
        beastmansDagger.setType("dagger");
        beastmansDagger.setDamage(5.60);
        beastmansDagger.setCriticalChance(0.042);
        beastmansDagger.setArmorPenetration(6.40);
        beastmansDagger.setSilverValue(158.00);

        return Arrays.asList(battleaxe, wardensLongsword, enchantedDagger, magicStaff, enchantersStaff,
                acolytesStaff, barbarianAxe, dwarvenLongsword, dagger, beastmansDagger);
    }

    private List<Boots> prepareBoots() {
        Boots splintmailBoots = new Boots();
        splintmailBoots.setName("Splintmail Boots");
        splintmailBoots.setType("medium_boots");
        splintmailBoots.setArmor(1.00);
        splintmailBoots.setFatigue(0.015);
        splintmailBoots.setSilverValue(16.00);

        Boots leatherBoots = new Boots();
        leatherBoots.setName("Leather Boots");
        leatherBoots.setType("light_boots");
        leatherBoots.setArmor(0.75);
        leatherBoots.setFatigue(0.005);
        leatherBoots.setSilverValue(8.00);

        Boots dwarvenHeavyBoots = new Boots();
        dwarvenHeavyBoots.setName("Dwarven Heavy Boots");
        dwarvenHeavyBoots.setType("heavy_boots");
        dwarvenHeavyBoots.setArmor(1.25);
        dwarvenHeavyBoots.setFatigue(0.0225);
        dwarvenHeavyBoots.setSilverValue(27.00);

        Boots ancientElvenBoots = new Boots();
        ancientElvenBoots.setName("Ancient Elven Boots");
        ancientElvenBoots.setType("medium_boots");
        ancientElvenBoots.setArmor(1.60);
        ancientElvenBoots.setFatigue(0.0172);
        ancientElvenBoots.setSilverValue(144.00);

        Boots heavyPlateBoots = new Boots();
        heavyPlateBoots.setName("Heavy Plate Boots");
        heavyPlateBoots.setType("massive_boots");
        heavyPlateBoots.setArmor(1.5);
        heavyPlateBoots.setFatigue(0.03);
        heavyPlateBoots.setSilverValue(40.00);

        return Arrays.asList(splintmailBoots, leatherBoots, dwarvenHeavyBoots, ancientElvenBoots, heavyPlateBoots);
    }

    private List<Gloves> prepareGloves() {
        Gloves splintmailGloves = new Gloves();
        splintmailGloves.setName("Splintmail Gloves");
        splintmailGloves.setType("medium_gloves");
        splintmailGloves.setArmor(0.75);
        splintmailGloves.setFatigue(0.0125);
        splintmailGloves.setSilverValue(12.00);

        Gloves dwarvenHeavyGloves = new Gloves();
        dwarvenHeavyGloves.setName("Dwarven Heavy Gloves");
        dwarvenHeavyGloves.setType("heavy_gloves");
        dwarvenHeavyGloves.setArmor(1.00);
        dwarvenHeavyGloves.setFatigue(0.0175);
        dwarvenHeavyGloves.setSilverValue(23.00);

        Gloves ancientElvenGloves = new Gloves();
        ancientElvenGloves.setName("Ancient Elven Gloves");
        ancientElvenGloves.setType("medium_gloves");
        ancientElvenGloves.setArmor(1.20);
        ancientElvenGloves.setFatigue(0.0144);
        ancientElvenGloves.setSilverValue(198.00);

        Gloves leatherGloves = new Gloves();
        leatherGloves.setName("Leather Gloves");
        leatherGloves.setType("light_gloves");
        leatherGloves.setArmor(0.50);
        leatherGloves.setFatigue(0.01);
        leatherGloves.setSilverValue(7.00);

        Gloves heavyPlateGloves = new Gloves();
        heavyPlateGloves.setName("Heavy Plate Gloves");
        heavyPlateGloves.setType("massive_gloves");
        heavyPlateGloves.setArmor(1.25);
        heavyPlateGloves.setFatigue(0.03);
        heavyPlateGloves.setSilverValue(30.00);

        return Arrays.asList(splintmailGloves, dwarvenHeavyGloves, ancientElvenGloves, leatherGloves, heavyPlateGloves);
    }

    private List<Armor> prepareArmors() {
        Armor splintmail = new Armor();
        splintmail.setName("Splintmail");
        splintmail.setType("medium_chestpiece");
        splintmail.setArmor(4.25);
        splintmail.setFatigue(0.07);
        splintmail.setSilverValue(50.00);

        Armor chantryRobe = new Armor();
        chantryRobe.setName("Chantry Robe");
        chantryRobe.setType("clothing");
        chantryRobe.setArmor(0.0);
        chantryRobe.setFatigue(0.0);
        chantryRobe.setSilverValue(90.00);

        Armor morrigansRobes = new Armor();
        morrigansRobes.setName("Morrigan's Robes");
        morrigansRobes.setType("clothing");
        morrigansRobes.setArmor(0.0);
        morrigansRobes.setFatigue(0.0);
        morrigansRobes.setSilverValue(210.00);

        Armor dwarvenHeavyArmor = new Armor();
        dwarvenHeavyArmor.setName("Dwarven Heavy Armor");
        dwarvenHeavyArmor.setType("heavy_chestpiece");
        dwarvenHeavyArmor.setArmor(6.25);
        dwarvenHeavyArmor.setFatigue(0.14);
        dwarvenHeavyArmor.setSilverValue(70.00);

        Armor clothing = new Armor();
        clothing.setName("Clothing");
        clothing.setType("clothing");
        clothing.setArmor(0.0);
        clothing.setFatigue(0.0);
        clothing.setSilverValue(11.00);

        Armor seniorEnchantersRobes = new Armor();
        seniorEnchantersRobes.setName("Senior Enchanter's Robes");
        seniorEnchantersRobes.setType("clothing");
        seniorEnchantersRobes.setArmor(0.0);
        seniorEnchantersRobes.setFatigue(0.0);
        seniorEnchantersRobes.setSilverValue(300.00);

        Armor ancientElvenArmor = new Armor();
        ancientElvenArmor.setName("Ancient Elven Armor");
        ancientElvenArmor.setType("medium_chestpiece");
        ancientElvenArmor.setArmor(6.80);
        ancientElvenArmor.setFatigue(0.0805);
        ancientElvenArmor.setSilverValue(450.00);

        Armor leatherArmor = new Armor();
        leatherArmor.setName("Leather Armor");
        leatherArmor.setType("light_chestpiece");
        leatherArmor.setArmor(3.00);
        leatherArmor.setFatigue(0.02);
        leatherArmor.setSilverValue(20.00);

        Armor heavyPlateArmor = new Armor();
        heavyPlateArmor.setName("Heavy Plate Armor");
        heavyPlateArmor.setType("massive_chestpiece");
        heavyPlateArmor.setArmor(8.75);
        heavyPlateArmor.setFatigue(0.021);
        heavyPlateArmor.setSilverValue(90.00);

        return Arrays.asList(splintmail, chantryRobe, morrigansRobes, dwarvenHeavyArmor, clothing, seniorEnchantersRobes,
                ancientElvenArmor, leatherArmor, heavyPlateArmor);
    }

    private List<Companion> prepareCompanions() {
        Companion alistair = new Companion();
        alistair.setName("Alistair");
        alistair.setStrength(1.5);
        alistair.setDexterity(0.8);
        alistair.setWillpower(0.4);
        alistair.setMagic(0.0);
        alistair.setCunning(0.0);
        alistair.setConstitution(0.9);

        Companion leliana = new Companion();
        leliana.setName("Leliana");
        leliana.setStrength(0.6);
        leliana.setDexterity(1.2);
        leliana.setWillpower(0.4);
        leliana.setMagic(0.0);
        leliana.setCunning(1.2);
        leliana.setConstitution(0.6);

        Companion morrigan = new Companion();
        morrigan.setName("Morrigan");
        morrigan.setStrength(0.125);
        morrigan.setDexterity(0.25);
        morrigan.setWillpower(1.125);
        morrigan.setMagic(1.5);
        morrigan.setCunning(0.0);
        morrigan.setConstitution(0.0);

        Companion oghren = new Companion();
        oghren.setName("Oghren");
        oghren.setStrength(1.4);
        oghren.setDexterity(0.25);
        oghren.setWillpower(0.0);
        oghren.setMagic(0.0);
        oghren.setCunning(0.0);
        oghren.setConstitution(0.0);

        Companion sten = new Companion();
        sten.setName("Sten");
        sten.setStrength(2.0);
        sten.setDexterity(0.25);
        sten.setWillpower(0.25);
        sten.setMagic(0.5);
        sten.setCunning(0.0);
        sten.setConstitution(0.0);

        Companion wynne = new Companion();
        wynne.setName("Wynne");
        wynne.setStrength(0.0);
        wynne.setDexterity(0.0);
        wynne.setWillpower(1.0);
        wynne.setMagic(1.25);
        wynne.setCunning(0.25);
        wynne.setConstitution(0.5);

        return Arrays.asList(alistair, leliana, morrigan, oghren, sten, wynne);
    }

    private void updateWeapons(WeaponRepository weaponRepository, LocationRepository locationRepository) {
        Location lothering = locationRepository.findByName("Lothering");
        Location korcariWilds = locationRepository.findByName("Korcari Wilds");
        Location circleTower = locationRepository.findByName("Circle Tower");
        Location deepRoads = locationRepository.findByName("Deep Roads");
        Location denerim = locationRepository.findByName("Denerim");
        Location orzammar = locationRepository.findByName("Orzammar");
        Location brecilianForest = locationRepository.findByName("Brecilian Forest");

        Weapon battleaxe = weaponRepository.findByName("Battleaxe");
        battleaxe.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(battleaxe, deepRoads, true, false, true),
                new FindIn(battleaxe, denerim, false, true, false)
        )));

        Weapon wardensLongsword = weaponRepository.findByName("Warden's Longsword");
        wardensLongsword.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(wardensLongsword, korcariWilds, true, false, false),
                new FindIn(wardensLongsword, denerim, true, false, false)
        )));

        Weapon enchantedDagger = weaponRepository.findByName("Enchanted Dagger");
        enchantedDagger.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(enchantedDagger, korcariWilds, false, false, true)
        )));

        Weapon magicStaff = weaponRepository.findByName("Magic Staff");
        magicStaff.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(magicStaff, denerim, false, true, false),
                new FindIn(magicStaff, circleTower, true, true, true)
        )));

        Weapon enchantersStaff = weaponRepository.findByName("Enchanter's Staff");
        enchantersStaff.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(enchantersStaff, lothering, false, true, false),
                new FindIn(enchantersStaff, circleTower, true, false, true),
                new FindIn(enchantersStaff, denerim, true, true, false)
        )));

        Weapon acolytesStaff = weaponRepository.findByName("Acolyte's Staff");
        acolytesStaff.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(acolytesStaff, circleTower, true, true, true)
        )));

        Weapon barbarianAxe = weaponRepository.findByName("Barbarian Axe");
        barbarianAxe.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(barbarianAxe, orzammar, true, true, false),
                new FindIn(barbarianAxe, deepRoads, true, false, true)
        )));

        Weapon dwarvenLongsword = weaponRepository.findByName("Dwarven Longsword");
        dwarvenLongsword.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(dwarvenLongsword, orzammar, true, true, true),
                new FindIn(dwarvenLongsword, lothering, false, true, false),
                new FindIn(dwarvenLongsword, deepRoads, false, false, true)
        )));

        Weapon dagger = weaponRepository.findByName("Dagger");
        dagger.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(dagger, denerim, true, true, true),
                new FindIn(dagger, lothering, true, true, true),
                new FindIn(dagger, circleTower, true, false, false),
                new FindIn(dagger, korcariWilds, false, false, true),
                new FindIn(dagger, brecilianForest, true, true, false)
        )));

        Weapon beastmansDagger = weaponRepository.findByName("Beastman's Dagger");
        beastmansDagger.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(beastmansDagger, circleTower, true, false, false),
                new FindIn(beastmansDagger, brecilianForest, true, true, false)
        )));

        Arrays.asList(battleaxe, wardensLongsword, enchantedDagger, magicStaff, enchantersStaff,
                acolytesStaff, barbarianAxe, dwarvenLongsword, dagger, beastmansDagger)
                .forEach(weaponRepository::save);
    }

    private void updateBoots(BootsRepository bootsRepository, LocationRepository locationRepository) {
        Location lothering = locationRepository.findByName("Lothering");
        Location circleTower = locationRepository.findByName("Circle Tower");
        Location deepRoads = locationRepository.findByName("Deep Roads");
        Location denerim = locationRepository.findByName("Denerim");
        Location orzammar = locationRepository.findByName("Orzammar");
        Location brecilianForest = locationRepository.findByName("Brecilian Forest");

        Boots splintmailBoots = bootsRepository.findByName("Splintmail Boots");
        splintmailBoots.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(splintmailBoots, lothering, false, true, false),
                new FindIn(splintmailBoots, denerim, false, true, true),
                new FindIn(splintmailBoots, circleTower, true, true, false)
        )));

        Boots dwarvenHeavyBoots = bootsRepository.findByName("Dwarven Heavy Boots");
        dwarvenHeavyBoots.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(dwarvenHeavyBoots, orzammar, true, true, true),
                new FindIn(dwarvenHeavyBoots, denerim, false, true, false),
                new FindIn(dwarvenHeavyBoots, deepRoads, true, false, true)
        )));

        Boots ancientElvenBoots = bootsRepository.findByName("Ancient Elven Boots");
        ancientElvenBoots.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(ancientElvenBoots, brecilianForest, true, true, false)
        )));

        Boots leatherBoots = bootsRepository.findByName("Leather Boots");
        leatherBoots.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(leatherBoots, lothering, false, true, true),
                new FindIn(leatherBoots, denerim, true, true, true)
        )));

        Boots heavyPlateBoots = bootsRepository.findByName("Heavy Plate Boots");
        heavyPlateBoots.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(heavyPlateBoots, denerim, false, true, false)
        )));

        Arrays.asList(splintmailBoots, dwarvenHeavyBoots, ancientElvenBoots, leatherBoots, heavyPlateBoots)
                .forEach(bootsRepository::save);
    }

    private void updateGloves(GlovesRepository glovesRepository, LocationRepository locationRepository) {
        Location lothering = locationRepository.findByName("Lothering");
        Location circleTower = locationRepository.findByName("Circle Tower");
        Location deepRoads = locationRepository.findByName("Deep Roads");
        Location denerim = locationRepository.findByName("Denerim");
        Location orzammar = locationRepository.findByName("Orzammar");
        Location brecilianForest = locationRepository.findByName("Brecilian Forest");

        Gloves splintmailGloves = glovesRepository.findByName("Splintmail Gloves");
        splintmailGloves.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(splintmailGloves, lothering, false, true, false),
                new FindIn(splintmailGloves, denerim, false, true, true),
                new FindIn(splintmailGloves, circleTower, true, true, false)
        )));

        Gloves dwarvenHeavyGloves = glovesRepository.findByName("Dwarven Heavy Gloves");
        dwarvenHeavyGloves.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(dwarvenHeavyGloves, orzammar, true, true, true),
                new FindIn(dwarvenHeavyGloves, denerim, false, true, false),
                new FindIn(dwarvenHeavyGloves, deepRoads, true, false, true)
        )));

        Gloves ancientElvenGloves = glovesRepository.findByName("Ancient Elven Gloves");
        ancientElvenGloves.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(ancientElvenGloves, brecilianForest, true, true, false)
        )));

        Gloves leatherGloves = glovesRepository.findByName("Leather Gloves");
        leatherGloves.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(leatherGloves, lothering, false, true, true),
                new FindIn(leatherGloves, denerim, true, true, true)
        )));

        Gloves heavyPlateGloves = glovesRepository.findByName("Heavy Plate Gloves");
        heavyPlateGloves.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(heavyPlateGloves, denerim, false, true, false)
        )));

        Arrays.asList(splintmailGloves, dwarvenHeavyGloves, ancientElvenGloves, leatherGloves, heavyPlateGloves)
                .forEach(glovesRepository::save);
    }

    private void updateArmors(ArmorRepository armorRepository, LocationRepository locationRepository) {
        Location lothering = locationRepository.findByName("Lothering");
        Location circleTower = locationRepository.findByName("Circle Tower");
        Location deepRoads = locationRepository.findByName("Deep Roads");
        Location denerim = locationRepository.findByName("Denerim");
        Location orzammar = locationRepository.findByName("Orzammar");
        Location brecilianForest = locationRepository.findByName("Brecilian Forest");

        Armor splintmail = armorRepository.findByName("Splintmail");
        splintmail.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(splintmail, lothering, false, true, false),
                new FindIn(splintmail, denerim, false, true, true),
                new FindIn(splintmail, circleTower, true, true, false)
        )));

        Armor dwarvenHeavyArmor = armorRepository.findByName("Dwarven Heavy Armor");
        dwarvenHeavyArmor.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(dwarvenHeavyArmor, orzammar, true, true, true),
                new FindIn(dwarvenHeavyArmor, denerim, false, true, false),
                new FindIn(dwarvenHeavyArmor, deepRoads, true, false, true)
        )));

        Armor ancientElvenArmor = armorRepository.findByName("Ancient Elven Armor");
        ancientElvenArmor.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(ancientElvenArmor, brecilianForest, true, true, false)
        )));

        Armor leatherArmor = armorRepository.findByName("Leather Armor");
        leatherArmor.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(leatherArmor, lothering, false, true, true),
                new FindIn(leatherArmor, denerim, true, true, true)
        )));

        Armor heavyPlateArmor = armorRepository.findByName("Heavy Plate Armor");
        heavyPlateArmor.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(heavyPlateArmor, denerim, false, true, false)
        )));

        Armor clothing = armorRepository.findByName("Clothing");
        clothing.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(clothing, denerim, true, true, false),
                new FindIn(clothing, lothering, true, true, true),
                new FindIn(clothing, circleTower, true, true, false),
                new FindIn(clothing, orzammar, true, true, false),
                new FindIn(clothing, brecilianForest, true, true, false)
        )));

        Armor seniorEnchantersRobes = armorRepository.findByName("Senior Enchanter's Robes");
        seniorEnchantersRobes.setLocations(new HashSet<>(Arrays.asList(
                new FindIn(seniorEnchantersRobes, denerim, false, true, false),
                new FindIn(seniorEnchantersRobes, circleTower, true, true, false)
        )));

        Arrays.asList(splintmail, dwarvenHeavyArmor, ancientElvenArmor, leatherArmor,
                heavyPlateArmor, clothing, seniorEnchantersRobes)
                .forEach(armorRepository::save);
    }

    private void updateCompanions(CompanionRepository companionRepository, LocationRepository locationRepository,
                                  WeaponRepository weaponRepository, BootsRepository bootsRepository,
                                  GlovesRepository glovesRepository, ArmorRepository armorRepository) {

        Location lothering = locationRepository.findByName("Lothering");
        Location korcariWilds = locationRepository.findByName("Korcari Wilds");
        Location circleTower = locationRepository.findByName("Circle Tower");
        Location deepRoads = locationRepository.findByName("Deep Roads");

        Weapon battleaxe = weaponRepository.findByName("Battleaxe");
        Weapon wardensLongsword = weaponRepository.findByName("Warden's Longsword");
        Weapon enchantedDagger = weaponRepository.findByName("Enchanted Dagger");
        Weapon magicStaff = weaponRepository.findByName("Magic Staff");
        Weapon enchantersStaff = weaponRepository.findByName("Enchanter's Staff");

        Boots splintmailBoots = bootsRepository.findByName("Splintmail Boots");
        Boots leatherBoots = bootsRepository.findByName("Leather Boots");
        Boots dwarvenHeavyBoots = bootsRepository.findByName("Dwarven Heavy Boots");

        Gloves splintmailGloves = glovesRepository.findByName("Splintmail Gloves");
        Gloves dwarvenHeavyGloves = glovesRepository.findByName("Dwarven Heavy Gloves");

        Armor splintmail = armorRepository.findByName("Splintmail");
        Armor chantryRobe = armorRepository.findByName("Chantry Robe");
        Armor morrigansRobes = armorRepository.findByName("Morrigan's Robes");
        Armor dwarvenHeavyArmor = armorRepository.findByName("Dwarven Heavy Armor");
        Armor clothing = armorRepository.findByName("Clothing");
        Armor seniorEnchantersRobes = armorRepository.findByName("Senior Enchanter's Robes");

        Companion alistair = companionRepository.findByName("Alistair");
        alistair.setLocation(korcariWilds);
        alistair.setWeapon(wardensLongsword);
        alistair.setBoots(splintmailBoots);
        alistair.setGloves(splintmailGloves);
        alistair.setArmor(splintmail);

        Companion leliana = companionRepository.findByName("Leliana");
        leliana.setLocation(lothering);
        leliana.setWeapon(enchantedDagger);
        leliana.setBoots(leatherBoots);
        leliana.setArmor(chantryRobe);

        Companion morrigan = companionRepository.findByName("Morrigan");
        morrigan.setLocation(korcariWilds);
        morrigan.setWeapon(magicStaff);
        morrigan.setArmor(morrigansRobes);

        Companion oghren = companionRepository.findByName("Oghren");
        oghren.setLocation(deepRoads);
        oghren.setWeapon(battleaxe);
        oghren.setBoots(dwarvenHeavyBoots);
        oghren.setGloves(dwarvenHeavyGloves);
        oghren.setArmor(dwarvenHeavyArmor);

        Companion sten = companionRepository.findByName("Sten");
        sten.setLocation(lothering);
        sten.setArmor(clothing);

        Companion wynne = companionRepository.findByName("Wynne");
        wynne.setLocation(circleTower);
        wynne.setWeapon(enchantersStaff);
        wynne.setArmor(seniorEnchantersRobes);

        Arrays.asList(alistair, leliana, morrigan, oghren, sten, wynne)
                .forEach(companionRepository::save);
    }
}
