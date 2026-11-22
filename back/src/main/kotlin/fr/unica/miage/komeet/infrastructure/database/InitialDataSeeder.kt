package fr.unica.miage.komeet.infrastructure.database

import fr.unica.miage.komeet.domain.model.Location
import fr.unica.miage.komeet.domain.model.Profile
import fr.unica.miage.komeet.domain.model.Region
import fr.unica.miage.komeet.domain.model.User
import fr.unica.miage.komeet.domain.repository.ProfileRepository
import fr.unica.miage.komeet.domain.repository.RegionRepository
import fr.unica.miage.komeet.domain.repository.UserRepository
import fr.unica.miage.komeet.infrastructure.security.PasswordHasher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.Instant

object InitialDataSeeder {

    private val logger = LoggerFactory.getLogger(InitialDataSeeder::class.java)

    val defaultRegions = listOf(
        // MAROC (12 régions officielles)
        Region("reg-ma-1", "Casablanca-Settat", "Maroc", "06"),
        Region("reg-ma-2", "Marrakech-Safi", "Maroc", "07"),
        Region("reg-ma-3", "Rabat-Salé-Kénitra", "Maroc", "04"),
        Region("reg-ma-4", "Tanger-Tétouan-Al Hoceïma", "Maroc", "01"),
        Region("reg-ma-5", "Fès-Meknès", "Maroc", "03"),
        Region("reg-ma-6", "Souss-Massa", "Maroc", "09"),
        Region("reg-ma-7", "L'Oriental", "Maroc", "02"),
        Region("reg-ma-8", "Béni Mellal-Khénifra", "Maroc", "05"),
        Region("reg-ma-9", "Drâa-Tafilalet", "Maroc", "08"),
        Region("reg-ma-10", "Guelmim-Oued Noun", "Maroc", "10"),
        Region("reg-ma-11", "Laâyoune-Sakia El Hamra", "Maroc", "11"),
        Region("reg-ma-12", "Dakhla-Oued Ed-Dahab", "Maroc", "12"),

        // FRANCE (8 régions principales)
        Region("reg-fr-1", "Île-de-France", "France", "11"),
        Region("reg-fr-2", "Provence-Alpes-Côte d'Azur", "France", "93"),
        Region("reg-fr-3", "Auvergne-Rhône-Alpes", "France", "84"),
        Region("reg-fr-4", "Nouvelle-Aquitaine", "France", "75"),
        Region("reg-fr-5", "Occitanie", "France", "76"),
        Region("reg-fr-6", "Hauts-de-France", "France", "32"),
        Region("reg-fr-7", "Grand Est", "France", "44"),
        Region("reg-fr-8", "Bretagne", "France", "53"),

        // USA (6 régions)
        Region("reg-us-1", "California", "USA", "CA"),
        Region("reg-us-2", "New York", "USA", "NY"),
        Region("reg-us-3", "Texas", "USA", "TX"),
        Region("reg-us-4", "Florida", "USA", "FL"),
        Region("reg-us-5", "Washington", "USA", "WA"),
        Region("reg-us-6", "Illinois", "USA", "IL"),

        // JAPON (5 régions)
        Region("reg-jp-1", "Kanto", "Japon", "KT"),
        Region("reg-jp-2", "Kansai", "Japon", "KS"),
        Region("reg-jp-3", "Hokkaido", "Japon", "HK"),
        Region("reg-jp-4", "Chubu", "Japon", "CB"),
        Region("reg-jp-5", "Kyushu", "Japon", "KY"),

        // ARABIE SAOUDITE (5 régions)
        Region("reg-sa-1", "Riyadh Region", "Arabie Saoudite", "01"),
        Region("reg-sa-2", "Makkah Region", "Arabie Saoudite", "02"),
        Region("reg-sa-3", "Eastern Province", "Arabie Saoudite", "04"),
        Region("reg-sa-4", "Medina Region", "Arabie Saoudite", "03"),
        Region("reg-sa-5", "Asir Region", "Arabie Saoudite", "06")
    )

