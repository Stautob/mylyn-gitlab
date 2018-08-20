package ch.stautob.eclipse.mylyn.gitlab.core.utils;

import org.eclipse.mylyn.tasks.core.TaskRepository;


public class TaskRepositoryUtil {

   public static boolean getUsesPrivateToken(TaskRepository repository) {
      return Boolean.parseBoolean(repository.getProperty("ch.stautob.eclipse.mylyn.gitlab.usePrivateToken"));
   }

   public static void setUsePrivateToken(TaskRepository repository, boolean should) {
      repository.setProperty("ch.stautob.eclipse.mylyn.gitlab.usePrivateToken", String.valueOf(should));
   }
   
   public static String getGitlabBaseUrl(TaskRepository repository) {
      return repository.getProperty("ch.stautob.eclipse.mylyn.gitlab.gitlabBaseUrl");
   }

   public static void setGitlabBaseUrl(TaskRepository repository, String url) {
      repository.setProperty("ch.stautob.eclipse.mylyn.gitlab.gitlabBaseUrl", url);
   }

}
