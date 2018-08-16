package ch.stautob.eclipse.mylyn.gitlab.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


public class GitlabImages {

   private static final URL baseURL = GitlabUIPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

   public static final ImageDescriptor OVERLAY_BUG = create("overlay-bug.gif"); //$NON-NLS-1$

   public static final ImageDescriptor OVERLAY_FEATURE = create("overlay-feature.gif"); //$NON-NLS-1$

   public static final ImageDescriptor OVERLAY_STORY = create("overlay-story.gif"); //$NON-NLS-1$

   public static final Image IMAGE_LABEL = create("label.png").createImage();

   private static ImageDescriptor create(String name) {
      try {
         return ImageDescriptor.createFromURL(makeIconFileURL(name));
      } catch (MalformedURLException e) {
         return ImageDescriptor.getMissingImageDescriptor();
      }
   }

   private static URL makeIconFileURL(String name) throws MalformedURLException {
      if (baseURL == null) { throw new MalformedURLException(); }

      StringBuilder buffer = new StringBuilder();
      buffer.append(name);
      return new URL(baseURL, buffer.toString());
   }

}
