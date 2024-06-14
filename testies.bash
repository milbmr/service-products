#!/usr/bin/bash

:${HOST=localhost}
:${PORT=7000}
:${PROD_ID_RECS_REVS=1}
:${PROD_ID_NOT_FOUND=13}
:${PROD_ID_NO_RECS=113}
:${PROD_ID_NO_REVS=213}

function assertCurl() {
  local expectedCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode=${result:(-3)}
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedCode" ]
  then
    if [ "$http_code" = "200" ]
    then
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

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value $actual)"
  else
    echo "Test FAILED EXPECTED VALUE $expected, GOT $actual, WILL ABORT!"
    exit 1
  fi
}

set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_RECS_REVS -s"
assertEqual $PROD_ID_RECS_REVS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s"
assertEqual "\"Not found product Id: $PROD_ID_NOT_FOUND\"" "$(echo $RESPONSE | jq .message)"

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

echo "End all Tests OK: " $(date)
