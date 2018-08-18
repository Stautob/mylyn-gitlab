package org.gitlab.api.models;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitlabMergeRequestApprovals {
    public static final String URL = "/approvals";

    private Integer id;
    private Integer iid;
    @JsonProperty("project_id")
    private Integer projectId;
    private String title;
    private String description;
    private String state;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("merge_status")
    private String mergeStatus;
    
    @JsonProperty("approvals_required")
    private Integer approvalsRequired;

    @JsonProperty("approvals_left")
    private Integer approvalsLeft;

    @JsonProperty("approved_by")
    private List<GitlabApprovedBy> approvedBy;

    @JsonProperty("suggested_approvers")
    private List<GitlabUser> suggestedApprovers;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIid() {
        return iid;
    }

    public void setIid(Integer iid) {
        this.iid = iid;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        description = d;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getMergeStatus() {
        return mergeStatus;
    }

    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    public Integer getApprovalsRequired() {
        return approvalsRequired;
    }

    public void setApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
    }
    
    public Integer getApprovalsLeft() {
        return approvalsLeft;
    }

    public void setApprovalsLeft(Integer approvalsLeft) {
        this.approvalsLeft = approvalsLeft;
    }

    public List<GitlabApprovedBy> getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(List<GitlabApprovedBy> approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public List<GitlabUser> getSuggestedApprovers() {
        return suggestedApprovers;
    }

    public void setSuggestedApprovers(List<GitlabUser> suggestedApprovers) {
        this.suggestedApprovers = suggestedApprovers;
    }
}
