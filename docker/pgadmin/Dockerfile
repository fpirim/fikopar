# Dev-only pgAdmin with the fikopar server pre-registered. pgpass is baked in
# because pgAdmin ignores it unless it is 0600, which a bind mount cannot guarantee.
FROM dpage/pgadmin4

COPY servers.json /pgadmin4/servers.json
COPY pgpass /pgpass

USER root
RUN chmod 600 /pgpass && chown pgadmin:root /pgpass
USER pgadmin
