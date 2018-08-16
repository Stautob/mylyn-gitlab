package ch.stautob.eclipse.mylyn.gitlab.ui;

import org.eclipse.jface.resource.ImageDescriptor;


public class GitlabImages {

   public static final ImageDescriptor OVERLAY_BUG     = create("overlay-bug.gif");     //$NON-NLS-1$
   public static final ImageDescriptor OVERLAY_FEATURE = create("overlay-feature.gif"); //$NON-NLS-1$
   public static final ImageDescriptor OVERLAY_STORY   = create("overlay-story.gif");   //$NON-NLS-1$
   public static final ImageDescriptor IMAGE_LABEL     = create("label.png");           //$NON-NLS-1$

   private static ImageDescriptor create(String name) {
      return Activator.imageDescriptorFromPlugin("ch.stautob.eclipse.mylyn.gitlab.ui", name);
   }

}
