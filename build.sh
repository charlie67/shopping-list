echo $2 | docker login -u $1 --password-stdin
docker build --tag $1/shopping-list:$3 -f Dockerfile .
docker push $1/shopping-list:$3
