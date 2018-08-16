package ch.stautob.eclipse.mylyn.gitlab.ui;

import java.util.Set;

import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;

public class GitlabEditorPage extends AbstractTaskEditorPage {

	public GitlabEditorPage(TaskEditor editor, String connectorKind) {
		super(editor, connectorKind);
		setNeedsPrivateSection(false);
		setNeedsSubmitButton(true);
		setNeedsAddToCategory(false);
	}
	
	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
	   return Sets.adapt(super.createPartDescriptors()).select(desc -> !desc.getId().equals(ID_PART_PLANNING));
	}

}
