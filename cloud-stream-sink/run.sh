export APP_SERVICE_NAME=cloud-stream-sink
export APP_JAR_FILE=target/${APP_SERVICE_NAME}-0.0.3-SNAPSHOT.jar

export OTEL_AGENT_VERSION=1.27.0
export JCSMP_OTEL_EXT_VERSION=1.2.0
export SOLACE_BINDER_OTEL_EXT_VERSION=5.8.0
export LIB_DIR=../3rdparty

mkdir -p ${LIB_DIR}

#Download the OpenTelemetry Java Agent, if it is not already downloaded
if [ ! -f ${LIB_DIR}/opentelemetry-javaagent.jar ]; then
  echo "Downloading OpenTelemetry Java Agent"
  curl -o ${LIB_DIR}/opentelemetry-javaagent.jar  https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/${OTEL_AGENT_VERSION}/opentelemetry-javaagent-${OTEL_AGENT_VERSION}.jar
fi

#Download the Solace JCSMP OpenTelemetry Extension, if it is not already downloaded
if [ ! -f ${LIB_DIR}/solace-opentelemetry-jcsmp-integration.jar ]; then
  echo "Downloading Solace JCSMP OpenTelemetry Extension"
  curl -o ${LIB_DIR}/solace-opentelemetry-jcsmp-integration.jar https://repo1.maven.org/maven2/com/solace/solace-opentelemetry-jcsmp-integration/${JCSMP_OTEL_EXT_VERSION}/solace-opentelemetry-jcsmp-integration-${JCSMP_OTEL_EXT_VERSION}.jar
fi

#Download the Solace Binder OpenTelemetry Extension, if it is not already downloaded
if [ ! -f ${LIB_DIR}/spring-cloud-stream-binder-solace-instrumentation.jar ]; then
  echo "Downloading Solace Binder OpenTelemetry Extension"
  curl -o ${LIB_DIR}/spring-cloud-stream-binder-solace-instrumentation.jar https://repo1.maven.org/maven2/com/solace/spring/cloud/spring-cloud-stream-binder-solace-instrumentation/${SOLACE_BINDER_OTEL_EXT_VERSION}/spring-cloud-stream-binder-solace-instrumentation-${SOLACE_BINDER_OTEL_EXT_VERSION}.jar
fi

REQUIRED_FILES=(
  "${LIB_DIR}/opentelemetry-javaagent.jar"
  "${LIB_DIR}/solace-opentelemetry-jcsmp-integration.jar"
  "${LIB_DIR}/spring-cloud-stream-binder-solace-instrumentation.jar"
  ${APP_JAR_FILE}
)

for file in "${REQUIRED_FILES[@]}"; do
  if [ ! -f "$file" ]; then
    echo "Required jar file not found: $file"
    exit 1
  fi
done

#export JAVA_HOME=~/.sdkman/candidates/java/17.0.6-tem
#export PATH=$JAVA_HOME/bin:$PATH

export OTEL_DEBUG=true

java -javaagent:${LIB_DIR}/opentelemetry-javaagent.jar \
  -Dotel.javaagent.extensions=${LIB_DIR}/solace-opentelemetry-jcsmp-integration.jar,${LIB_DIR}/spring-cloud-stream-binder-solace-instrumentation.jar \
  -Dotel.instrumentation.common.default-enabled=false \
  -Dotel.javaagent.debug=${OTEL_DEBUG} \
  -Dotel.traces.exporter=otlp \
  -Dotel.metrics.exporter=none \
  -Dotel.logs.exporter=none \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
  -Dotel.resource.attributes=service.name=${APP_SERVICE_NAME} \
  -Dotel.service.name=${APP_SERVICE_NAME} \
  -Dotel.propagators=solace_jcsmp_tracecontext \
  -Dotel.bsp.schedule.delay=100 \
  -Dotel.bsp.max.queue.size=2048 \
  -Dotel.bsp.max.export.batch.size=5 \
  -Dotel.bsp.export.timeout=10000 \
  -Dotel.java.disabled.resource.providers=io.opentelemetry.instrumentation.resources.ProcessResourceProvider \
  -Dotel.instrumentation.solace-opentelemetry-jcsmp-integration.enabled=true \
  -Dotel.instrumentation.spring-cloud-stream-binder-solace-instrumentation.enabled=true \
  -jar ${APP_JAR_FILE}

