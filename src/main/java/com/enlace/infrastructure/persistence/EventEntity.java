package com.enlace.infrastructure.persistence;
 
import com.enlace.domain.model.CoupleStory;
import com.enlace.domain.model.Event;
import com.enlace.domain.model.EventStatus;
import com.enlace.domain.model.Plan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
 
import java.time.Instant;
import java.util.UUID;
 
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class EventEntity {
 
    @Id
    private UUID id;
 
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
 
    @Column(nullable = false, unique = true)
    private String slug;
 
    @Column(nullable = false)
    private String title;
 
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;
 
    @Column(name = "ivs_channel_arn")
    private String ivsChannelArn;
 
    @Column(name = "ivs_channel_ingest_endpoint")
    private String ivsChannelIngestEndpoint;
 
    @Column(name = "ivs_playback_url")
    private String ivsPlaybackUrl;
 
    @Column(name = "recording_s3_prefix")
    private String recordingS3Prefix;

    @Column(name = "recording_available", nullable = false)
    private boolean recordingAvailable;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
 
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
 
    @SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "deleted_at")
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "partner1_name", length = 100)
    private String partner1Name;

    @Column(name = "partner2_name", length = 100)
    private String partner2Name;

    @Column(name = "story_message", length = 400)
    private String storyMessage;

    @Column(name = "photo_key_1", length = 500)
    private String photoKey1;

    @Column(name = "photo_key_2", length = 500)
    private String photoKey2;

    @Column(name = "photo_key_3", length = 500)
    private String photoKey3;

    public static EventEntity fromDomain(Event event) {
        EventEntity entity = new EventEntity();
        entity.id = event.getId();
        entity.customerId = event.getCustomerId();
        entity.slug = event.getSlug();
        entity.title = event.getTitle();
        entity.scheduledAt = event.getScheduledAt();
        entity.plan = event.getPlan();
        entity.status = event.getStatus();
        entity.ivsChannelArn = event.getIvsChannelArn();
        entity.ivsChannelIngestEndpoint = event.getIvsChannelIngestEndpoint();
        entity.ivsPlaybackUrl = event.getIvsPlaybackUrl();
        entity.recordingS3Prefix = event.getRecordingS3Prefix();
        entity.recordingAvailable = event.isRecordingAvailable();
        entity.createdAt = event.getCreatedAt();
        entity.updatedAt = event.getUpdatedAt();
        entity.deletedAt = event.getDeletedAt();
        entity.partner1Name = event.getCoupleStory() != null ? event.getCoupleStory().getPartner1Name() : null;
        entity.partner2Name = event.getCoupleStory() != null ? event.getCoupleStory().getPartner2Name() : null;
        entity.storyMessage = event.getCoupleStory() != null ? event.getCoupleStory().getMessage() : null;
        entity.photoKey1    = event.getCoupleStory() != null ? event.getCoupleStory().getPhotoKey1() : null;
        entity.photoKey2    = event.getCoupleStory() != null ? event.getCoupleStory().getPhotoKey2() : null;
        entity.photoKey3    = event.getCoupleStory() != null ? event.getCoupleStory().getPhotoKey3() : null;
        return entity;
    }
 
    public Event toDomain() {
        Event event = new Event();
        event.setId(id);
        event.setCustomerId(customerId);
        event.setSlug(slug);
        event.setTitle(title);
        event.setScheduledAt(scheduledAt);
        event.setPlan(plan);
        event.setStatus(status);
        event.setIvsChannelArn(ivsChannelArn);
        event.setIvsChannelIngestEndpoint(ivsChannelIngestEndpoint);
        event.setIvsPlaybackUrl(ivsPlaybackUrl);
        event.setRecordingS3Prefix(recordingS3Prefix);
        event.setRecordingAvailable(recordingAvailable);
        event.setCreatedAt(createdAt);
        event.setUpdatedAt(updatedAt);
        event.setDeletedAt(deletedAt);
        boolean hasStory = partner1Name != null || partner2Name != null || storyMessage != null
                        || photoKey1 != null || photoKey2 != null || photoKey3 != null;
        if (hasStory) {
            CoupleStory story = new CoupleStory(partner1Name, partner2Name, storyMessage);
            story.setPhotoKey1(photoKey1);
            story.setPhotoKey2(photoKey2);
            story.setPhotoKey3(photoKey3);
            event.setCoupleStory(story);
        }
        return event;
    }
}
