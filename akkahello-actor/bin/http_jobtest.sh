cd "$(dirname $0)"

curl http://localhost:8080/jobs -X POST -d @http_job.json --header "Content-Type:application/json"
curl http://localhost:8080/jobs/2