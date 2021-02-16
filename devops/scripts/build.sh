#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
. "${SCRIPT_DIR}/_initialize_package_variables.sh"
. "${SCRIPT_DIR}/_utils.sh"

build_kafka_connect_jdbc() {
  mvn ${KAFKA_MAVEN_ARGS}
  tgz_name="./target/kafka-connect-jdbc-*-package.tar.gz"
  mkdir -p "${BUILD_ROOT}/build"
  tar xvf ${tgz_name} -C "${BUILD_ROOT}/build"
}

main() {
  echo "Cleaning '${BUILD_ROOT}' dir..."
  rm -rf "$BUILD_ROOT"

  echo "Building project..."
  build_kafka_connect_jdbc

  echo "Preparing directory structure..."
  setup_role "mapr-kafka-connect-jdbc"

  setup_package "mapr-kafka-connect-jdbc"

  echo "Building packages..."
  build_package "mapr-kafka-connect-jdbc"

  echo "Resulting packages:"
  find "$DIST_DIR" -exec readlink -f {} \;
}

main
