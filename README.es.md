# Onesait Architecture Initializr

## Funcionalidad

Esta herramienta tiene como principal función proporcionar un arquetipo de inicialización al usuario con el propósito de acelerar la creación de un Microservicio con estructura y estándares Onesait y que incluye maven y Spring. La herramienta permite crear un nuevo proyecto en blanco, o bien añadir una serie de módulos y librerías de manera automatizada gracias a la selección de los mismos desde los cuadros seleccionables. Las nuevas utilidades serán añadidas al proyecto mediante la inclusión automática de dependencias en el pom.xml de la nueva aplicación y la inclusión de las clases de configuración de cada funcionalidad en el caso de que fuesen necesarias.

## Estructura del proyecto generado
A continuación se detallará la estructura generada por una ejecución de un ciclo de operaciones como el "init". Si observamos la ejecución vemos que bajo la ruta especificada se ha generado el proyecto con el nombre de `onesait-<artifactId>`. Bajo este directorio se generará la siguiente estructura:

Directorio `/sources`:
* `pom.xml`: dependencias Spring-boot, Spring-Security, repositorios de Onesait Platform, plugin package de maven, libreria de utilidades de onesait...
* `/docker`: Dockerfile básico para la aplicación.
* `/src/main/java/com/minsait/onesait/<artifactId>`:
    * `/configuration`: Añade las clases de configuración de OpenApi y Spring Security
    * `/controllers`:  contiene un controlador REST de ejemplo. En caso de que se especifique el fichero YAML de descripción de las APIs, se crearán las clases correspondientes. 
    * `/model`: contiene unos modelos de ejemplo. En caso de especificar el fichero YAML, se crearán las clases correspondientes.
    * `/service`: contiene los servicios y sus implementaciones de ejemplo.
    * Application.java

## Configuración
A la hora de usar este servicio hay que tener en cuenta las siguientes variables de configuración:

| Variable de entorno | Descripción |
| ------------ | ------------ |
| SECURITY_KEY | Clave de seguridad generada. Recomendable hexadecimal de 32 caracteres. Usada para acceder al servicio de forma segura |
| AUDIT_LOGGER_ENABLE | Booleano que indica si hay auditoría a través del logger |
| AUDIT_CONSOLE_ENABLE | Booleano que indica si hay auditoría a través de la consola |
| AUDIT_FILE_PATH | Ruta donde se guarda el fichero de auditoría |
| AUDIT_FILE_NAME | Nombre del fichero de auditoría |
| AUDIT_FILE_ENABLE | Booleano que indica si hay auditoría a través de un fichero |
| AUDIT_OP_ENABLE | Booleano que indica si hay auditoría de Onesait Platform |
| AUDIT_OP_ENDPOINT | URL de la Onesait Platform |
| AUDIT_CUSTOM_ELASTIC_ENABLED | Booleano que indica si hay auditoría a través de Elastic |
| AUDIT_CUSTOM_ELASTIC_HOST | Host de Elastic |
| AUDIT_CUSTOM_ELASTIC_BLACKLIST | Blacklist de Elastic |
| AUDIT_TRANSACTIONAL | Booleano que indica si hay auditoría de base de datos |
| AUDIT_OUTPUT | Booleano que indica si hay auditoría de la respuesta de los métodos |

Para hacer uso del servicio en local hay que configurar su equivalente en el `application.yml`, que ya tiene variables definidas para ello.