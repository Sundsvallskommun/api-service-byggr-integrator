package se.sundsvall.byggrintegrator.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Entity
@Table(name = "file_access_token", indexes = {
	@Index(name = "idx_file_access_token_expires_at", columnList = "expires_at"),
	@Index(name = "idx_file_access_token_file_id_municipality_id", columnList = "file_id, municipality_id")
})
public class FileAccessTokenEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "file_id", nullable = false)
	private String fileId;

	@Column(name = "municipality_id", nullable = false, length = 4)
	private String municipalityId;

	@Column(name = "expires_at", nullable = false)
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime expiresAt;

	@Column(name = "created", nullable = false)
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	public static FileAccessTokenEntity create() {
		return new FileAccessTokenEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public FileAccessTokenEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(final String fileId) {
		this.fileId = fileId;
	}

	public FileAccessTokenEntity withFileId(final String fileId) {
		this.fileId = fileId;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public FileAccessTokenEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(final OffsetDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public FileAccessTokenEntity withExpiresAt(final OffsetDateTime expiresAt) {
		this.expiresAt = expiresAt;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	public FileAccessTokenEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	@PrePersist
	void prePersist() {
		created = OffsetDateTime.now();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final var that = (FileAccessTokenEntity) o;
		return Objects.equals(id, that.id)
			&& Objects.equals(fileId, that.fileId)
			&& Objects.equals(municipalityId, that.municipalityId)
			&& Objects.equals(expiresAt, that.expiresAt)
			&& Objects.equals(created, that.created);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fileId, municipalityId, expiresAt, created);
	}

	@Override
	public String toString() {
		return "FileAccessTokenEntity{" +
			"id='" + id + '\'' +
			", fileId='" + fileId + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", expiresAt=" + expiresAt +
			", created=" + created +
			'}';
	}
}
