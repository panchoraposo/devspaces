oc create secret generic tempostack-demo-s3 \
  --from-literal=bucket="tempostack-demo-s3" \
  --from-literal=endpoint="https://s3.<AWS_REGION>.amazonaws.com" \
  --from-literal=access_key_id="<AWS_ACCESS_KEY>" \
  --from-literal=access_key_secret="<AWS_SECRET_ACCESS_KEY>"