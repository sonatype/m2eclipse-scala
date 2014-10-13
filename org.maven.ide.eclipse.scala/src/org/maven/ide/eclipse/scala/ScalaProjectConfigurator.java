/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scala;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;

//TODO check the jre/java version compliance (>= 1.5)
//TODO check JDT Weaving is enabled (if not enabled, icon of scala file is [J] same as java (and property of  the file display "Type :... Java Source File" )
//TODO check that pom.xml and ScalaLib Container declare the same scala version
//TODO keep sync scala-compiler configuration between pom.xml and scala-plugin ? (sync bi-direction) ?
//TODO ask sonatype/mailing-list about how to retreive maven plugin configuration, like additional sourceDirectory
/**
 * @author Sonatype Inc (http://github.com/sonatype/m2eclipse-scala)
 * @author davidB (http://github.com/davidB)
 * @author germanklf  (http://github.com/germanklf)
 * @author Fred Bricon
 */
public class ScalaProjectConfigurator extends AbstractJavaProjectConfigurator {


  private static final String SCALA_CONTAINER_PATH = "org.scala-ide.sdt.launching.SCALA_CONTAINER";

  private static final String MAVEN_CONTAINER_PATH = "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER";

  private static String DEPLOYABLE_KEY = "org.eclipse.jst.component.dependency";

  private static String NON_DEPLOYABLE_KEY = "org.eclipse.jst.component.nondependency";

  private Map<String, Integer> mapSourceTypeWeight;

  private Map<String, Integer> mapResourceWeight;

  private Comparator<IClasspathEntry> comparator;

  public ScalaProjectConfigurator() {
    super();

    mapSourceTypeWeight = new HashMap<String, Integer>();
    mapSourceTypeWeight.put("src/main/", 9000);
    mapSourceTypeWeight.put("src/test/", 1000);

    mapResourceWeight = new HashMap<String, Integer>();
    mapResourceWeight.put("java", 100);
    mapResourceWeight.put("resources", -10);

    comparator = new Comparator<IClasspathEntry>() {
      public int compare(IClasspathEntry o1, IClasspathEntry o2) {
        Integer w1 = getWeight(o1);
        Integer w2 = getWeight(o2);
        if(w1 > 0 || w2 > 0) {
          if(w1.equals(w2))
            return o1.getPath().toString().compareTo(o2.getPath().toString());
          else
            return w1.compareTo(w2) * -1;
        }
        return 0;
      }

      private Integer getWeight(IClasspathEntry ce) {
        int value = 0;
        if(ce.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          for(Entry<String, Integer> e : mapSourceTypeWeight.entrySet()) {
            if(ce.getPath().toString().contains(e.getKey())) {
              value += e.getValue();
            }
          }
          for(Entry<String, Integer> e : mapResourceWeight.entrySet()) {
            if(ce.getPath().toString().endsWith(e.getKey())) {
              value += e.getValue();
            }
          }
        }
        return value;
      }
    };
  }

  private String scalaNatureId() {
    ScalaPluginIds ids = Activator.getInstance().scalaPluginIds();
    return (ids == null)?null : ids.natureId;
  }
  
//  private boolean shouldManageClasspath() {
//    Version v4 = new Version(4,0,0);
//    return v4.compareTo(Activator.getInstance().scalaPluginIds().version) > 0;
//  }

