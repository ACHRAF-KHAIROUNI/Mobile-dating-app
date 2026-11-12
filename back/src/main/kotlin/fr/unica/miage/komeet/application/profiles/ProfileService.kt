package fr.unica.miage.komeet.application.profiles

import fr.unica.miage.komeet.api.dto.PageResponse
import fr.unica.miage.komeet.api.dto.PaginationMeta
import fr.unica.miage.komeet.api.dto.ProfileDto
import fr.unica.miage.komeet.api.dto.toDto
import fr.unica.miage.komeet.domain.error.ProfileNotFoundException
import fr.unica.miage.komeet.domain.error.ValidationException
import fr.unica.miage.komeet.domain.model.ImportMetadata
import fr.unica.miage.komeet.domain.repository.ImportMetadataRepository
import fr.unica.miage.komeet.domain.repository.ProfileRepository
import fr.unica.miage.komeet.domain.repository.ProfileSearchParams
import fr.unica.miage.komeet.infrastructure.randomuser.RandomUserDataSource
import org.slf4j.LoggerFactory
import java.time.Instant

data class ImportResult(
    val success: Boolean,
    val importedCount: Int,
    val totalCount: Long,
    val message: String
)

class ProfileService(
    private val profileRepository: ProfileRepository,
    private val randomUserDataSource: RandomUserDataSource,
    private val importMetadataRepository: ImportMetadataRepository
) {
    private val logger = LoggerFactory.getLogger(ProfileService::class.java)

    fun listProfiles(params: ProfileSearchParams): PageResponse<ProfileDto> {
        val validationErrors = mutableListOf<String>()

        if (params.page < 1) {
            validationErrors.add("Le numéro de page 'page' doit être supérieur ou égal à 1")
        }
        if (params.pageSize < 1 || params.pageSize > 100) {
            validationErrors.add("La taille de page 'pageSize' doit être comprise entre 1 et 100")
        }
        if (params.minAge != null && params.minAge < 0) {
            validationErrors.add("Le paramètre 'minAge' ne peut pas être négatif")
        }
        if (params.maxAge != null && params.maxAge < 0) {
            validationErrors.add("Le paramètre 'maxAge' ne peut pas être négatif")
        }
        if (params.minAge != null && params.maxAge != null && params.minAge > params.maxAge) {
            validationErrors.add("Le paramètre 'minAge' ne peut pas être supérieur à 'maxAge'")
        }
        val allowedSorts = setOf("name", "age", "country")
        if (params.sort != null && params.sort.lowercase() !in allowedSorts) {
            validationErrors.add("Le paramètre de tri 'sort' doit être l'un des suivants : ${allowedSorts.joinToString()}")
        }
        val allowedOrders = setOf("asc", "desc")
        if (params.order != null && params.order.lowercase() !in allowedOrders) {
            validationErrors.add("Le paramètre d'ordre 'order' doit être 'asc' ou 'desc'")
        }

        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Paramètres de recherche invalides", validationErrors)
        }

        val result = profileRepository.findAll(params)

        return PageResponse(
            items = result.items.map { it.toDto() },
            pagination = PaginationMeta(
                page = result.page,
                pageSize = result.pageSize,
                totalItems = result.totalItems,
                totalPages = result.totalPages
            )
        )
    }

    fun getProfileById(id: String): ProfileDto {
        val profile = profileRepository.findById(id)
            ?: throw ProfileNotFoundException(id)
        return profile.toDto()
    }

    suspend fun importProfiles(
        count: Int,
        seed: String? = null,
        nationality: String? = "fr",
        force: Boolean = false
    ): ImportResult {
        val currentCount = profileRepository.count()
        if (currentCount > 0 && !force) {
            logger.info("Database already contains $currentCount profiles. Skipping initial import.")
            return ImportResult(
                success = true,
                importedCount = 0,
                totalCount = currentCount,
                message = "Profils déjà présents dans la base locale ($currentCount profils)."
            )
        }

        return try {
            val profiles = randomUserDataSource.fetchProfiles(count, seed, nationality)
            val savedCount = profileRepository.saveAll(profiles)

            val total = profileRepository.count()
            importMetadataRepository.save(
                ImportMetadata(
                    id = "default",
                    lastImportAt = Instant.now(),
                    profilesCount = total.toInt(),
                    source = "randomuser.me"
                )
            )

            logger.info("Successfully imported $savedCount profiles into database.")
            ImportResult(
                success = true,
                importedCount = savedCount,
                totalCount = total,
                message = "Importation réussie de $savedCount profils."
            )
        } catch (e: Exception) {
            logger.error("Error importing profiles from Random User Generator: ${e.message}")
            val fallbackCount = profileRepository.count()
            if (fallbackCount > 0) {
                logger.warn("External import failed, continuing with existing $fallbackCount local profiles.")
                ImportResult(
                    success = false,
                    importedCount = 0,
                    totalCount = fallbackCount,
                    message = "Échec de l'import externe, données locales conservées ($fallbackCount profils)."
                )
            } else {
                ImportResult(
                    success = false,
                    importedCount = 0,
                    totalCount = 0,
                    message = "Échec de l'import externe et aucun profil local disponible : ${e.message}"
                )
            }
        }
    }

    fun count(): Long = profileRepository.count()
}
