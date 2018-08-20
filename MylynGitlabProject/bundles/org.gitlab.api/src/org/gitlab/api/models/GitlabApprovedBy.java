package org.gitlab.api.models;

/**
 * Incomprehensibly, Gitlab packages "approved_by" in a wrapper which contains a user
 * and nothing else.
 */
public class GitlabApprovedBy {

   private GitlabUser user;

   public GitlabUser getUser() {
      return user;
   }

   public void setUser(GitlabUser user) {
      this.user = user;
   }
}
