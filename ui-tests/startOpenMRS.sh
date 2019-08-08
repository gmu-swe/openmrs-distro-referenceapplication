#!/usr/bin/env bash
path="$(cd "$(dirname "$0")" && pwd)"
SOURCES=${path}/config-files/sources
SINKS=${path}/config-files/sinks
TAINT_THROUGHS=${path}/config-files/taintThrough
AUTO_TAINT="-Dphosphor.sources=${SOURCES} -Dphosphor.sinks=${SINKS} -Dphosphor.taintThrough=${TAINT_THROUGHS}"

INST_JAVA="${HOME}/.phosphor-jvm200/bin/java"
M2_REPO="${HOME}/.m2/repository"
PHOSPHOR_JAR="${M2_REPO}/edu/gmu/swe/phosphor/Phosphor/0.0.4-SNAPSHOT/Phosphor-0.0.4-SNAPSHOT.jar"
TAPESTRY_CORE_JAR="${M2_REPO}/edu/gmu/swe/tapestry/tapestry-core/0.0.4-SNAPSHOT/tapestry-core-0.0.4-SNAPSHOT.jar"
SERVER_JAR="${M2_REPO}/edu/gmu/swe/tapestry/embedded-server/0.0.4-SNAPSHOT/embedded-server-0.0.4-SNAPSHOT.jar"
PHOSPHOR_OPTS="taintSourceWrapper=edu.gmu.swe.tapestry.internal.PhosphorFuzzer,ignore=edu/gmu/swe/tapestry/internal/,arrayindex,priorClassVisitor=edu.gmu.swe.tapestry.internal.IfcCV,ignoredMethod=org/mindrot/jbcrypt/BCrypt.encipher([II)V,taintSources=${SOURCES},taintSinks=${SINKS},taintThrough=${TAINT_THROUGHS}"
JAVA_FLAGS="-Xmx8g -DphosphorCacheDirectory=cached-phosphor -Xbootclasspath/p:${PHOSPHOR_JAR}:${TAPESTRY_CORE_JAR} -javaagent:${PHOSPHOR_JAR}=${PHOSPHOR_OPTS}" 

#/Users/jon/Documents/GMU/Projects/junit-ifc/experiments/openmrs-distro-referenceapplication/ui-tests/target/mysql-dist/bin/mysqld --no-defaults --user=jon --general_log --console --innodb_buffer_pool_size=64M --innodb_log_file_size=64M --log_warnings --innodb_use_native_aio=0 --binlog-ignore-db=root --basedir=/Users/jon/Documents/GMU/Projects/junit-ifc/experiments/openmrs-distro-referenceapplication/ui-tests/target/mysql-dist --lc-messages-dir=/Users/jon/Documents/GMU/Projects/junit-ifc/experiments/openmrs-distro-referenceapplication/ui-tests/target/mysql-dist/share --datadir=/Users/jon/Documents/GMU/Projects/junit-ifc/experiments/openmrs-distro-referenceapplication/ui-tests/target/mysql-data/data --tmpdir=/Users/jon/Documents/GMU/Projects/junit-ifc/experiments/openmrs-distro-referenceapplication/ui-tests/target/mysql-data/temp --socket=/tmp/junit-ifc-mysql.sock --pid-file=/Users/jon/Documents/GMU/Projects/junit-ifc/experiments/openmrs-distro-referenceapplication/ui-tests/target/mysql-data/mysql.pid --port=52145

 USER_FLAGS="-Difc.criticalReproductionFiles -DOPENMRS_APPLICATION_DATA_DIRECTORY=${path}/openmrs-data -DOPENMRS_INSTALLATION_SCRIPT=${path}/openmrs-server.properties -Dmysql.port=52145"

CMD="${INST_JAVA} ${JAVA_FLAGS} -cp ${SERVER_JAR} ${USER_FLAGS} edu.gmu.swe.tapestry.internal.server.EmbeddedServer 8383 8382 ${path}/../package/target/distro/web/openmrs.war openmrs"
echo "$CMD"
$CMD
