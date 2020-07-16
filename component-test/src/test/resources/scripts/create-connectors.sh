#!/bin/sh
echo "Waiting for Kafka Connect to start listening on kafka-connect  "
while :; do
    # Check if the connector endpoint is ready
    # If not check again
    curl_status=$(curl -s -o /dev/null -w %{http_code} http://kafka-connect:8083/connectors)
    echo -e $(date) "Kafka Connect listener HTTP state: " $curl_status " (waiting for 200)"
    if [ $curl_status -eq 200 ]; then
        break
    fi
    sleep 5
done

echo "======> Creating connectors"
# Send a simple POST request to create the connector
curl -X POST \
    -H "Content-Type: application/json" \
    --data '{
    "name": "sms-connector",
    "config": {
	          "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
	          "database.hostname": "postgres-db",
	          "database.port": "5432",
	          "database.user": "postgres",
	          "database.password": "postgres",
	          "database.dbname": "sms",
	          "database.server.name": "smsdb1",
	          "table.whitelist": "public.outboxevent",
	          "transforms": "outbox",
	          "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
	          "transforms.OutboxEventRouter.event.key": "aggregate_id",
	          "transforms.outbox.table.fields.additional.placement": "type:header:eventType"
        }
    }' http://kafka-connect:8083/connectors
