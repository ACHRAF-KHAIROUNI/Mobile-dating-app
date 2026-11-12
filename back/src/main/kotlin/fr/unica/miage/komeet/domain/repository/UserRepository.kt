package fr.unica.miage.komeet.domain.repository

import fr.unica.miage.komeet.domain.model.User

interface UserRepository {
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun save(user: User): User
    fun update(user: User): User
    fun delete(id: String): Boolean
    fun count(): Long
}
