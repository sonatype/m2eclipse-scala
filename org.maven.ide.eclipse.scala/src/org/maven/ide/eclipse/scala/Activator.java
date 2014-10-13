
package org.maven.ide.eclipse.scala;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class Activator extends Plugin {

  /////////////////////////////////////////////////////////////////////////////
  // STATIC
  /////////////////////////////////////////////////////////////////////////////

  private static ScalaPluginIds[] SCALA_PLUGIN_IDS = {
    new ScalaPluginIds("org.scala-ide.sdt"),
    new ScalaPluginIds("ch.epfl.lamp.sdt")
  };

  private static Activator INSTANCE;

  public static Activator getInstance() {
    return INSTANCE;
  } 
  
  /////////////////////////////////////////////////////////////////////////////
  // INSTANCE
  /////////////////////////////////////////////////////////////////////////////
  private ScalaPluginIds _scalaPluginIds;
  
  public Activator() {
    INSTANCE = this;
  }
  
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    Bundle[] bundles = context.getBundles();
    for(int i = 0; _scalaPluginIds == null && i < SCALA_PLUGIN_IDS.length; i++ ) {
      ScalaPluginIds ids = SCALA_PLUGIN_IDS[i];
      for(int j = 0; _scalaPluginIds == null && j < bundles.length; j++ ) {
        Bundle bundle = bundles[j];
        if(ids.pluginId.equals(bundle.getSymbolicName())) {
          _scalaPluginIds = ids;
          _scalaPluginIds.version = bundle.getVersion();
        }
      }
    }
  }

  protected ScalaPluginIds scalaPluginIds() {
    return _scalaPluginIds;
  }
}


class ScalaPluginIds {
  protected String pluginId;

  protected String natureId;

  protected String containerLibId;
  
  protected Version version;

  public ScalaPluginIds(String pluginId, String natureId, String containerLibId) {
    super();
    this.pluginId = pluginId;
    this.natureId = natureId;
    this.containerLibId = containerLibId;
  }

  public ScalaPluginIds(String prefix) {
    this(prefix + ".core", prefix + ".core.scalanature", prefix + ".launching.SCALA_CONTAINER");
  }
}
