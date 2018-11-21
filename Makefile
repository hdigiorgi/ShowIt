NAME=si
VERSION=0.0.2
BUILD_DIR=/tmp/si_build

.PHONY: prod-compile
prod-compile: ${NAME}_${VERSION}.tgz
	echo ${VERSION} > VERSION

${NAME}_${VERSION}.tgz:
	rm -rf ${BUILD_DIR}
	mkdir -p ${BUILD_DIR}
	cp -r * ${BUILD_DIR}
	cd ${BUILD_DIR} && \
	  sed -i '/<APP_VERSION>/c\version := \"'"${VERSION}"'\",' build.sbt && \
	  sed -i '/<APP_NAME>/c\name := \"'"${NAME}"'\",' build.sbt && \
	  mv server/conf/application.conf server/conf/application.dev.conf && \
	  mv server/conf/application.prod.conf server/conf/application.conf && \
	  sbt universal:packageZipTarball
	cp ${BUILD_DIR}/server/target/universal/${NAME}-${VERSION}.tgz ${NAME}_${VERSION}.tgz

