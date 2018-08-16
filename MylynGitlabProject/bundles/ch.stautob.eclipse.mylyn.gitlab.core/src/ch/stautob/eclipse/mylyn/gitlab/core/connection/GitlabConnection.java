package ch.stautob.eclipse.mylyn.gitlab.core.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabMilestone;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabProjectMember;

import ch.stautob.eclipse.mylyn.gitlab.core.attributes.GitlabAttributeMapper;


public class GitlabConnection {

   public final String                host;
   public final String                token;
   public final GitlabProject         project;
   public final GitlabAttributeMapper mapper;

   private List<GitlabMilestone>     milestones;
   private List<GitlabProjectMember> members;

   public GitlabConnection(String host, GitlabProject project, String token, GitlabAttributeMapper mapper) {
      this.host = host;
      this.project = project;
      this.token = token;
      this.mapper = mapper;
   }

   public GitlabAPI api() {
      return GitlabAPI.connect(host, token);
   }

   public void update() throws IOException {
      ArrayList<GitlabProjectMember> memberList = new ArrayList<GitlabProjectMember>();

      milestones = api().getMilestones(project);
      memberList.addAll(api().getProjectMembers(project));
      try {
         memberList.addAll(api().getNamespaceMembers(project.getNamespace()));
      } catch (Exception e) {} catch (Error e) {}
      members = Collections.unmodifiableList(memberList);
   }

   public List<GitlabMilestone> getMilestones() {
      return Collections.unmodifiableList(milestones);
   }

   public List<GitlabProjectMember> getProjectMembers() {
      return Collections.unmodifiableList(members);
   }

}
