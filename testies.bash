#!/usr/bin/bash

: ${HOST=localhost}
: ${PORT=8080}
: ${PROD_ID_RECS_REVS=1}
: ${PROD_ID_NOT_FOUND=13}
: ${PROD_ID_NO_RECS=113}
: ${PROD_ID_NO_REVS=213}

function assertCurl() {
  local expectedCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode=${result:(-3)}
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedCode" ]; then
    if [ "$http_code" = "200" ]; then
      echo "Test OK (http code ${http_code})"
    else
      echo "Test OK (http code ${http_code} respose ${RESPONSE})"
    fi
  else
    echo "Test FAILED Expected http code $expectedCode, got $httpCode, will abort!"
    echo " - FAILING COMMAND $curlCmd"
    echo " - RESPONSE $RESPONSE"
    exit 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value $actual)"
  else
    echo "Test FAILED EXPECTED VALUE $expected, GOT $actual, WILL ABORT!"
    exit 1
  fi
}

function testUrl() {
  url=$@
  curl=$(eval $url -ks -f -w "%{http_code}" -o dev/null)
  statusCode=${curl:(-3)}
  if [[ $statusCode == 200 ]]; then
    return 0
  else
    return 1
  fi
}

function waitForService() {
  url=$@
  echo -n "Waiting for $url..."
  n=0
  until testUrl $url; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo "Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "Done, continues.."
}

function recreateComposite() {
  local productId=$1
  local body=$2

  assertCurl 200 "curl -X DELETE http://$HOST:$PORT/product-composite/${productId} -s"
  curl -X POST http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$body"
}

function seedData() {
  body="{\"productId\":$PROD_ID_RECS_REVS"
  body+=\
',"name":"product name","weight":1,"recommendations":[
      {"recommendationId":1,"author":"a","rate":1,"content":"c"},
      {"recommendationId":2,"author":"a","rate":2,"content":"c"},
      {"recommendationId":3,"author":"a","rate":3,"content":"c"}
      ], "reviews":[
      {"reviewId":1,"author":"a","subject":"s","content":"c"},
      {"reviewId":2,"author":"a","subject":"s","content":"c"},
      {"reviewId":3,"author":"a","subject":"s","content":"c"}]}'

  recreateComposite "$PROD_ID_RECS_REVS" "$body"

  body="{\"productId\":$PROD_ID_NO_RECS"
  body+=\
',"reviews":[
      {"reviewId":1,"author":"a","subject":"s","content":"c"},
      {"reviewId":2,"author":"a","subject":"s","content":"c"},
      {"reviewId":3,"author":"a","subject":"s","content":"c"}]}'

  recreateComposite "$PROD_ID_NO_RECS" "$body"

  body="{\"productId\":$PROD_ID_NO_REVS"
  body+=\
',"name":"product name","weight":1,"recommendations":[
      {"recommendationId":1,"author":"a","rate":1,"content":"c"},
      {"recommendationId":2,"author":"a","rate":2,"content":"c"},
      {"recommendationId":3,"author":"a","rate":3,"content":"c"}]}'

  recreateComposite "$PROD_ID_NO_REVS" "$body"
}

set -e

echo "Start tests:" $(date)

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]; then
  echo "Restarting test environment"
  echo "docker compose down --remove-orphans"
  docker compose down --remove-orphans
  echo "docker compose up"
  docker compose up -d
fi

waitForService curl -X DELETE http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND

seedData

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_RECS_REVS -s"
assertEqual $PROD_ID_RECS_REVS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s"
assertEqual "\"No product found $PROD_ID_NOT_FOUND\"" "$(echo $RESPONSE | jq .message)"

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_RECS -s"
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_REVS -s"
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s"
assertEqual "\"Invalid product Id: -1\"" "$(echo $RESPONSE | jq .message)"

assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalid-id -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

#Verify Swagger openAPI
echo "Swagger/OpenAPI tests"
assertCurl 302 "curl -s http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -sL http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs"
assertEqual "3.0.1" "$(echo $RESPONSE | jq -r .openapi)"
assertEqual "http://$HOST:$PORT" "$(echo $RESPONSE | jq -r '.servers[0].url')"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs.yaml"

if [[ $@ == *"stop"* ]]; then
  echo "We are done stoping our test environment"
  echo "docker compose down"
  docker compose down
fi

echo "End all Tests OK: " $(date)