  @Override
  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
    super.configure(request, monitor);
    String scalaNature = scalaNatureId();
    if (scalaNature == null) {
      //TODO show an alert to user that he should to install scala-ide plugin;
      return;
    }
    try {
      if (request != null)  {
        IProject project = request.getProject();
        if(!project.hasNature(scalaNature) && isScalaProject(request.getMavenProjectFacade(), monitor)) {
          if(!project.hasNature("org.eclipse.jdt.core.javanature")) {
            addNature(project, "org.eclipse.jdt.core.javanature", monitor);
          }
          addNature(project, scalaNature, monitor);
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * configure Classpath : contain of "Maven Dependencies" Librairies Container.
   */
  @Override
  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {
    String scalaNature = scalaNatureId();
    if (scalaNature == null) {
      //TODO show an alert to user that he should to install scala-ide plugin;
      return;
    }
    if (!isLaunchConfigurationCtx()) {
      IProject project = facade.getProject();
      if(isScalaProject(project)) {
  //      if(!project.hasNature(ID_NATURE)) {
  //        addNature(project, ID_NATURE, monitor);
  //      }
        removeScalaFromMavenContainer(classpath);
        sortContainerScalaJre(project, monitor); //
        makeScalaLibDeployable(facade, classpath, monitor);
      }
    }
  }

  // workaround to avoid NPE
  @Override
  protected IPath getFullPath( IMavenProjectFacade facade, File file ) {
    if (file == null) return null;
    return super.getFullPath(facade, file);
  }

  private void removeScalaFromMavenContainer(final IClasspathDescriptor classpath) {
    classpath.removeEntry(new IClasspathDescriptor.EntryFilter() {
      public boolean accept(IClasspathEntryDescriptor descriptor) {
        boolean back = "org.scala-lang".equals(descriptor.getGroupId());
        //TODO, use content of Scala Library Container instead of hardcoded value
        back = back && ("scala-library".equals(descriptor.getArtifactId())
        //|| "scala-compiler".equals(descriptor.getArtifactId())
            || "scala-dbc".equals(descriptor.getArtifactId()) || "scala-swing".equals(descriptor.getArtifactId()));
        return back;
      }
    });
  }

  /**
   * Check and reorder Containers : "Scala Lib" should be before "JRE Sys",
   * else 'Run Scala Application' set Boot Entries JRE before Scala and failed with scala.* NotFound Exception.
   * Should already be done when adding nature
   * @see scala.tools.eclipse.Nature#configure()
   */
  private void sortContainerScalaJre(final IProject project, IProgressMonitor monitor) throws CoreException {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();

    Boolean sorted = true;
    for (int i = 0; i < classpathEntries.length - 1; i++) {
      if (comparator.compare(classpathEntries[i], classpathEntries[i+1]) > 0){sorted = false; break;}
    }

    if(!sorted) {
      List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(classpathEntries.length);
      Collections.addAll(entries, classpathEntries);

      Collections.sort(entries, comparator);
      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
    }
  }

  private boolean isScalaProject(IProject project) {
    try {
      return project != null && project.isAccessible() && project.hasNature(scalaNatureId());
    } catch(CoreException e) {
      return false;
    }
  }

  private boolean isScalaProject(IMavenProjectFacade facade, IProgressMonitor monitor) throws CoreException {
    List<Plugin> plugins = facade.getMavenProject(monitor).getBuildPlugins();
    if(plugins != null) {
      for(Plugin plugin : plugins) {
        if(isMavenBundlePluginMojo(plugin) && !plugin.getExecutions().isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isLaunchConfigurationCtx() {
    for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
      if ("launch".equals(e.getMethodName())) {
        return true;
      }
    }
    return false;
  }

  protected boolean isMavenBundlePluginMojo(MojoExecution execution) {
    return isMavenBundlePluginMojo(execution.getGroupId(), execution.getArtifactId());
  }

  private boolean isMavenBundlePluginMojo(Plugin plugin) {
    return isMavenBundlePluginMojo(plugin.getGroupId(), plugin.getArtifactId());
  }

  private boolean isMavenBundlePluginMojo(String groupId, String artifactId) {
    return ("org.scala-tools".equals(groupId) && "maven-scala-plugin".equals(artifactId))
     || ("net.alchim31.maven".equals(groupId) && "scala-maven-plugin".equals(artifactId));
  }

  @SuppressWarnings("restriction")
  private void makeScalaLibDeployable(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {

    IJavaProject javaProject = JavaCore.create(facade.getProject());
    if (javaProject == null) {
      return;
    }

    IClasspathEntry scalaLibrary = getContainer(javaProject, SCALA_CONTAINER_PATH);

    if (scalaLibrary == null) {
      //Really nothing to do here
      return;
    }

    IClasspathEntry mavenLibrary = getContainer(javaProject, MAVEN_CONTAINER_PATH);

    IClasspathAttribute deployableAttribute = getDeployableAttribute(mavenLibrary);
    //If the Maven Classpath Container is set to be deployed in WTP, then do the same for the Scala one
    if (deployableAttribute != null) {
      //Add the deployable attribute only if it's not set already.
      if (getDeployableAttribute(scalaLibrary) == null) {
        addDeployableAttribute(javaProject, deployableAttribute, monitor);
      }
    }

  }

  private static void addDeployableAttribute(IJavaProject javaProject, IClasspathAttribute deployableAttribute, IProgressMonitor monitor) throws JavaModelException, CoreException {
    if (javaProject == null) return;
    ClasspathContainerInitializer scalaInitializer = JavaCore.getClasspathContainerInitializer(SCALA_CONTAINER_PATH);
    if (scalaInitializer == null) return;
    IPath scalaContainerPath = Path.fromPortableString(SCALA_CONTAINER_PATH);
    Boolean updateAble = scalaInitializer.canUpdateClasspathContainer(scalaContainerPath, javaProject);
    final IClasspathContainer scalaLibrary = JavaCore.getClasspathContainer(scalaContainerPath, javaProject);
    final IClasspathEntry[] cpEntries = scalaLibrary.getClasspathEntries();

    for(int i = 0; i < cpEntries.length; i++ ) {
      IClasspathEntry cpe = cpEntries[i];
      LinkedHashMap<String, IClasspathAttribute> attrs = new LinkedHashMap<String, IClasspathAttribute>();
      for(IClasspathAttribute attr : cpe.getExtraAttributes()) {
        //Keep all existing attributes except the non_deployable key
        if(!attr.getName().equals(NON_DEPLOYABLE_KEY)) {
          attrs.put(attr.getName(), attr);
        }
      }
      attrs.put(deployableAttribute.getName(), deployableAttribute);
      IClasspathAttribute[] newAttrs = attrs.values().toArray(new IClasspathAttribute[attrs.size()]);
      cpEntries[i] = JavaCore.newLibraryEntry(cpe.getPath(), cpe.getSourceAttachmentPath(),
          cpe.getSourceAttachmentRootPath(), cpe.getAccessRules(), newAttrs, cpe.isExported());
    }

    IClasspathContainer candidateScalaContainer = new IClasspathContainer() {
      public IPath getPath() { return scalaLibrary.getPath(); }
      public IClasspathEntry[] getClasspathEntries() { return cpEntries; }
      public String getDescription() { return scalaLibrary.getDescription(); }
      public int getKind() { return scalaLibrary.getKind(); }
    };

    if (updateAble){
      scalaInitializer.requestClasspathContainerUpdate(scalaContainerPath, javaProject, candidateScalaContainer);
    } else {
      IJavaProject [] jPArray = { javaProject };
      IClasspathContainer[] cpArray = { candidateScalaContainer };
      JavaCore.setClasspathContainer(scalaContainerPath, jPArray, cpArray, null);
    }
  }

  private static IClasspathEntry getContainer(IJavaProject javaProject, String containerPath) throws JavaModelException {
    IClasspathEntry[] cp = javaProject.getRawClasspath();
    for(int i = 0; i < cp.length; i++ ) {
      if(IClasspathEntry.CPE_CONTAINER == cp[i].getEntryKind()
          && containerPath.equals(cp[i].getPath().lastSegment())) {
        return cp[i];
      }
    }
    return null;
  }

  private static IClasspathAttribute getDeployableAttribute(IClasspathEntry library) {
    if (library == null) {
      return null;
    }

    IClasspathAttribute[] attributes = library.getExtraAttributes();
    if (attributes == null || attributes.length == 0) {
      return null;
    }

    for(IClasspathAttribute attr : attributes) {
      if (DEPLOYABLE_KEY.equals(attr.getName())) {
           return new ClasspathAttribute(attr.getName(), attr.getValue());
      }
    }
    return null;
  }
}
