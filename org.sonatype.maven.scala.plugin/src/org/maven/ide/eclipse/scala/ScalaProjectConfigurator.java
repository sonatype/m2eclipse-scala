/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scala;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.maven.ide.eclipse.jdt.IClasspathDescriptor;
import org.maven.ide.eclipse.jdt.IClasspathEntryDescriptor;
import org.maven.ide.eclipse.jdt.IJavaProjectConfigurator;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;

//TODO check the jre/java version compliance (>= 1.5)
//TODO check JDT Weaving is enabled (if not enabled, icon of scala file is [J] same as java (and property of  the file display "Type :... Java Source File" )
public class ScalaProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

  public static String ID_NATURE = "ch.epfl.lamp.sdt.core.scalanature";
  public static String MOJO_GROUP_ID = "org.scala-tools";
  public static String MOJO_ARTIFACT_ID = "maven-scala-plugin";

  //public static String SCALA_PLUGIN_ID_BUILDER = "ch.epfl.lamp.sdt.core.scalabuilder";

  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
    //MavenProject mavenProject = request.getMavenProject();
    IProject project = request.getProject();
    if(!project.hasNature(ID_NATURE) && isScalaProject(request.getMavenProjectFacade(), monitor)) {
      addNature(project, ID_NATURE, monitor);
    }
  }

  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
      throws CoreException {
    if(isScalaProject(facade.getProject())) {
      removeScalaFromMavenContainer(classpath);
      sortContainerScalaJre(facade, monitor);
    }
  }

  private void removeScalaFromMavenContainer(IClasspathDescriptor classpath) {
    classpath.removeEntry(new IClasspathDescriptor.EntryFilter() {
      public boolean accept(IClasspathEntryDescriptor descriptor) {
        boolean back = "org.scala-lang".equals(descriptor.getGroupId());
        //TODO, use content of Scala Library Container instead of hardcoded value
        back = back && (
            "scala-library".equals(descriptor.getArtifactId())
            || "scala-compiler".equals(descriptor.getArtifactId())
            || "scala-dbc".equals(descriptor.getArtifactId())
            || "scala-swing".equals(descriptor.getArtifactId())
        );
        return back;
      }
    });
  }

  /**
   * Check and reorder Containers : "Scala Lib" should be before "JRE Sys",
   * else 'Run Scala Application' set Boot Entries JRE before Scala and failed with scala.* NotFound Exception.
   */
  private void sortContainerScalaJre(IMavenProjectFacade facade, IProgressMonitor monitor) throws CoreException {
    IProject project = facade.getProject();
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();

    int posScalaContainer = -1;
    int posJreContainer = -1;
    for (int i = 0; i < rawClasspath.length; i++) {
      IClasspathEntry e = rawClasspath[i];
      if (IClasspathEntry.CPE_CONTAINER == e.getEntryKind()) {
        if ("org.eclipse.jdt.launching.JRE_CONTAINER".equals(e.getPath().segment(0))) {
          posJreContainer = i;
        }
        if ("ch.epfl.lamp.sdt.launching.SCALA_CONTAINER".equals(e.getPath().segment(0))) {
          posScalaContainer = i;
        }
      }
    }
    if (posScalaContainer != -1 && posJreContainer != -1 && posScalaContainer > posJreContainer) {
      // swap position to have scalaContainer first
      IClasspathEntry tmp = rawClasspath[posScalaContainer];
      rawClasspath[posScalaContainer] = rawClasspath[posJreContainer];
      rawClasspath[posJreContainer]= tmp;
      javaProject.setRawClasspath(rawClasspath, monitor);
    }
  }

  public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
      IProgressMonitor monitor) throws CoreException {
    // TODO Auto-generated method configureRawClasspath

  }

  static boolean isScalaProject(IProject project) {
    try {
      return project != null && project.isAccessible() && project.hasNature(ID_NATURE);
    } catch(CoreException e) {
      return false;
    }
  }

  private boolean isScalaProject(IMavenProjectFacade facade, IProgressMonitor monitor)
    throws CoreException {
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

  protected boolean isMavenBundlePluginMojo(MojoExecution execution) {
    return isMavenBundlePluginMojo(execution.getGroupId(), execution.getArtifactId());
  }

  private boolean isMavenBundlePluginMojo(Plugin plugin) {
    return isMavenBundlePluginMojo(plugin.getGroupId(), plugin.getArtifactId());
  }

  private boolean isMavenBundlePluginMojo(String groupId, String artifactId) {
    return MOJO_GROUP_ID.equals(groupId) && MOJO_ARTIFACT_ID.equals(artifactId);
  }

}
