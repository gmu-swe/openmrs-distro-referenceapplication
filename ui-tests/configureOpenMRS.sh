#!/bin/bash
DB_CREATE_TABLES=${DB_CREATE_TABLES:-false}
DB_AUTO_UPDATE=${DB_AUTO_UPDATE:-false}
MODULE_WEB_ADMIN=${MODULE_WEB_ADMIN:-true}
DEBUG=${DEBUG:-false}

mkdir -p target/openmrs-data
cat > target/openmrs-server.properties << EOF
install_method=auto
connection.url=jdbc\:mysql\://${DB_HOST}\:${DB_PORT}/${DB_DATABASE}?autoReconnect\=true&sessionVariables\=default_storage_engine\=InnoDB&useUnicode\=true&characterEncoding\=UTF-8
connection.username=${DB_USERNAME}
connection.password=${DB_PASSWORD}
has_current_openmrs_database=true
create_database_user=false
module_web_admin=${MODULE_WEB_ADMIN}
create_tables=${DB_CREATE_TABLES}
auto_update_database=${DB_AUTO_UPDATE}
EOF

cp -r ../package/target/distro/web/modules target/openmrs-data/modules
cp -r ../package/target/distro/web/owa target/openmrs-data/owa
