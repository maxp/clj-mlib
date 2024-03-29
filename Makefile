# # #

GROUP_ID 	= maxp
ARTEFACT  = mlib
MAIN      = example__app.app.main

PROJECT_DESCR = mlib commons
PROJECT_URL = https://github.com/maxp/clj-mlib

# # #

.EXPORT_ALL_VARIABLES:
.PHONY: clean minor bump shapshot release uberjar deploy

SHELL = bash
VERSION = $(shell cat VERSION)

ifndef TIMESTAMP
  TIMESTAMP = $(shell date -Isec)
endif

ifndef COMMIT
  COMMIT = $(shell git rev-parse HEAD)
endif
	
#
RESOURCES = ./resources
TARGET    = ./target
# CLASSES   = ${TARGET}/classes
JAR_FILE  = ${TARGET}/${ARTEFACT}-${VERSION}.jar
UBER_JAR  = ${TARGET}/${ARTEFACT}.jar
BUILD_EDN = ${RESOURCES}/build.edn
APPNAME   = ${ARTEFACT}
#
JAVA_SRC  = ./src
#
REPO_ID   		= clojars
RELEASES_URL  = https://clojars.org/repo/
# SNAPSHOTS_URL = https://mvn.host.dom/repository/maven-snapshots/

build_edn: VERSION
	@echo "{">${BUILD_EDN}
	@echo ":appname \"${APPNAME}\"">>${BUILD_EDN}
	@echo ":version \"${VERSION}\"">>${BUILD_EDN}
	@echo ":commit \"${COMMIT}\"">>${BUILD_EDN}
	@echo ":timestamp \"${TIMESTAMP}\"">>${BUILD_EDN}
	@echo "}">>${BUILD_EDN}

pom: VERSION deps.edn
	@cat tools/pom-template.xml | envsubst > pom.xml
	@clojure -Spom

config: pom build_edn

javac:
	javac ${JAVA_SRC}/mlib_org/mindrot/jbcrypt/BCrypt.java

# compile:
# 	@mkdir -p ${CLASSES}
# 	clojure -e "(set! *compile-path* \"${CLASSES}\") (compile '${MAIN})"

jar: config
	@echo "build jar:" ${JAR_FILE}
	@clojure -A:depstar -m hf.depstar.jar ${JAR_FILE}

uberjar: clean config
	@echo "Build uberjar:" ${UBER_JAR}
	@clojure -A:depstar:uberjar -m hf.depstar.uberjar ${UBER_JAR} --main ${MAIN} --compile

# uberdeps: clean pom config compile css
#       clojure -A:uberdeps -m uberdeps.uberjar --target ${UBER_JAR} --main-class ${MAIN} --level info

# uberjar: clean pom config compile
# 	clojure -A:uberdeps --target ${UBER_JAR} --main-class ${MAIN} --level info \
# 	| grep -v com.sun.mail/javax.mail \
# 	| grep -v services/com.fasterxml.jackson.core.JsonFactory
# #

# # # NOTE: release file JAR or UBERJAR

snapshot: export VERSION := ${VERSION}-SNAPSHOT
snapshot: uberjar
	@mvn deploy:deploy-file 		\
		-DpomFile=pom.xml					\
		-Dfile=${UBER_JAR} 				\
		-DrepositoryId=${REPO_ID}	\
		-Durl=${SNAPSHOTS_URL}

# release: uberjar
# 	@mvn deploy:deploy-file 		\
# 		-DpomFile=pom.xml					\
# 		-Dfile=${UBER_JAR} 				\
# 		-DrepositoryId=${REPO_ID}	\
# 		-Durl=${RELEASES_URL}

deploy: bump jar
	@mvn deploy:deploy-file 		\
		-DpomFile=pom.xml					\
		-Dfile=${JAR_FILE} 				\
		-DrepositoryId=${REPO_ID}	\
		-Durl=${RELEASES_URL}

# # # VERSION:

# major.minor.build

# inc-major:
# 	@(VERS=`awk -F'.' '{print $$1+1 "." 0 "." 0}' VERSION` && echo $${VERS} > VERSION)
# 	@echo -n "New version: " && cat VERSION

minor:
	@(VERS=`awk -F'.' '{print $$1 "." $$2+1 "." $$3+1}' VERSION` && echo $${VERS} > VERSION)
	@echo -n "New version: " && cat VERSION

bump:
	@(VERS=`awk -F'.' '{print $$1 "." $$2 "." $$3+1}' VERSION` && echo $${VERS} > VERSION)
	@echo -n "New version: " && cat VERSION	

clean:
	rm -rf ${TARGET}

# # #

#.
