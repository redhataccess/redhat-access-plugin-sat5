#!/bin/bash
WAR_NAME=redhat_access.war
JS_NAME=insights.js
CSS_NAME=insights.css
SAT_HOSTNAME=$1

cd /home/chris/dev/projects/active/sat5-telemetry

echo "Copying javascript..."
scp source/gui/dist/scripts/$JS_NAME root@$SAT_HOSTNAME:/var/www/html/javascript/$JS_NAME
echo -e "Done.\n"

echo "Copying css..."
scp source/gui/dist/styles/$CSS_NAME root@$SAT_HOSTNAME:/var/www/html/css/$CSS_NAME
echo -e "Done.\n"

echo "Copying war..."
scp source/proxy/smartproxy/target/$WAR_NAME root@$SAT_HOSTNAME:/usr/share/tomcat6/webapps
echo -e "Done.\n"

echo "Restarting satellite..."
ssh root@$SAT_HOSTNAME -t rhn-satellite restart
echo -e "Done.\n"
