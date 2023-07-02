mvn clean install

cd frontend
npm install
npm run build

echo $2 | docker login -u $1 --password-stdin
docker build --tag $1/shopping-list:${GITHUB_REF_NAME} -f Dockerfile ../
docker push $1/shopping-list:${GITHUB_REF_NAME}