    fun seed(
        db: Database,
        regionRepository: RegionRepository,
        profileRepository: ProfileRepository,
        userRepository: UserRepository,
        passwordHasher: PasswordHasher
    ) {
        logger.info("Checking and seeding regions and international profiles...")

        // 1. Seed / update regions
        regionRepository.saveAll(defaultRegions)
        logger.info("Successfully verified ${defaultRegions.size} default regions.")

        // 2. Always upsert international profiles for ALL regions across all countries
        val internationalProfiles = buildInternationalProfiles()
        profileRepository.saveAll(internationalProfiles)
        logger.info("Successfully seeded/updated ${internationalProfiles.size} international profiles across ALL regions.")

        // 3. Harmonize French profiles to official French regions
        transaction(db) {
            Profiles.selectAll().where { Profiles.country.lowerCase() eq "france" }.forEach { row ->
                val id = row[Profiles.id]
                val currentReg = row[Profiles.region] ?: ""
                val dept = row[Profiles.department] ?: ""
                val mappedRegion = mapFrenchDepartmentToRegion(dept, currentReg)
                Profiles.update({ Profiles.id eq id }) {
                    it[region] = mappedRegion
                }
            }
        }

        // 4. Seed demo users
        seedDemoUsers(userRepository, passwordHasher)
    }

    private fun mapFrenchDepartmentToRegion(dept: String, currentRegion: String): String {
        val text = "$dept $currentRegion".lowercase()
        return when {
            text.contains("paris") || text.contains("75") || text.contains("seine") || text.contains("essonne") ||
                    text.contains("marne") || text.contains("val") || text.contains("île-de-france") -> "Île-de-France"
            text.contains("alpes-maritimes") || text.contains("06") || text.contains("nice") || text.contains("marseille") ||
                    text.contains("vaucluse") || text.contains("hautes-alpes") || text.contains("côte d'azur") -> "Provence-Alpes-Côte d'Azur"
            text.contains("rhône") || text.contains("69") || text.contains("lyon") || text.contains("haute-loire") ||
                    text.contains("auvergne") -> "Auvergne-Rhône-Alpes"
            text.contains("gironde") || text.contains("creuse") || text.contains("deux-sèvres") || text.contains("vienne") ||
                    text.contains("bordeaux") || text.contains("aquitaine") -> "Nouvelle-Aquitaine"
            text.contains("toulouse") || text.contains("garonne") || text.contains("pyrénées") || text.contains("lot") ||
                    text.contains("lozère") || text.contains("tarn") || text.contains("occitanie") -> "Occitanie"
            text.contains("aisne") || text.contains("nord") || text.contains("somme") || text.contains("lille") ||
                    text.contains("hauts-de-france") -> "Hauts-de-France"
            text.contains("ardennes") || text.contains("rhin") || text.contains("moselle") || text.contains("strasbourg") ||
                    text.contains("grand est") -> "Grand Est"
            text.contains("ille-et-vilaine") || text.contains("côtes-d'armor") || text.contains("morbihan") || text.contains("rennes") ||
                    text.contains("brest") || text.contains("bretagne") -> "Bretagne"
            else -> "Île-de-France"
        }
    }

    private fun seedDemoUsers(userRepository: UserRepository, passwordHasher: PasswordHasher) {
        val demoUsers = listOf(
            User(
                id = "usr-achraf",
                email = "achraf@komeet.ma",
                passwordHash = passwordHasher.hash("AchrafPassword123"),
                displayName = "Achraf Khairouni",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                region = "Casablanca-Settat"
            ),
            User(
                id = "usr-alice",
                email = "alice@komeet.fr",
                passwordHash = passwordHasher.hash("AlicePassword123"),
                displayName = "Alice Dupont",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                region = "Île-de-France"
            ),
            User(
                id = "usr-michael",
                email = "michael@komeet.us",
                passwordHash = passwordHasher.hash("MichaelPassword123"),
                displayName = "Michael Miller",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                region = "California"
            ),
            User(
                id = "usr-kenji",
                email = "kenji@komeet.jp",
                passwordHash = passwordHasher.hash("KenjiPassword123"),
                displayName = "Kenji Sato",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                region = "Kanto"
            ),
            User(
                id = "usr-fahad",
                email = "fahad@komeet.sa",
                passwordHash = passwordHasher.hash("FahadPassword123"),
                displayName = "Fahad Al-Otaibi",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                region = "Riyadh Region"
            )
        )

        demoUsers.forEach { user ->
            if (userRepository.findByEmail(user.email) == null) {
                userRepository.save(user)
            }
        }
    }

