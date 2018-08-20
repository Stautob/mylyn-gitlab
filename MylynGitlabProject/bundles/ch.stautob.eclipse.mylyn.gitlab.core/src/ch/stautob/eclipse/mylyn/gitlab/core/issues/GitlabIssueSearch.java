package ch.stautob.eclipse.mylyn.gitlab.core.issues;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.gitlab.api.models.GitlabIssue;


public class GitlabIssueSearch {

   private String assignee;
   private String milestone;

   private Boolean opened;
   private Boolean closed;

   private List<Pattern> labels = new ArrayList<>();

   public GitlabIssueSearch(IRepositoryQuery query) {
      assignee = query.getAttribute("assignee");
      milestone = query.getAttribute("milestone");

      opened = Boolean.parseBoolean(query.getAttribute("opened"));
      closed = Boolean.parseBoolean(query.getAttribute("closed"));

      for (String label : query.getAttribute("labels").split(",")) {
         if (label.trim().length() > 0) {
            labels.add(Pattern.compile(label.trim()));
         }
      }
   }

   public boolean doesMatch(GitlabIssue issue) {
      if (!assignee.equals("") && (issue.getAssignee() == null || !(assignee.equals(issue.getAssignee().getUsername()) || assignee.equals(issue
            .getAssignee().getName())))) { return false; }

      if (!milestone.equals("") && (issue.getMilestone() == null || !milestone.equals(issue.getMilestone().getTitle()))) { return false; }

      List<Pattern> matchedLabels = new ArrayList<>();
      for (Pattern p : labels) {
         for (String label : issue.getLabels()) {
            if (p.matcher(label).find()) {
               matchedLabels.add(p);
            }
         }
      }

      if (matchedLabels.size() < labels.size()) { return false; }

      if (!closed && issue.getState().equals(GitlabIssue.STATE_CLOSED)) { return false; }

      if (!opened && issue.getState().equals(GitlabIssue.STATE_OPENED)) { return false; }

      return true;
   }

}
