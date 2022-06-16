# Onesait Architecture Initializr

## Functionality

This tool's main function is to provide an initialization archetype to the user with the purpose of accelerating the creation of a Microservice with Onesait structure and standards included by Maven and Spring. The tool allows you to create a new blank project, or add a series of modules and libraries in an automated way thanks to their selection from the selectable boxes. The new utilities will be added to the project through the automatic inclusion of dependencies in the pom.xml of the new application and the inclusion of the configuration classes of each functionality in case they are necessary.

## Estructura del proyecto generado
Then, the structure generated by an execution of a cycle of operations such as "init" will be detailed. If we observe the execution we see that under the specified path the project with the name of `onesait-<artifactId>` has been generated. Under this directory the following structure will be generated:

Directory `/sources`:
* `pom.xml`: Spring-boot dependencies, Spring-Security, Onesait Platform repositories, maven plugin package, onesait utility library...
* `/docker`: Basic Dockerfile for the application.
* `/src/main/java/com/minsait/onesait/<artifactId>`:
    * `/configuration`: Add the OpenApi and Spring Security configuration classes
    * `/controllers`:  contains a sample REST controller. If the API description YAML file is specified, the corresponding classes will be created.
    * `/model`: contains some sample models. In case of specifying the YAML file, the corresponding classes will be created.
    * `/service`: contains the services and their example implementations.
    * Application.java

## Configuración
When using this service, the following configuration variables must be indicated:

| Environment variable | Description |
| ------------ | ------------ |
| SECURITY_KEY | Security key generated. Recommended hexadecimal of 32 characters. Used to access the service securely |
| AUDIT_LOGGER_ENABLE | Boolean that indicates if there is an audit through the logger |
| AUDIT_CONSOLE_ENABLE | Boolean that indicates if there is auditing through the console |
| AUDIT_FILE_PATH | Path where the audit file is saved |
| AUDIT_FILE_NAME | Audit file name |
| AUDIT_FILE_ENABLE | Boolean that indicates if there is an audit through a file |
| AUDIT_OP_ENABLE | Boolean that indicates if there is an audit of Onesait Platform |
| AUDIT_OP_ENDPOINT | Onesait Platform URL |
| AUDIT_CUSTOM_ELASTIC_ENABLED | Boolean indicating whether there is auditing through Elastic |
| AUDIT_CUSTOM_ELASTIC_HOST | Elastic host |
| AUDIT_CUSTOM_ELASTIC_BLACKLIST | Elastic blacklist |
| AUDIT_TRANSACTIONAL | Boolean indicating if there is database auditing |
| AUDIT_OUTPUT | Boolean that indicates if there is auditing of the response of the methods |

To make use of the service locally, its equivalent must be configured in the `application.yml`, which already has variables defined for it.