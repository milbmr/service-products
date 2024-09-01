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
      echo "Test OK (http code $http_code)"
    else
      echo "Test OK (http code "$http_code", respose $RESPONSE)"
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

  echo "$actual"
  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value $actual)"
  else
    echo "Test FAILED EXPECTED VALUE $expected, GOT '$actual', WILL ABORT!"
    exit 1
  fi
}

function tryAgain() {
  url="$1 --data '$2' -w \"%{http_code}\""
  echo $url
  response=$(eval "$url")
  http_code=${response:(-3)}

  n=0
  until [[ "$http_code" == "202" ]]; do
    n=$((n + 1))
    if [[ $n == 10 ]]; then
      echo "Fail url, got $response"
      exit 1
    else
      sleep 2
      echo "retry, #$n"
    fi
  done

  return $http_code
}

function testUrl() {
  url=$@
  actuatorStatus=$(eval $url | jq -r .status)
  if [[ "$actuatorStatus" == "UP" ]]; then
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

function testCompositeCreated() {
  if ! assertCurl 200 "curl http://$HOST:$PORT/product-composite/${PROD_ID_RECS_REVS} -s"; then
    echo -n "FAIL"
    return 1
  fi

  set +e

  assertEqual "$PROD_ID_RECS_REVS" $(echo $RESPONSE | jq .productId)
  if [[ "$?" == "1" ]]; then return 1; fi

  assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
  if [[ "$?" == "1" ]]; then return 1; fi

  assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
  if [[ "$?" == "1" ]]; then return 1; fi

  set -e
}

function waitForMessageProcessing() {
  echo "Waiting for messages to process"

  sleep 1

  n=0
  until testCompositeCreated; do

    n=$((n + 1))
    if [[ $n == 40 ]]; then
      echo "Test Fail messages failed to processes"
      exit 1
    else
      echo -n " ,retry #$n"
    fi
  done

  echo "All tests are processed correctly"
}

function recreateComposite() {
  local productId=$1
  local body=$2

  assertCurl 202 "curl -X DELETE http://$HOST:$PORT/product-composite/${productId} -s"
  assertEqual 202 $(curl -X POST http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$body" -w "%{http_code}")
}

function seedData() {
  body="{\"productId\":$PROD_ID_NO_RECS"
  body+=',"name":"product name A","weight":100, "reviews":[
  {"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
  {"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
  {"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
]}'

  recreateComposite "$PROD_ID_NO_RECS" "$body"

  body="{\"productId\":$PROD_ID_NO_REVS"
  body+=',"name":"product name","weight":1, "recommendations":[
      {"recommendationId":1,"author":"a","rate":1,"content":"c"},
      {"recommendationId":2,"author":"a","rate":2,"content":"c"},
      {"recommendationId":3,"author":"a","rate":3,"content":"c"}]}'

  recreateComposite "$PROD_ID_NO_REVS" "$body"

  body="{\"productId\":$PROD_ID_RECS_REVS"
  body+=',"name":"product name","weight":1, "recommendations":[
      {"recommendationId":1,"author":"a","rate":1,"content":"c"},
      {"recommendationId":2,"author":"a","rate":2,"content":"c"},
      {"recommendationId":3,"author":"a","rate":3,"content":"c"}
      ], "reviews":[
      {"reviewId":1,"author":"a","subject":"s","content":"c"},
      {"reviewId":2,"author":"a","subject":"s","content":"c"},
      {"reviewId":3,"author":"a","subject":"s","content":"c"}]}'

  recreateComposite "$PROD_ID_RECS_REVS" "$body"
}

# function automate() {
#   echo "going to seed data"
#   set +e
#
#   n=0
#   until [[ "$n" == "10" ]]; do
#     echo -n "Seed #$n, retry"
#     seedData
#     n=$((n + 1))
#   done
#
#   set -e
#
#   echo "data seeded"
# }

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

waitForService curl -s http://$HOST:$PORT/actuator/health
seedData

waitForMessageProcessing

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
