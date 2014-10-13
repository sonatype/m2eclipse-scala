m2eclipse-scala
========

[m2eclipse-scala] is a work in progress to ease integration between [m2eclipse] and [scala-ide for eclipse].

* issue tracker : [m2eclipse-scala at github](https://github.com/sonatype/m2eclipse-scala/issues)
* mailing-list : [scala-ide-user](http://groups.google.fr/group/scala-ide-user)
* update-site : [http://alchim31.free.fr/m2e-scala/update-site](http://alchim31.free.fr/m2e-scala/update-site)
* update-site for previous-version follow pattern : http://alchim31.free.fr/m2e-scala/update-site-<major>.<minor>.<micro>


### Working configuration

The plugin is "validated" by importing into Eclipse the sample projects :

* [Scala Only (no java)](http://github.com/sonatype/m2eclipse-scala/tree/master/samples/prj-scala-only/)
* [Scala depends of some java code](http://github.com/sonatype/m2eclipse-scala/tree/master/samples/prj-scala-after-java/)
* [Scala and Java cyclic dependency](http://github.com/sonatype/m2eclipse-scala/tree/master/samples/prj-scala-cycle-java/) (configuration longer than mono direction scala -> java or java -> scala to compile under maven)
* [Java main code and Scala for test](http://github.com/sonatype/m2eclipse-scala/tree/master/samples/prj-java-test-in-scala/)
* [Custom Layout](http://github.com/sonatype/m2eclipse-scala/tree/master/samples/prj-custom-layout/) java + scala cyclic dependencies, java+scala under the same source folder `src` and `test` (for test source folder).
* [Lift based project](http://github.com/sonatype/m2eclipse-scala/tree/master/samples/prj-liftbased/) created from a shell created + imported project (used lift-archetype-basic 2.0-scala280-SNAPSHOT)

### Changes

#### 0.5.1

* remove use of Jobs to update classpath (fix #35 & #36 )

#### 0.5.0

* improve compatibility with scala version auto-detection of Scala-IDE 4.0

#### 0.4.3

* add license in feature (EPL)
* fix a bug in support of "add-source": "src/main/scala" and "src/test/scala" should be added when "add-source" is executed

#### 0.4.2

* add support for eclipse WTP contribution of [Fred Bricon](https://github.com/fbricon)
* upgrade tycho (0.15.0)
* remove automatic add of "src/main/scala" and "src/test/scala"

#### 0.4.1

* upgrade tycho (0.14.1), eclipse p2 repository (indigo)
* modify lifecycle: every goals are modified to "ignore" or to avoid run during incremental compilation 
* add JavaNature to project if it missing

#### 0.4.0

* remove scala-tools.org from catalags
* add support for [scala-maven-plugin]
* sort classpath and entries of the project

#### 0.3.1

* support of m2eclipse 1.0.0
* upate update-site to propose install of m2eclipse

#### 0.3.0

* support of m2eclipse 0.13.0

#### 0.2.3

* change urls of scala-tools.org catalags

#### 0.2.2

* rebuild to work with m2eclipse 0.12

#### 0.2.1

* fix critical bug about detection/activation of scala (plugin, nature) => 0.2.0 useless

#### 0.2.0

* should work with lift-archetype-basic 2.0-scala280-SNAPSHOT (new sample project added)
* support current and futur ids of scala-ide, select from the installed plugin (ch.epfl... and org.scala-ide....)

#### 0.1.0

* reintegration to sonatype repository

#### 0.0.5

* use assembla as "project host" to be under Scala-IDE umbrella
* re-do tycho configuration (to build on any box)
* rename directory
* generate update-site via tycho
* align groupId/artifactId to m2eclipse-extras

#### 0.0.4


* include a workaround to run JUnit test, but right-click on JUnit test doesn't propose 'Run as unit test'. Tu run as junit :
  * select source file and use keybord shortcut (Alt+Shit+X T)
    * source file have to be named with test class name and being in the 'right' directory (like in java)
    * I only test with annotation (see source of samples projects listed on wiki)
  * create JUnit Launch configuration

#### 0.0.3

* try to fix a issue (NPE) if default folders doesn't exist

#### 0.0.2


* auto add "src/main/scala" and "src/test/scala" if you don't use goal "add-sources" (nor <sourceDirectory>)
* add samples project http://github.com/sonatype/m2eclipse-scala/tree/master/samples/
* put documentation at https://www.assembla.com/wiki/show/scala-ide/With_M2Eclipse

#### 0.0.1

* I fork the m2eclipse-scala project from sonatype and add the following feature
* remove of scala-library, scala-compiler, scala-dbc, scala-swing from "Maven Dependencies" Container (under eclipse only)
* re-order JRE Container and Scala Container (to avoid scala.ScalaObject NotFound and C° when use "Run As Scala Application")
* add Scala-tools.org catalog

   [scala-maven-plugin]: http://davidb.github.com/scala-maven-plugin/
   [maven-scala-plugin]: http://scala-tools.org/mvnsites/maven-scala-plugin/
   [maven-eclipse-plugin]: http://maven.apache.org/plugins/maven-eclipse-plugin
   [build-helper-maven-plugin]: http://mojo.codehaus.org/build-helper-maven-plugin/
   [m2eclipse]: http://m2eclipse.sonatype.org/
   [m2eclipse-scala]: https://www.assembla.com/wiki/edit/scala-ide/With_M2Eclipse
   [IAM]: http://www.eclipse.org/iam/
   [Q4E]: http://code.google.com/p/q4e/
   [ESMi]: http://code.google.com/p/esmi/
   [scala-ide for eclipse]: http://scala-ide.assembla.com/
