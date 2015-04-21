#!/bin/bash
WAR_NAME=redhat_access.war
JS_NAME=scripts.js
SAT_HOSTNAME=sat57-demo.usersys.redhat.com

cd /home/chris/dev/projects/active/sat5-telemetry

echo "Copying javascript..."
scp source/gui/dist/scripts/$JS_NAME root@$SAT_HOSTNAME:/var/www/html/javascript/scripts.js
scp source/gui/dist/scripts/$JS_NAME.map root@$SAT_HOSTNAME:/var/www/html/javascript/scripts.js.map
echo -e "Done.\n"

echo "Copying war..."
scp source/proxy/smartproxy/target/$WAR_NAME root@$SAT_HOSTNAME:/usr/share/tomcat6/webapps
echo -e "Done.\n"

echo "Restarting satellite..."
ssh root@$SAT_HOSTNAME -t rhn-satellite restart
echo -e "Done.\n"
