package LAB1;

import java.time.LocalDateTime;


public class ProcessedRequest {

    private Long id;
    private String referenceId;
    private LocalDateTime processedAt;

    public ProcessedRequest(Long id, String referenceId, LocalDateTime processedAt) {
        this.id = id;
        this.referenceId = referenceId;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        return "ProcessedRequest{id=" + id + ", referenceId='" + referenceId + "', processedAt=" + processedAt + "}";
    }
}
