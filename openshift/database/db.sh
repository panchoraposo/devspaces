oc new-app --name=orders --template=postgresql-ephemeral \
           --param=DATABASE_SERVICE_NAME=orders \
           --param=POSTGRESQL_USER=quarkus \
           --param=POSTGRESQL_PASSWORD=quarkus \
           --labels=app.kubernetes.io/part-of=orders,systemname=orders,tier=database,database=postgresql,app.kubernetes.io/runtime=postgresql