package fr.unica.miage.komeet.integration

import fr.unica.miage.komeet.domain.model.User
import fr.unica.miage.komeet.infrastructure.database.DatabaseFactory
import fr.unica.miage.komeet.infrastructure.repository.ExposedUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant

class PersistenceAndImportTest {
 @Test fun `data persists in SQLite across connections`() {
  val f=File.createTempFile("komeet_exposed_", ".db")
  try { val first=DatabaseFactory.connect(f.absolutePath); val repo=ExposedUserRepository(first); val now=Instant.now(); repo.save(User("u1","a@example.com","hash","Alice",now,now)); val second=DatabaseFactory.connect(f.absolutePath); val loaded=ExposedUserRepository(second).findById("u1"); assertNotNull(loaded); assertEquals("a@example.com",loaded?.email) } finally { f.delete() }
 }
}
