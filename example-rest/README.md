Copyright 2016 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


Example REST
============
This module provides an example implementation of a Gaffer REST Interface.

There are two options for running it:

Option 1 - Deployable war file

If you wish to deploy the war file to a container of your choice, then use this option.
When run in this format, the default schemas represent the Film/Viewings example and the store used is a MockAccumuloStore.

To build a war file and deploy it to a server of your choice, then run the following command from the parent directory:
mvn clean package -pl example-rest/

In order for the application to function, it needs a number of system properties to be set up on the server:

gaffer.rest-api.basePath=/example-rest/v1
gaffer.schemas=${SOME PATH}/dataSchema.json,${SOME PATH}/dataTypes.json,${SOME PATH}/storeSchema.json,${SOME PATH}/storeTypes.json
gaffer.storeProperties=${SOME PATH}/mockaccumulostore.properties

The above example assumes that your schemas are in separate files, but if they were in a combined file, then you would just provide the path to one file.
Either way, the relevant files need to exist in the specified path on the appropriate machine. Examples can be copied from src/main/resources/. These examples illustrate the schema
 for a Film/Viewing graph and use the MockAccumuloStore as the Gaffer Store Implementation.

These properties need to be configured on your server, or passed into the application as System Properties.
An example of this in Tomcat would be to add the lines above to the end of ${CATALINA_HOME}/conf/catalina.properties

Option 2 - Build using the standalone profile
It can be built and then run as a basic executable standalone war file, by running the following command form the parent directory:

mvn clean install jetty:run -pl example-rest/ -P standalone

This should launch an embedded jetty container, which can then be accessed via the following url:
http://localhost:8080/example-rest/

If you need to change the port or customise anything else jetty related, you can change the class example.rest.launcher.Main accordingly.

As a default, there are a number of system properties that are configured in the pom.xml file. The most important of these are the locations of the schema
files and the data store .properties file. As a default, these point to the same files that are to be found in src/main/resources. These can be changed and the project rebuilt using the previous maven command

<systemProperties>
      <systemProperty>
          <name>gaffer.rest-api.basePath</name>
          <value>/example-rest/v1</value>
      </systemProperty>
      <systemProperty>
          <name>gaffer.schemas</name>
          <!-- this needs to point to your Gaffer schema files-->
          <value>${project.build.outputDirectory}/dataSchema.json,${project.build.outputDirectory}/dataTypes.json,${project.build.outputDirectory}/storeSchema.json,${project.build.outputDirectory}/storeTypes.json</value>
      </systemProperty>
      <systemProperty>
          <name>gaffer.storeProperties</name>
           <!-- this needs to point your data store properties file-->
          <value>${project.build.outputDirectory}/mockaccumulostore.properties</value>
      </systemProperty>
  </systemProperties>

