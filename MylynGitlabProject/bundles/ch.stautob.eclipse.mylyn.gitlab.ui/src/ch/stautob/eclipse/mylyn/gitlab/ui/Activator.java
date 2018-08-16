package ch.stautob.eclipse.mylyn.gitlab.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class Activator extends AbstractUIPlugin {

   private static Activator plugin;

   public Activator() {
      plugin = this;
   }

   @Override
   public void start(BundleContext context) throws Exception {
      super.start(context);
   }

   @Override
   public void stop(BundleContext context) throws Exception {
      plugin = null;
      super.stop(context);
   }

   public static Activator getDefault() {
      return plugin;
   }

}
