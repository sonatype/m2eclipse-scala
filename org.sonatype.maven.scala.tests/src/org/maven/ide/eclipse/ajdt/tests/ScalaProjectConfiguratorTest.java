/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.ajdt.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.maven.ide.eclipse.project.ResolverConfiguration;
import org.maven.ide.eclipse.scala.ScalaProjectConfigurator;
import org.maven.ide.eclipse.tests.common.AbstractMavenProjectTestCase;

/**
 * @author Igor Fedorenko
 */
public class ScalaProjectConfiguratorTest extends AbstractMavenProjectTestCase {
  
  private String origGoalsOnImport;
  
  protected void setUp() throws Exception {
    super.setUp();

    origGoalsOnImport = mavenConfiguration.getGoalOnImport();
    mavenConfiguration.setGoalOnImport("process-test-classes");
  }

  protected void tearDown() throws Exception {
    mavenConfiguration.setGoalOnImport(origGoalsOnImport);

    super.tearDown();
  }
  
  public void testSimple01_import() throws IOException, CoreException {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/scaven/pom.xml", configuration);
    assertTrue("Expected Scala nature", project.hasNature(ScalaProjectConfigurator.ID_NATURE));

    IJavaProject javaProject = JavaCore.create(project);
    List<IClasspathEntry> sources = getSources(javaProject.getRawClasspath());
    assertEquals(1, sources.size());
    assertEquals(project.getFolder("src/main/scala").getFullPath(), sources.get(0).getPath());
  }

  private List<IClasspathEntry> getSources(IClasspathEntry[] rawCp) {
    ArrayList<IClasspathEntry> sources = new ArrayList<IClasspathEntry>();
    for (IClasspathEntry entry : rawCp) {
      if (IClasspathEntry.CPE_SOURCE == entry.getEntryKind()) {
        sources.add(entry);
      }
    }
    return sources;
  }
  
  public void xtestInterProject() throws Exception {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject[] projects = importProjects("projects/interproject", new String[] {"pom.xml", "aspect/pom.xml", "depa/pom.xml", "depi/pom.xml"}, configuration);
    
    assertTrue("Expected AJDT nature", projects[1].hasNature(ScalaProjectConfigurator.ID_NATURE));
    assertTrue("Expected AJDT nature", projects[2].hasNature(ScalaProjectConfigurator.ID_NATURE));
  }
}
