docker container run \
  --name postgres-twoway \
  -p 5444:5432 \
  -d \
  --rm \
  -e POSTGRES_USER=twoway \
  -e POSTGRES_PASSWORD=twoway \
  -e POSTGRES_DB=twoway \
  postgres:14.3
