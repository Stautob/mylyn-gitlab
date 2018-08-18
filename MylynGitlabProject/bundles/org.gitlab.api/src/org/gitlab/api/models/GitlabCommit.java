package org.gitlab.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class GitlabCommit {

    public final static String URL = "/commits";

    private String id;
    private String title;
    private String message;

    @JsonProperty("short_id")
    private String shortId;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_email")
    private String authorEmail;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("committed_date")
    private Date committedDate;

    @JsonProperty("authored_date")
    private Date authoredDate;

    @JsonProperty("parent_ids")
    private List<String> parentIds;

    @JsonProperty("last_pipeline")
    private GitlabPipeline lastPipeline;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getParentIds() {
        return parentIds;
    }

    public void setParentIds(List<String> parentIds) {
        this.parentIds = parentIds;
    }

    public Date getCommittedDate() {
        return committedDate;
    }

    public void setCommittedDate(Date committedDate) {
        this.committedDate = committedDate;
    }

    public Date getAuthoredDate() {
        return authoredDate;
    }

    public void setAuthoredDate(Date authoredDate) {
        this.authoredDate = authoredDate;
    }

    @Override
    public boolean equals(Object obj) {
        // we say that two commit objects are equal iff they have the same ID
        // this prevents us from having to do clever workarounds for
        // https://gitlab.com/gitlab-org/gitlab-ce/issues/759
        try {
            GitlabCommit commitObj = (GitlabCommit) obj;
            return (this.getId().compareTo(commitObj.getId()) == 0);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    public GitlabPipeline getLastPipeline() {
        return lastPipeline;
    }

    public void setLastPipeline(GitlabPipeline lastPipeline) {
        this.lastPipeline = lastPipeline;
    }
}
