FROM eclipse-temurin:21.0.7_6-jre-noble
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y nginx
COPY application/target/food-planner-application-0.0.1-SNAPSHOT-exec.jar /app.jar
COPY frontend/build /var/www/html/
COPY docker/nginx.conf /etc/nginx/sites-enabled/nginx.conf
EXPOSE 80
COPY docker/entrypoint.sh entrypoint.sh
RUN chmod 777 entrypoint.sh
RUN echo "daemon off;" >> /etc/nginx/nginx.conf
RUN rm /etc/nginx/sites-enabled/default
CMD ./entrypoint.sh