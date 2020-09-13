cd "$(dirname $0)"

curl http://localhost:8080/jobs -X POST -d @http_request.json --header "Content-Type:application/json"
curl http://localhost:8080/jocdbs/2