package fr.unica.miage.komeet.infrastructure.repository

import fr.unica.miage.komeet.domain.model.*
import fr.unica.miage.komeet.domain.repository.*
import fr.unica.miage.komeet.infrastructure.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

private fun ResultRow.profile() = Profile(
    id = this[Profiles.id],
    firstName = this[Profiles.firstName],
    lastName = this[Profiles.lastName],
    email = this[Profiles.email],
    age = this[Profiles.age],
    gender = this[Profiles.gender],
    phone = this[Profiles.phone],
    location = Location(
        streetNumber = this[Profiles.streetNumber],
        streetName = this[Profiles.streetName],
        city = this[Profiles.city],
        department = this[Profiles.department],
        postcode = this[Profiles.postcode],
        country = this[Profiles.country],
        region = this[Profiles.region] ?: this[Profiles.department]
    ),
    thumbnailUrl = this[Profiles.thumbnailUrl],
    pictureUrl = this[Profiles.pictureUrl],
    source = this[Profiles.origin],
    sourceId = this[Profiles.sourceId],
    createdAt = Instant.parse(this[Profiles.createdAt])
)

class ExposedUserRepository(private val db: Database) : UserRepository {
    override fun findById(id: String) = transaction(db) {
        Users.selectAll().where { Users.id eq id }.singleOrNull()?.let {
            User(
                id = it[Users.id],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash],
                displayName = it[Users.displayName],
                createdAt = Instant.parse(it[Users.createdAt]),
                updatedAt = Instant.parse(it[Users.updatedAt]),
                region = it[Users.region]
            )
        }
    }

    override fun findByEmail(email: String) = transaction(db) {
        Users.selectAll().where { Users.email eq email.trim().lowercase() }.singleOrNull()?.let {
            User(
                id = it[Users.id],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash],
                displayName = it[Users.displayName],
                createdAt = Instant.parse(it[Users.createdAt]),
                updatedAt = Instant.parse(it[Users.updatedAt]),
                region = it[Users.region]
            )
        }
    }

    override fun save(user: User) = transaction(db) {
        Users.insert {
            it[id] = user.id
            it[email] = user.email.trim().lowercase()
            it[passwordHash] = user.passwordHash
            it[displayName] = user.displayName
            it[region] = user.region
            it[createdAt] = user.createdAt.toString()
            it[updatedAt] = user.updatedAt.toString()
        }
        user
    }

    override fun update(user: User) = transaction(db) {
        Users.update({ Users.id eq user.id }) {
            it[displayName] = user.displayName
            it[region] = user.region
            it[updatedAt] = user.updatedAt.toString()
        }
        user
    }

    override fun delete(id: String) = transaction(db) { Users.deleteWhere { Users.id eq id } > 0 }
    override fun count() = transaction(db) { Users.selectAll().count() }
}

class ExposedProfileRepository(private val db: Database) : ProfileRepository {
    override fun findById(id: String) =
        transaction(db) { Profiles.selectAll().where { Profiles.id eq id }.singleOrNull()?.profile() }

    override fun findAll(params: ProfileSearchParams): PaginatedResult<Profile> = transaction(db) {
        val safePage = params.page.coerceAtLeast(1)
        val size = params.pageSize.coerceIn(1, 100)
        var q: Op<Boolean> = Op.TRUE

        params.query?.trim()?.takeIf { it.isNotEmpty() }?.let {
            val pat = "%${it.lowercase()}%"
            q = q and (
                (Profiles.firstName.lowerCase() like pat) or
                (Profiles.lastName.lowerCase() like pat) or
                (Profiles.city.lowerCase() like pat) or
                (Profiles.department.lowerCase() like pat) or
                (Profiles.region.lowerCase() like pat) or
                (Profiles.country.lowerCase() like pat)
            )
        }
        params.minAge?.let { q = q and (Profiles.age greaterEq it) }
        params.maxAge?.let { q = q and (Profiles.age lessEq it) }
        params.gender?.let { q = q and (Profiles.gender.lowerCase() eq it.lowercase()) }
        params.country?.let { q = q and (Profiles.country.lowerCase() eq it.lowercase()) }
        params.department?.let { q = q and (Profiles.department.lowerCase() eq it.lowercase()) }
        params.region?.trim()?.takeIf { it.isNotEmpty() }?.let {
            val r = it.lowercase()
            q = q and ((Profiles.region.lowerCase() eq r) or (Profiles.department.lowerCase() eq r))
        }

        val total = Profiles.selectAll().where { q }.count()
        val sort = when (params.sort?.lowercase()) {
            "age" -> Profiles.age
            "country" -> Profiles.country
            else -> Profiles.lastName
        }
        val ordered = if (params.order.equals("desc", true)) sort to SortOrder.DESC else sort to SortOrder.ASC
        val rows = Profiles.selectAll().where { q }
            .orderBy(ordered)
            .limit(size)
            .offset(((safePage - 1) * size).toLong())
            .map { it.profile() }

        PaginatedResult(rows, total, safePage, size)
    }

    override fun saveAll(profiles: List<Profile>) = transaction(db) {
        profiles.forEach { p ->
            Profiles.upsert(Profiles.id) {
                it[id] = p.id
                it[firstName] = p.firstName
                it[lastName] = p.lastName
                it[email] = p.email
                it[age] = p.age
                it[gender] = p.gender
                it[phone] = p.phone
                it[streetNumber] = p.location.streetNumber
                it[streetName] = p.location.streetName
                it[city] = p.location.city
                it[department] = p.location.department
                it[region] = p.location.region ?: p.location.department
                it[postcode] = p.location.postcode
                it[country] = p.location.country
                it[thumbnailUrl] = p.thumbnailUrl
                it[pictureUrl] = p.pictureUrl
                it[Profiles.origin] = p.source
                it[sourceId] = p.sourceId
                it[createdAt] = p.createdAt.toString()
            }
        }
        profiles.size
    }

