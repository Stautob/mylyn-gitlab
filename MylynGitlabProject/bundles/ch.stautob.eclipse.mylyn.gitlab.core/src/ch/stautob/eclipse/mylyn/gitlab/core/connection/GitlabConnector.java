package ch.stautob.eclipse.mylyn.gitlab.core.connection;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabIssue;

import ch.stautob.eclipse.mylyn.gitlab.core.Activator;
import ch.stautob.eclipse.mylyn.gitlab.core.exceptions.GitlabException;
import ch.stautob.eclipse.mylyn.gitlab.core.issues.GitlabIssueSearch;
import ch.stautob.eclipse.mylyn.gitlab.core.tasks.GitlabTaskDataHandler;
import ch.stautob.eclipse.mylyn.gitlab.core.tasks.GitlabTaskMapper;


public class GitlabConnector extends AbstractRepositoryConnector {

   private GitlabTaskDataHandler handler = new GitlabTaskDataHandler();

   @Override
   public boolean canCreateNewTask(TaskRepository repository) {
      return true;
   }

   @Override
   public boolean canCreateTaskFromKey(TaskRepository repository) {
      return false;
   }

   @Override
   public String getConnectorKind() {
      return Activator.CONNECTOR_KIND;
   }

   @Override
   public String getLabel() {
      return "Gitlab issues";
   }

   @Override
   public String getRepositoryUrlFromTaskUrl(String arg0) {
      return null;
   }

   @Override
   public TaskData getTaskData(TaskRepository repository, String id, IProgressMonitor monitor) throws CoreException {

      try {
         monitor.beginTask("Task Download", IProgressMonitor.UNKNOWN);
         return handler.downloadTaskData(repository, GitlabConnector.getTicketId(id));
      } finally {
         monitor.done();
      }
   }

   @Override
   public String getTaskIdFromTaskUrl(String url) {
      return null;
   }

   @Override
   public String getTaskUrl(String arg0, String arg1) {
      return null;
   }

   @Override
   public boolean hasTaskChanged(TaskRepository repository, ITask task, TaskData data) {
      TaskMapper mapper = new GitlabTaskMapper(data);
      if (data.isPartial()) {
         return mapper.hasChanges(task);
      } else {
         Date repositoryDate = mapper.getModificationDate();
         Date localDate = task.getModificationDate();
         if (repositoryDate != null && repositoryDate.equals(localDate)) { return false; }
         return true;
      }
   }

   @Override
   public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector, ISynchronizationSession session,
         IProgressMonitor monitor) {

      try {
         monitor.beginTask("Tasks querying", IProgressMonitor.UNKNOWN);
         GitlabConnection connection = ConnectionManager.get(repository);
         GitlabAPI api = connection.api();

         GitlabIssueSearch search = new GitlabIssueSearch(query);
         List<GitlabIssue> issues = api.getIssues(connection.project);

         for (GitlabIssue i : issues) {
            if (search.doesMatch(i)) collector.accept(handler.createTaskDataFromGitlabIssue(i, repository, api.getNotes(i)));
         }

         return Status.OK_STATUS;
      } catch (CoreException e) {
         return new Status(IStatus.ERROR, Activator.ID_PLUGIN, "Unable to execute Query: " + e.getMessage());
      } catch (IOException e) {
         return new Status(IStatus.ERROR, Activator.ID_PLUGIN, "Unable to execute Query: " + e.getMessage());
      } finally {
         monitor.done();
      }
   }

   @Override
   public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
      try {
         monitor.beginTask("Updating repository configuration", IProgressMonitor.UNKNOWN);
         ConnectionManager.get(repository, true);
      } finally {
         monitor.done();
      }
   }

   @Override
   public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData data) {
      getTaskMapping(data).applyTo(task);
   }

   @Override
   public GitlabTaskMapper getTaskMapping(TaskData taskData) {
      return new GitlabTaskMapper(taskData);
   }

   public static void validate(TaskRepository taskRepo) throws CoreException {
      try {
         ConnectionManager.validate(taskRepo);
      } catch (GitlabException e) {
         throw e;
      } catch (Exception e) {
         throw new GitlabException("Connection not successful or repository not found: " + e.getMessage());
      } catch (Error e) {
         throw new GitlabException("Connection not successful or repository not found: " + e.getMessage());
      }
   }

   @Override
   public AbstractTaskDataHandler getTaskDataHandler() {
      return handler;
   }

   public static Integer getTicketId(String id) {
      return Integer.parseInt(id);
   }

}
