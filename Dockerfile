FROM eclipse-temurin:25.0.3_9-jre-alpine-3.23
COPY --from=caddy:2.11.4-alpine /usr/bin/caddy /usr/bin/caddy
COPY application/target/food-planner-application-0.0.1-SNAPSHOT-exec.jar /app.jar
COPY frontend/build /var/www/html/
COPY docker/Caddyfile /etc/caddy/Caddyfile
EXPOSE 80
COPY docker/entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh
CMD ./entrypoint.sh