    override fun count() = transaction(db) { Profiles.selectAll().count() }
    override fun deleteAll() {
        transaction(db) { Profiles.deleteAll() }
    }
}

class ExposedPreferencesRepository(private val db: Database) : PreferencesRepository {
    override fun findByUserId(userId: String) = transaction(db) {
        Preferences.selectAll().where { Preferences.userId eq userId }.singleOrNull()?.let {
            UserPreferences(
                userId = it[Preferences.userId],
                minAge = it[Preferences.minAge],
                maxAge = it[Preferences.maxAge],
                country = it[Preferences.country],
                region = it[Preferences.region],
                updatedAt = Instant.parse(it[Preferences.updatedAt])
            )
        }
    }

    override fun save(preferences: UserPreferences) = transaction(db) {
        Preferences.upsert(Preferences.userId) {
            it[userId] = preferences.userId
            it[minAge] = preferences.minAge
            it[maxAge] = preferences.maxAge
            it[country] = preferences.country
            it[region] = preferences.region
            it[updatedAt] = preferences.updatedAt.toString()
        }
        preferences
    }

    override fun deleteByUserId(userId: String) =
        transaction(db) { Preferences.deleteWhere { Preferences.userId eq userId } > 0 }
}

class ExposedImportMetadataRepository(private val db: Database) : ImportMetadataRepository {
    override fun findById(id: String) = transaction(db) {
        ImportMetadata.selectAll().where { ImportMetadata.id eq id }.singleOrNull()?.let {
            fr.unica.miage.komeet.domain.model.ImportMetadata(
                it[ImportMetadata.id],
                Instant.parse(it[ImportMetadata.lastImportAt]),
                it[ImportMetadata.profilesCount],
                it[fr.unica.miage.komeet.infrastructure.database.ImportMetadata.origin]
            )
        }
    }

    override fun save(metadata: fr.unica.miage.komeet.domain.model.ImportMetadata) = transaction(db) {
        ImportMetadata.upsert(ImportMetadata.id) {
            it[id] = metadata.id
            it[lastImportAt] = metadata.lastImportAt.toString()
            it[profilesCount] = metadata.profilesCount
            it[origin] = metadata.source
        }
        metadata
    }
}

class ExposedRegionRepository(private val db: Database) : RegionRepository {
    override fun findAll(country: String?): List<Region> = transaction(db) {
        val query = if (!country.isNullOrBlank()) {
            Regions.selectAll().where { Regions.country.lowerCase() eq country.trim().lowercase() }
        } else {
            Regions.selectAll()
        }
        query.orderBy(Regions.country to SortOrder.ASC, Regions.name to SortOrder.ASC).map {
            Region(
                id = it[Regions.id],
                name = it[Regions.name],
                country = it[Regions.country],
                code = it[Regions.code]
            )
        }
    }

    override fun findById(id: String): Region? = transaction(db) {
        Regions.selectAll().where { Regions.id eq id }.singleOrNull()?.let {
            Region(
                id = it[Regions.id],
                name = it[Regions.name],
                country = it[Regions.country],
                code = it[Regions.code]
            )
        }
    }

    override fun saveAll(regions: List<Region>): Int = transaction(db) {
        regions.forEach { r ->
            Regions.upsert(Regions.id) {
                it[id] = r.id
                it[name] = r.name
                it[country] = r.country
                it[code] = r.code
            }
        }
        regions.size
    }

    override fun count(): Long = transaction(db) { Regions.selectAll().count() }
}

class ExposedFavoriteRepository(private val db: Database, private val profiles: ProfileRepository) :
    FavoriteRepository {
    override fun exists(userId: String, profileId: String) = transaction(db) {
        Favorites.selectAll().where { (Favorites.userId eq userId) and (Favorites.profileId eq profileId) }.count() > 0
    }

    override fun add(userId: String, profileId: String) = transaction(db) {
        if (exists(userId, profileId)) false else {
            Favorites.insert {
                it[Favorites.userId] = userId
                it[Favorites.profileId] = profileId
                it[createdAt] = Instant.now().toString()
            }
            true
        }
    }

    override fun remove(userId: String, profileId: String) =
        transaction(db) { Favorites.deleteWhere { (Favorites.userId eq userId) and (Favorites.profileId eq profileId) } > 0 }

    override fun removeAllByUserId(userId: String) =
        transaction(db) { Favorites.deleteWhere { Favorites.userId eq userId } }

    override fun findByUserId(userId: String, page: Int, pageSize: Int): PaginatedResult<Favorite> {
        val p = page.coerceAtLeast(1)
        val s = pageSize.coerceIn(1, 100)
        val pairs = transaction(db) {
            Favorites.selectAll().where { Favorites.userId eq userId }.orderBy(Favorites.createdAt to SortOrder.DESC)
                .limit(s).offset(((p - 1) * s).toLong())
                .map { it[Favorites.profileId] to Instant.parse(it[Favorites.createdAt]) }
        }
        val total = transaction(db) { Favorites.selectAll().where { Favorites.userId eq userId }.count() }
        val items = pairs.mapNotNull { (id, date) ->
            profiles.findById(id)?.let { Favorite(userId, it, date) }
        }
        return PaginatedResult(items, total, p, s)
    }
}
