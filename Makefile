NAME=si
VERSION=0.0.1
BUILD_DIR=/tmp/si_build



.PHONY: prod-compile
prod-compile: ${NAME}_${VERSION}.deb
	cp ${NAME}_${VERSION}.deb ${NAME}.deb
	echo ${VERSION} > VERSION


${NAME}_${VERSION}.deb:
	rm -rf ${BUILD_DIR}
	mkdir -p ${BUILD_DIR}
	cp -r * ${BUILD_DIR}
	cd ${BUILD_DIR} && \
	  sed -i '/<APP_VERSION>/c\version := \"'"${VERSION}"'\",' build.sbt && \
	  sed -i '/<APP_NAME>/c\name := \"'"${NAME}"'\",' build.sbt && \
	  sbt debian:packageBin
	cp ${BUILD_DIR}/server/target/${NAME}_${VERSION}_all.deb ${NAME}_${VERSION}.deb

