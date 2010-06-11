package org.maven.ide.eclipse.scala;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.DebugPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

  private ScalaLaunchConfigurationListener _launchConfigurationListener;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    System.out.println(">>>>>>>>>>>>>>> register");
    _launchConfigurationListener = new ScalaLaunchConfigurationListener();
    DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(_launchConfigurationListener);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(_launchConfigurationListener);
    _launchConfigurationListener = null;
  }
}
