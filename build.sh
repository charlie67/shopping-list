echo $2 | docker login -u $1 --password-stdin
docker build --tag $1/money-dashboard:$3 -f Dockerfile .
docker push $1/money-dashboard:$3