    private fun p(
        id: String,
        firstName: String,
        lastName: String,
        email: String,
        age: Int,
        gender: String,
        phone: String,
        streetNum: Int,
        streetName: String,
        city: String,
        region: String,
        postcode: String,
        country: String,
        photoId: String,
        now: Instant
    ): Profile {
        return Profile(
            id = id,
            firstName = firstName,
            lastName = lastName,
            email = email,
            age = age,
            gender = gender,
            phone = phone,
            location = Location(
                streetNumber = streetNum,
                streetName = streetName,
                city = city,
                department = region,
                region = region,
                postcode = postcode,
                country = country
            ),
            thumbnailUrl = "https://images.unsplash.com/$photoId?w=150&h=150&fit=crop&crop=faces",
            pictureUrl = "https://images.unsplash.com/$photoId?w=800&h=1000&fit=crop&crop=faces",
            source = "komeet-seed",
            sourceId = id,
            createdAt = now
        )
    }

    private fun buildInternationalProfiles(): List<Profile> {
        val now = Instant.now()
        val list = mutableListOf<Profile>()

        // ==========================================
        // 1. MAROC - 12 Régions (1 Homme + 1 Femme par région)
        // ==========================================
        // 1. Casablanca-Settat
        list.add(p("prof-ma-1", "Yassine", "Benali", "yassine.benali@komeet.ma", 26, "male", "+212 6 12 34 56 78", 14, "Boulevard d'Anfa", "Casablanca", "Casablanca-Settat", "20000", "Maroc", "photo-1507003211169-0a1dd7228f2d", now))
        list.add(p("prof-ma-2", "Kenza", "Alami", "kenza.alami@komeet.ma", 23, "female", "+212 6 22 33 44 55", 18, "Boulevard Zerktouni", "Casablanca", "Casablanca-Settat", "20100", "Maroc", "photo-1517841905240-472988babdf9", now))

        // 2. Marrakech-Safi
        list.add(p("prof-ma-3", "Othmane", "Berrada", "othmane.berrada@komeet.ma", 27, "male", "+212 6 33 44 55 66", 7, "Avenue Mohammed VI", "Marrakech", "Marrakech-Safi", "40000", "Maroc", "photo-1506794778202-cad84cf45f1d", now))
        list.add(p("prof-ma-4", "Salma", "El Amrani", "salma.amrani@komeet.ma", 24, "female", "+212 6 98 76 54 32", 8, "Hivernage", "Marrakech", "Marrakech-Safi", "40020", "Maroc", "photo-1494790108377-be9c29b29330", now))

        // 3. Rabat-Salé-Kénitra
        list.add(p("prof-ma-5", "Mehdi", "Chraibi", "mehdi.chraibi@komeet.ma", 28, "male", "+212 6 55 44 33 22", 22, "Avenue Annakhil, Hay Riad", "Rabat", "Rabat-Salé-Kénitra", "10000", "Maroc", "photo-1500648767791-00dcc994a43e", now))
        list.add(p("prof-ma-6", "Fatima-Zahra", "Bennani", "fz.bennani@komeet.ma", 25, "female", "+212 6 44 55 66 77", 12, "Avenue de France, Agdal", "Rabat", "Rabat-Salé-Kénitra", "10090", "Maroc", "photo-1524504388940-b1c1722653e1", now))

        // 4. Tanger-Tétouan-Al Hoceïma
        list.add(p("prof-ma-7", "Hamza", "Mansouri", "hamza.mansouri@komeet.ma", 29, "male", "+212 6 66 77 88 99", 3, "Boulevard Pasteur", "Tanger", "Tanger-Tétouan-Al Hoceïma", "90000", "Maroc", "photo-1522075469751-3a6694fb2f61", now))
        list.add(p("prof-ma-8", "Yasmina", "Bazi", "yasmina.bazi@komeet.ma", 22, "female", "+212 6 11 99 88 77", 15, "Avenue Hassan II", "Tanger", "Tanger-Tétouan-Al Hoceïma", "90050", "Maroc", "photo-1544005313-94ddf0286df2", now))

        // 5. Fès-Meknès
        list.add(p("prof-ma-9", "Taha", "Filali", "taha.filali@komeet.ma", 27, "male", "+212 6 13 57 91 35", 19, "Avenue Hassan II", "Fès", "Fès-Meknès", "30000", "Maroc", "photo-1519085360753-af0119f7cbe7", now))
        list.add(p("prof-ma-10", "Nouhaila", "Sqalli", "nouhaila.sqalli@komeet.ma", 24, "female", "+212 6 24 68 02 46", 30, "Route d'Imouzzer", "Fès", "Fès-Meknès", "30050", "Maroc", "photo-1534528741775-53994a69daeb", now))

        // 6. Souss-Massa
        list.add(p("prof-ma-11", "Reda", "Idrissi", "reda.idrissi@komeet.ma", 28, "male", "+212 6 35 79 13 57", 5, "Boulevard Hassan II", "Agadir", "Souss-Massa", "80000", "Maroc", "photo-1539571696357-5a69c17a67c6", now))
        list.add(p("prof-ma-12", "Imane", "Tazi", "imane.tazi@komeet.ma", 25, "female", "+212 6 77 88 99 00", 42, "Boulevard du 20 Août", "Agadir", "Souss-Massa", "80020", "Maroc", "photo-1529626455594-4ff0802cfb7e", now))

        // 7. L'Oriental
        list.add(p("prof-ma-13", "Bilal", "Ziani", "bilal.ziani@komeet.ma", 26, "male", "+212 6 46 80 24 68", 11, "Boulevard Mohammed V", "Oujda", "L'Oriental", "60000", "Maroc", "photo-1492562080023-ab3db95bfbce", now))
        list.add(p("prof-ma-14", "Soukaina", "Kadiri", "soukaina.kadiri@komeet.ma", 23, "female", "+212 6 57 91 35 79", 9, "Avenue Maghreb Arabe", "Oujda", "L'Oriental", "60050", "Maroc", "photo-1488426862026-3ee34a7d66df", now))

        // 8. Béni Mellal-Khénifra
        list.add(p("prof-ma-15", "Younes", "Tahiri", "younes.tahiri@komeet.ma", 30, "male", "+212 6 68 02 46 80", 25, "Avenue Bir Anzarane", "Béni Mellal", "Béni Mellal-Khénifra", "23000", "Maroc", "photo-1501196354995-cbb51c65aaea", now))
        list.add(p("prof-ma-16", "Chaimae", "El Fassi", "chaimae.elfassi@komeet.ma", 25, "female", "+212 6 79 13 57 91", 14, "Boulevard Mohammed V", "Béni Mellal", "Béni Mellal-Khénifra", "23050", "Maroc", "photo-1517841905240-472988babdf9", now))

        // 9. Drâa-Tafilalet
        list.add(p("prof-ma-17", "Ayoub", "Amrani", "ayoub.amrani@komeet.ma", 27, "male", "+212 6 80 24 68 02", 4, "Avenue Mohammed V", "Ouarzazate", "Drâa-Tafilalet", "45000", "Maroc", "photo-1507003211169-0a1dd7228f2d", now))
        list.add(p("prof-ma-18", "Hajar", "Bennani", "hajar.bennani@komeet.ma", 24, "female", "+212 6 91 35 79 13", 16, "Boulevard Al Mouahidine", "Errachidia", "Drâa-Tafilalet", "52000", "Maroc", "photo-1544005313-94ddf0286df2", now))

        // 10. Guelmim-Oued Noun
        list.add(p("prof-ma-19", "Tariq", "Sahraoui", "tariq.sahraoui@komeet.ma", 29, "male", "+212 6 02 46 80 24", 8, "Avenue Hassan II", "Guelmim", "Guelmim-Oued Noun", "81000", "Maroc", "photo-1506794778202-cad84cf45f1d", now))
        list.add(p("prof-ma-20", "Asmae", "Oualid", "asmae.oualid@komeet.ma", 23, "female", "+212 6 13 57 92 46", 12, "Avenue des FAR", "Tan-Tan", "Guelmim-Oued Noun", "82000", "Maroc", "photo-1494790108377-be9c29b29330", now))

        // 11. Laâyoune-Sakia El Hamra
        list.add(p("prof-ma-21", "Nabil", "Boussaid", "nabil.boussaid@komeet.ma", 28, "male", "+212 6 24 68 03 57", 10, "Boulevard de la Mecque", "Laâyoune", "Laâyoune-Sakia El Hamra", "70000", "Maroc", "photo-1500648767791-00dcc994a43e", now))
        list.add(p("prof-ma-22", "Sara", "El Jaouhari", "sara.eljaouhari@komeet.ma", 24, "female", "+212 6 35 79 14 68", 22, "Avenue Smara", "Laâyoune", "Laâyoune-Sakia El Hamra", "70020", "Maroc", "photo-1524504388940-b1c1722653e1", now))

        // 12. Dakhla-Oued Ed-Dahab
        list.add(p("prof-ma-23", "Amine", "Laghrissi", "amine.laghrissi@komeet.ma", 27, "male", "+212 6 46 80 25 79", 6, "Boulevard Al Oula", "Dakhla", "Dakhla-Oued Ed-Dahab", "73000", "Maroc", "photo-1522075469751-3a6694fb2f61", now))
        list.add(p("prof-ma-24", "Kaoutar", "Berrada", "kaoutar.berrada@komeet.ma", 25, "female", "+212 6 57 91 36 80", 14, "Avenue Bahya", "Dakhla", "Dakhla-Oued Ed-Dahab", "73020", "Maroc", "photo-1534528741775-53994a69daeb", now))


        // ==========================================
        // 2. FRANCE - 8 Régions (1 Homme + 1 Femme par région)
        // ==========================================
        // 1. Île-de-France
        list.add(p("prof-fr-1", "Lucas", "Bernard", "lucas.bernard@komeet.fr", 27, "male", "+33 6 12 34 56 78", 12, "Rue de Rivoli", "Paris", "Île-de-France", "75001", "France", "photo-1507003211169-0a1dd7228f2d", now))
        list.add(p("prof-fr-2", "Camille", "Dubois", "camille.dubois@komeet.fr", 25, "female", "+33 6 23 45 67 89", 45, "Boulevard Saint-Germain", "Paris", "Île-de-France", "75006", "France", "photo-1494790108377-be9c29b29330", now))

        // 2. Provence-Alpes-Côte d'Azur
        list.add(p("prof-fr-3", "Jean", "Dujardin", "jean.dujardin@komeet.fr", 30, "male", "+33 6 34 56 78 90", 8, "Promenade des Anglais", "Nice", "Provence-Alpes-Côte d'Azur", "06000", "France", "photo-1500648767791-00dcc994a43e", now))
        list.add(p("prof-fr-4", "Sophie", "Lambert", "sophie.lambert@komeet.fr", 26, "female", "+33 6 45 67 89 01", 14, "La Canebière", "Marseille", "Provence-Alpes-Côte d'Azur", "13001", "France", "photo-1517841905240-472988babdf9", now))

        // 3. Auvergne-Rhône-Alpes
        list.add(p("prof-fr-5", "Romain", "Barbier", "romain.barbier@komeet.fr", 28, "male", "+33 6 56 78 90 12", 20, "Place Bellecour", "Lyon", "Auvergne-Rhône-Alpes", "69002", "France", "photo-1506794778202-cad84cf45f1d", now))
        list.add(p("prof-fr-6", "Giulia", "Dupont", "giulia.dupont@komeet.fr", 24, "female", "+33 6 67 89 01 23", 5, "Rue de la République", "Lyon", "Auvergne-Rhône-Alpes", "69001", "France", "photo-1524504388940-b1c1722653e1", now))

        // 4. Nouvelle-Aquitaine
        list.add(p("prof-fr-7", "Gauthier", "Dupont", "gauthier.dupont@komeet.fr", 29, "male", "+33 6 78 90 12 34", 17, "Place de la Bourse", "Bordeaux", "Nouvelle-Aquitaine", "33000", "France", "photo-1522075469751-3a6694fb2f61", now))
        list.add(p("prof-fr-8", "Marine", "Durand", "marine.durand@komeet.fr", 25, "female", "+33 6 89 01 23 45", 3, "Rue Sainte-Catherine", "Bordeaux", "Nouvelle-Aquitaine", "33000", "France", "photo-1544005313-94ddf0286df2", now))

        // 5. Occitanie
        list.add(p("prof-fr-9", "Hugo", "Gonzalez", "hugo.gonzalez@komeet.fr", 27, "male", "+33 6 90 12 34 56", 28, "Place du Capitole", "Toulouse", "Occitanie", "31000", "France", "photo-1519085360753-af0119f7cbe7", now))
        list.add(p("prof-fr-10", "Agathe", "Deschamps", "agathe.deschamps@komeet.fr", 23, "female", "+33 6 01 23 45 67", 9, "Place de la Comédie", "Montpellier", "Occitanie", "34000", "France", "photo-1529626455594-4ff0802cfb7e", now))

        // 6. Hauts-de-France
        list.add(p("prof-fr-11", "Maxence", "Guillot", "maxence.guillot@komeet.fr", 28, "male", "+33 6 12 34 56 79", 33, "Grand Place", "Lille", "Hauts-de-France", "59000", "France", "photo-1539571696357-5a69c17a67c6", now))
        list.add(p("prof-fr-12", "Justine", "Mercier", "justine.mercier@komeet.fr", 24, "female", "+33 6 23 45 67 80", 11, "Rue Nationale", "Lille", "Hauts-de-France", "59800", "France", "photo-1488426862026-3ee34a7d66df", now))

        // 7. Grand Est
        list.add(p("prof-fr-13", "Alban", "Roger", "alban.roger@komeet.fr", 31, "male", "+33 6 34 56 78 91", 4, "Place Kléber", "Strasbourg", "Grand Est", "67000", "France", "photo-1492562080023-ab3db95bfbce", now))
        list.add(p("prof-fr-14", "Mélissa", "Schmitt", "melissa.schmitt@komeet.fr", 26, "female", "+33 6 45 67 89 02", 21, "Rue des Hallebardes", "Strasbourg", "Grand Est", "67000", "France", "photo-1534528741775-53994a69daeb", now))

        // 8. Bretagne
        list.add(p("prof-fr-15", "Yann", "Leroux", "yann.leroux@komeet.fr", 29, "male", "+33 6 56 78 90 13", 16, "Place Sainte-Anne", "Rennes", "Bretagne", "35000", "France", "photo-1501196354995-cbb51c65aaea", now))
        list.add(p("prof-fr-16", "Enora", "Lefebvre", "enora.lefebvre@komeet.fr", 24, "female", "+33 6 67 89 01 24", 7, "Rue de Siam", "Brest", "Bretagne", "29200", "France", "photo-1517841905240-472988babdf9", now))


        // ==========================================
        // 3. USA - 6 Régions (1 Homme + 1 Femme par région)
        // ==========================================
        // 1. California
        list.add(p("prof-us-1", "Michael", "Miller", "michael.miller@komeet.us", 29, "male", "+1 310 555 0192", 120, "Sunset Boulevard", "Los Angeles", "California", "90028", "USA", "photo-1500648767791-00dcc994a43e", now))
        list.add(p("prof-us-2", "Jessica", "Williams", "jessica.williams@komeet.us", 26, "female", "+1 415 555 0148", 55, "Market Street", "San Francisco", "California", "94103", "USA", "photo-1494790108377-be9c29b29330", now))

        // 2. New York
        list.add(p("prof-us-3", "David", "Smith", "david.smith@komeet.us", 31, "male", "+1 212 555 0183", 350, "5th Avenue", "New York", "New York", "10118", "USA", "photo-1507003211169-0a1dd7228f2d", now))
        list.add(p("prof-us-4", "Emily", "Johnson", "emily.johnson@komeet.us", 27, "female", "+1 718 555 0199", 82, "Bedford Avenue", "New York", "New York", "11211", "USA", "photo-1524504388940-b1c1722653e1", now))

        // 3. Texas
        list.add(p("prof-us-5", "James", "Wilson", "james.wilson@komeet.us", 30, "male", "+1 512 555 0134", 400, "Congress Avenue", "Austin", "Texas", "78701", "USA", "photo-1506794778202-cad84cf45f1d", now))
        list.add(p("prof-us-6", "Ashley", "Taylor", "ashley.taylor@komeet.us", 25, "female", "+1 713 555 0178", 1200, "Main Street", "Houston", "Texas", "77002", "USA", "photo-1517841905240-472988babdf9", now))

        // 4. Florida
        list.add(p("prof-us-7", "Matthew", "Brown", "matthew.brown@komeet.us", 28, "male", "+1 305 555 0162", 100, "Ocean Drive", "Miami", "Florida", "33139", "USA", "photo-1522075469751-3a6694fb2f61", now))
        list.add(p("prof-us-8", "Olivia", "Davis", "olivia.davis@komeet.us", 24, "female", "+1 407 555 0115", 45, "Orange Avenue", "Orlando", "Florida", "32801", "USA", "photo-1544005313-94ddf0286df2", now))

        // 5. Washington
        list.add(p("prof-us-9", "Daniel", "Anderson", "daniel.anderson@komeet.us", 32, "male", "+1 206 555 0143", 200, "Pike Street", "Seattle", "Washington", "98101", "USA", "photo-1519085360753-af0119f7cbe7", now))
        list.add(p("prof-us-10", "Sophia", "Thomas", "sophia.thomas@komeet.us", 26, "female", "+1 509 555 0187", 80, "Riverside Avenue", "Spokane", "Washington", "99201", "USA", "photo-1529626455594-4ff0802cfb7e", now))

        // 6. Illinois
        list.add(p("prof-us-11", "Christopher", "Jackson", "chris.jackson@komeet.us", 29, "male", "+1 312 555 0156", 233, "Michigan Avenue", "Chicago", "Illinois", "60601", "USA", "photo-1539571696357-5a69c17a67c6", now))
        list.add(p("prof-us-12", "Amanda", "White", "amanda.white@komeet.us", 25, "female", "+1 630 555 0122", 15, "Washington Street", "Naperville", "Illinois", "60540", "USA", "photo-1488426862026-3ee34a7d66df", now))


        // ==========================================
        // 4. JAPON - 5 Régions (1 Homme + 1 Femme par région)
        // ==========================================
        // 1. Kanto
        list.add(p("prof-jp-1", "Kenji", "Sato", "kenji.sato@komeet.jp", 28, "male", "+81 3 1234 5678", 1, "Shibuya 1-chome", "Tokyo", "Kanto", "150-0002", "Japon", "photo-1506794778202-cad84cf45f1d", now))
        list.add(p("prof-jp-2", "Sakura", "Watanabe", "sakura.watanabe@komeet.jp", 24, "female", "+81 3 9876 5432", 4, "Ginza 6-chome", "Tokyo", "Kanto", "104-0061", "Japon", "photo-1534528741775-53994a69daeb", now))

        // 2. Kansai
        list.add(p("prof-jp-3", "Ren", "Tanaka", "ren.tanaka@komeet.jp", 27, "male", "+81 6 2345 6789", 12, "Dotonbori", "Osaka", "Kansai", "542-0071", "Japon", "photo-1507003211169-0a1dd7228f2d", now))
        list.add(p("prof-jp-4", "Yui", "Takahashi", "yui.takahashi@komeet.jp", 23, "female", "+81 75 3456 7890", 8, "Gion-machi", "Kyoto", "Kansai", "605-0073", "Japon", "photo-1517841905240-472988babdf9", now))

        // 3. Hokkaido
        list.add(p("prof-jp-5", "Daiki", "Kobayashi", "daiki.kobayashi@komeet.jp", 30, "male", "+81 11 4567 8901", 3, "Odori Nishi", "Sapporo", "Hokkaido", "060-0042", "Japon", "photo-1500648767791-00dcc994a43e", now))
        list.add(p("prof-jp-6", "Aoi", "Nakamura", "aoi.nakamura@komeet.jp", 25, "female", "+81 138 5678 9012", 20, "Motomachi", "Hakodate", "Hokkaido", "040-0054", "Japon", "photo-1494790108377-be9c29b29330", now))

        // 4. Chubu
        list.add(p("prof-jp-7", "Haruto", "Ito", "haruto.ito@komeet.jp", 29, "male", "+81 52 6789 0123", 15, "Sakae 3-chome", "Nagoya", "Chubu", "460-0008", "Japon", "photo-1522075469751-3a6694fb2f61", now))
        list.add(p("prof-jp-8", "Hina", "Yamamoto", "hina.yamamoto@komeet.jp", 24, "female", "+81 54 7890 1234", 9, "Aoi-ku", "Shizuoka", "Chubu", "420-0853", "Japon", "photo-1524504388940-b1c1722653e1", now))

        // 5. Kyushu
        list.add(p("prof-jp-9", "Sota", "Kato", "sota.kato@komeet.jp", 26, "male", "+81 92 8901 2345", 2, "Tenjin 2-chome", "Fukuoka", "Kyushu", "810-0001", "Japon", "photo-1519085360753-af0119f7cbe7", now))
        list.add(p("prof-jp-10", "Mei", "Yoshida", "mei.yoshida@komeet.jp", 22, "female", "+81 96 9012 3456", 18, "Toricho", "Kumamoto", "Kyushu", "860-0845", "Japon", "photo-1544005313-94ddf0286df2", now))


        // ==========================================
        // 5. ARABIE SAOUDITE - 5 Régions (1 Homme + 1 Femme par région)
        // ==========================================
        // 1. Riyadh Region
        list.add(p("prof-sa-1", "Fahad", "Al-Otaibi", "fahad.otaibi@komeet.sa", 30, "male", "+966 50 123 4567", 10, "King Fahd Road, Olaya", "Riyadh", "Riyadh Region", "12211", "Arabie Saoudite", "photo-1507003211169-0a1dd7228f2d", now))
        list.add(p("prof-sa-2", "Sara", "Al-Harbi", "sara.harbi@komeet.sa", 24, "female", "+966 56 345 6789", 14, "Tahlia Street", "Riyadh", "Riyadh Region", "12331", "Arabie Saoudite", "photo-1524504388940-b1c1722653e1", now))

        // 2. Makkah Region
        list.add(p("prof-sa-3", "Tariq", "Al-Zahrani", "tariq.zahrani@komeet.sa", 28, "male", "+966 54 234 5678", 19, "Palestine Street", "Jeddah", "Makkah Region", "23212", "Arabie Saoudite", "photo-1506794778202-cad84cf45f1d", now))
        list.add(p("prof-sa-4", "Noura", "Al-Ghamdi", "noura.ghamdi@komeet.sa", 25, "female", "+966 55 987 6543", 25, "Corniche Road", "Jeddah", "Makkah Region", "23412", "Arabie Saoudite", "photo-1534528741775-53994a69daeb", now))

        // 3. Eastern Province
        list.add(p("prof-sa-5", "Sultan", "Al-Dossary", "sultan.dossary@komeet.sa", 31, "male", "+966 53 456 7890", 8, "Prince Turki Street", "Khobar", "Eastern Province", "34413", "Arabie Saoudite", "photo-1500648767791-00dcc994a43e", now))
        list.add(p("prof-sa-6", "Reem", "Al-Qahtani", "reem.qahtani@komeet.sa", 26, "female", "+966 57 567 8901", 33, "King Abdullah Road", "Dammam", "Eastern Province", "32414", "Arabie Saoudite", "photo-1517841905240-472988babdf9", now))

        // 4. Medina Region
        list.add(p("prof-sa-7", "Faisal", "Al-Mutairi", "faisal.mutairi@komeet.sa", 29, "male", "+966 58 678 9012", 5, "King Abdulaziz Road", "Medina", "Medina Region", "42313", "Arabie Saoudite", "photo-1522075469751-3a6694fb2f61", now))
        list.add(p("prof-sa-8", "Maha", "Al-Shehri", "maha.shehri@komeet.sa", 23, "female", "+966 59 789 0123", 16, "Yanbu Industrial City", "Yanbu", "Medina Region", "46452", "Arabie Saoudite", "photo-1494790108377-be9c29b29330", now))

        // 5. Asir Region
        list.add(p("prof-sa-9", "Bandar", "Al-Asiri", "bandar.asiri@komeet.sa", 32, "male", "+966 50 890 1234", 12, "Al-Soudah Road", "Abha", "Asir Region", "62521", "Arabie Saoudite", "photo-1519085360753-af0119f7cbe7", now))
        list.add(p("prof-sa-10", "Layla", "Al-Ghamdi", "layla.ghamdi@komeet.sa", 25, "female", "+966 55 901 2345", 28, "King Khalid Road", "Khamis Mushait", "Asir Region", "62411", "Arabie Saoudite", "photo-1544005313-94ddf0286df2", now))

        return list
    }
}
