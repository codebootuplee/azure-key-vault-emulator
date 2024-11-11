package com.AEJ.azure.vault

package com.example.mockkeyvault.controller

import com.example.mockkeyvault.entity.Secret
import com.example.mockkeyvault.model.SecretResponse
import com.example.mockkeyvault.model.SecretAttributes
import com.example.mockkeyvault.service.SecretService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/secrets")
class SecretController @Autowired constructor(private val secretService: SecretService) {

    @GetMapping
    fun listSecrets(request: HttpServletRequest): ResponseEntity<List<Map<String, Any>>> {
        val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}${request.contextPath}/api/v1/secrets"
        val secrets = secretService.getAllSecrets()
        val response = secrets.map { secret ->
            mapOf(
                "id" to "$baseUrl/${secret.keyName}",
                "attributes" to SecretAttributes(
                    enabled = secret.enabled,
                    created = secret.created,
                    updated = secret.updated,
                    recoveryLevel = secret.recoveryLevel,
                    expires = secret.expires
                )
            )
        }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{keyName}")
    fun getSecret(@PathVariable keyName: String, request: HttpServletRequest): ResponseEntity<SecretResponse> {
        val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}${request.contextPath}/api/v1/secrets"
        val secretOpt = secretService.getSecret(keyName)
        return if (secretOpt.isPresent) {
            val secret = secretOpt.get()
            val response = SecretResponse(
                value = secret.value,
                id = "$baseUrl/${secret.keyName}/${secret.id}",
                contentType = secret.contentType,
                attributes = SecretAttributes(
                    enabled = secret.enabled,
                    created = secret.created,
                    updated = secret.updated,
                    recoveryLevel = secret.recoveryLevel,
                    expires = secret.expires
                ),
                tags = mapOf("exampleTag" to "exampleValue") // Placeholder for tags
            )
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createSecret(@RequestParam keyName: String, @RequestParam value: String, @RequestParam(required = false) contentType: String?): ResponseEntity<Secret> {
        val createdSecret = secretService.createSecret(keyName, value, contentType)
        return ResponseEntity.ok(createdSecret)
    }
}



package com.example.mockkeyvault.controller

import com.example.mockkeyvault.entity.Secret
import com.example.mockkeyvault.model.SecretResponse
import com.example.mockkeyvault.model.SecretAttributes
import com.example.mockkeyvault.service.SecretService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/secrets")
class SecretController @Autowired constructor(private val secretService: SecretService) {

    private val vaultBaseUrl = "https://your-keyvault-name.vault.azure.net/secrets"

    @GetMapping
    fun listSecrets(): ResponseEntity<List<Map<String, Any>>> {
        val secrets = secretService.getAllSecrets()
        val response = secrets.map { secret ->
            mapOf(
                "id" to "$vaultBaseUrl/${secret.keyName}",
                "attributes" to SecretAttributes(
                    enabled = secret.enabled,
                    created = secret.created,
                    updated = secret.updated,
                    recoveryLevel = secret.recoveryLevel,
                    expires = secret.expires
                )
            )
        }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{keyName}")
    fun getSecret(@PathVariable keyName: String): ResponseEntity<SecretResponse> {
        val secretOpt = secretService.getSecret(keyName)
        return if (secretOpt.isPresent) {
            val secret = secretOpt.get()
            val response = SecretResponse(
                value = secret.value,
                id = "$vaultBaseUrl/${secret.keyName}/${secret.id}",
                contentType = secret.contentType,
                attributes = SecretAttributes(
                    enabled = secret.enabled,
                    created = secret.created,
                    updated = secret.updated,
                    recoveryLevel = secret.recoveryLevel,
                    expires = secret.expires
                ),
                tags = mapOf("exampleTag" to "exampleValue") // Placeholder for tags
            )
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createSecret(@RequestParam keyName: String, @RequestParam value: String, @RequestParam(required = false) contentType: String?): ResponseEntity<Secret> {
        val createdSecret = secretService.createSecret(keyName, value, contentType)
        return ResponseEntity.ok(createdSecret)
    }
}

package com.example.mockkeyvault.service

import com.example.mockkeyvault.entity.Secret
import com.example.mockkeyvault.repository.SecretRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Optional

@Service
class SecretService @Autowired constructor(private val secretRepository: SecretRepository) {

    fun getAllSecrets(): List<Secret> {
        return secretRepository.findAll()
    }

    fun getSecret(keyName: String): Optional<Secret> {
        return secretRepository.findByKeyName(keyName)
    }

    fun createSecret(keyName: String, value: String, contentType: String? = null): Secret {
        val currentTime = Instant.now().epochSecond
        val secret = Secret(
            keyName = keyName,
            value = value,
            contentType = contentType,
            created = currentTime,
            updated = currentTime,
            recoveryLevel = "Purgeable"
        )
        return secretRepository.save(secret)
    }
}

package com.example.mockkeyvault.model

data class SecretResponse(
    val value: String,
    val id: String,
    val contentType: String? = null,
    val attributes: SecretAttributes,
    val tags: Map<String, String>? = null
)

data class SecretAttributes(
    val enabled: Boolean,
    val created: Long?,
    val updated: Long?,
    val recoveryLevel: String?,
    val expires: Long? = null
)


package com.example.mockkeyvault.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Secret(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var keyName: String,
    var value: String,
    var contentType: String? = null,
    var enabled: Boolean = true,
    var created: Long? = null,
    var updated: Long? = null,
    var recoveryLevel: String? = null,
    var expires: Long? = null
)
