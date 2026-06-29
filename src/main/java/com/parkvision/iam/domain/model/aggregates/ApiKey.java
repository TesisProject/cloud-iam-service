package com.parkvision.iam.domain.model.aggregates;

import com.parkvision.iam.domain.commands.CreateApiKeyCommand;
import com.parkvision.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

/**
 * Credencial de máquina para los nodos Fog. La key entregada al cliente tiene el formato
 * {@code <keyId>.<secret>}: {@code keyId} es público e indexado (lookup rápido) y {@code secret} se
 * almacena únicamente hasheado con BCrypt en {@code secretHash}. El secreto en claro nunca se persiste
 * ni se vuelve a exponer tras la creación.
 */
@Entity
@Table(name = "api_key")
@Getter
public class ApiKey extends AuditableAbstractAggregateRoot<ApiKey> {

    @Column(name = "key_id", nullable = false, unique = true, length = 40)
    private String keyId;

    @Column(name = "secret_hash", nullable = false)
    private String secretHash;

    @Column(nullable = false, length = 100)
    private String name;

    /** Reservado para vincular la key a un {@code Node} de parking cuando exista. Nullable por ahora. */
    @Column(name = "node_id")
    private Long nodeId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    protected ApiKey() {}

    public ApiKey(CreateApiKeyCommand command, String keyId, String secretHash) {
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("API key name must not be blank");
        }
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("API key id must not be blank");
        }
        if (secretHash == null || secretHash.isBlank()) {
            throw new IllegalArgumentException("API key secret hash must not be blank");
        }
        this.keyId = keyId;
        this.secretHash = secretHash;
        this.name = command.name();
        this.nodeId = command.nodeId();
        this.expiresAt = command.expiresAt();
        this.active = true;
    }

    /** Una key es usable si está activa y no ha expirado (expiración nula = nunca expira). */
    public boolean isUsable() {
        return active && (expiresAt == null || Instant.now().isBefore(expiresAt));
    }

    public void recordUsage() {
        this.lastUsedAt = Instant.now();
    }

    public void revoke() {
        this.active = false;
    }
}
