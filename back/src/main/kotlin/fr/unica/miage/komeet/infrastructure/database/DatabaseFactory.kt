package fr.unica.miage.komeet.infrastructure.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Users : Table("UserEntity") {
    val id = varchar("id", 64);
    val email = varchar("email", 320).uniqueIndex();
    val passwordHash = varchar("passwordHash", 100);
    val displayName = varchar("displayName", 120);
    val createdAt = varchar("createdAt", 40);
    val updatedAt = varchar("updatedAt", 40);
    override val primaryKey = PrimaryKey(id)
}

object Profiles : Table("ProfileEntity") {
    val id = varchar("id", 128);
    val firstName = varchar("firstName", 120);
    val lastName = varchar("lastName", 120);
    val email = varchar("email", 320).nullable();
    val age = integer("age");
    val gender = varchar("gender", 16).nullable();
    val phone = varchar("phone", 64).nullable();
    val streetNumber = integer("streetNumber").nullable();
    val streetName = varchar("streetName", 180).nullable();
    val city = varchar("city", 120);
    val department = varchar("department", 120).nullable();
    val postcode = varchar("postcode", 32).nullable();
    val country = varchar("country", 120);
    val thumbnailUrl = text("thumbnailUrl");
    val pictureUrl = text("pictureUrl");
    val origin = varchar("source", 120);
    val sourceId = varchar("sourceId", 128).nullable();
    val createdAt = varchar("createdAt", 40);
    override val primaryKey = PrimaryKey(id)
}

object Favorites : Table("FavoriteEntity") {
    val userId = varchar("userId", 64).references(Users.id);
    val profileId = varchar("profileId", 128).references(Profiles.id);
    val createdAt = varchar("createdAt", 40);
    override val primaryKey = PrimaryKey(userId, profileId)
}

object Preferences : Table("UserPreferencesEntity") {
    val userId = varchar("userId", 64).references(Users.id);
    val minAge = integer("minAge").nullable();
    val maxAge = integer("maxAge").nullable();
    val country = varchar("country", 120).nullable();
    val updatedAt = varchar("updatedAt", 40);
    override val primaryKey = PrimaryKey(userId)
}

object ImportMetadata : Table("ImportMetadataEntity") {
    val id = varchar("id", 64);
    val lastImportAt = varchar("lastImportAt", 40);
    val profilesCount = integer("profilesCount");
    val origin = varchar("source", 120);
    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun connect(path: String): Database {
        if (path != ":memory:") File(path).absoluteFile.parentFile?.mkdirs();
        val actualPath = if (path == ":memory:") File.createTempFile("komeet-exposed-", ".db").absolutePath else path;
        val url = "jdbc:sqlite:$actualPath";
        val db = Database.connect(url, driver = "org.sqlite.JDBC"); transaction(db) {
            SchemaUtils.create(
                Users,
                Profiles,
                Favorites,
                Preferences,
                ImportMetadata
            )
        }; return db
    }
}
