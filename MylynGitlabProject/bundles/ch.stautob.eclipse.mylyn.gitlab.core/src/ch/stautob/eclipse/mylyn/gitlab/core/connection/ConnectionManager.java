package ch.stautob.eclipse.mylyn.gitlab.core.connection;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSession;

import ch.stautob.eclipse.mylyn.gitlab.core.attributes.GitlabAttributeMapper;
import ch.stautob.eclipse.mylyn.gitlab.core.exceptions.GitlabException;
import ch.stautob.eclipse.mylyn.gitlab.core.exceptions.GitlabExceptionHandler;
import ch.stautob.eclipse.mylyn.gitlab.core.exceptions.UnknownProjectException;
import ch.stautob.eclipse.mylyn.gitlab.core.utils.TaskRepositoryUtil;


public class ConnectionManager {

   private static MutableMap<String, GitlabConnection> connections = Maps.mutable.empty();

   private static Pattern URLPattern = Pattern.compile("((?:http|https)://(?:[^\\/]*))/((?:.*?)/(?:[^\\/]*?))$");

   /**
    * Returns the GitlabConnection for the given task repository
    *
    * @param repository
    * @return
    * @throws GitlabException
    */
   static public GitlabConnection get(TaskRepository repository) throws GitlabException {
      return get(repository, false);
   }

   /**
    * Returns the GitlabConnection for the given task repository. If it
    * failes for whatever reason, it returns null.
    *
    * @param repository
    * @return
    */
   static public GitlabConnection getSafe(TaskRepository repository) {
      try {
         return get(repository);
      } catch (GitlabException e) {
         return null;
      }
   }

   /**
    * Constructs a URL string for the given task repository.
    *
    * @param repository
    * @return
    */
   private static String constructURL(TaskRepository repository) {
      String username = repository.getCredentials(AuthenticationType.REPOSITORY).getUserName();
      String password = repository.getCredentials(AuthenticationType.REPOSITORY).getPassword();
      return repository.getUrl() + "?username=" + username + "&password=" + password.hashCode();
   }

   /**
    * Validates the given task repository and returns a GitlabConnection if
    * the task repository is a valid repository.
    *
    * @param repository
    * @return
    * @throws GitlabException
    */
   static GitlabConnection validate(TaskRepository repository) throws GitlabException {
      try {
         String projectPath = null;
         String host = null;

         if (TaskRepositoryUtil.getGitlabBaseUrl(repository).trim().length() > 0) {
            host = TaskRepositoryUtil.getGitlabBaseUrl(repository).trim();
            if (!repository.getUrl().startsWith(host)) { throw new GitlabException("Invalid project URL!"); }

            projectPath = repository.getUrl().replaceFirst(Matcher.quoteReplacement(host), "");
            if (projectPath.startsWith("/")) {
               projectPath = projectPath.substring(1);
            }
         } else {
            Matcher matcher = URLPattern.matcher(repository.getUrl());
            if (!matcher.find()) { throw new GitlabException("Invalid Project-URL!"); }

            projectPath = matcher.group(2);
            host = matcher.group(1);
         }

         String username = repository.getCredentials(AuthenticationType.REPOSITORY).getUserName();
         String password = repository.getCredentials(AuthenticationType.REPOSITORY).getPassword();

         GitlabSession session = null;
         String token = null;

         if (TaskRepositoryUtil.getUsesPrivateToken(repository)) {
            session = GitlabAPI.connect(host, password).getCurrentSession();
            token = password;
         } else {
            session = GitlabAPI.connect(host, username, password);
            token = session.getPrivateToken();
         }

         GitlabAPI api = GitlabAPI.connect(host, token);

         if (projectPath.endsWith(".git")) {
            projectPath = projectPath.substring(0, projectPath.length() - 4);
         }

         List<GitlabProject> projects = api.getProjects();
         for (GitlabProject p : projects) {
            if (p.getPathWithNamespace().equals(projectPath)) {
               GitlabConnection connection = new GitlabConnection(host, p, token, new GitlabAttributeMapper(repository));
               return connection;
            }
         }
         throw new UnknownProjectException(projectPath);
      } catch (GitlabException e) {
         throw e;
      } catch (Exception e) {
         throw GitlabExceptionHandler.handle(e);
      } catch (Error e) {
         e.printStackTrace();
         throw GitlabExceptionHandler.handle(e);
      }
   }

   /**
    * Returns a *valid* GitlabConnection, otherwise this method throws an exception.
    *
    * @param repository
    * @param forceUpdate
    *        if true, a new GitlabConnection instance will be created, even if a Gitlab
    *        Connection already exists for the given task repository
    * @return
    * @throws GitlabException
    */
   static GitlabConnection get(TaskRepository repository, boolean forceUpdate) throws GitlabException {
      try {
         String hash = constructURL(repository);
         if (connections.containsKey(hash) && !forceUpdate) {
            return connections.get(hash);
         } else {
            GitlabConnection connection = validate(repository);

            connections.put(hash, connection);
            connection.update();

            return connection;
         }
      } catch (GitlabException e) {
         throw e;
      } catch (Exception e) {
         throw GitlabExceptionHandler.handle(e);
      } catch (Error e) {
         throw GitlabExceptionHandler.handle(e);
      }
   }

}
