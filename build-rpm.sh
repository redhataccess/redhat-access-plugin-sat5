#!/bin/bash
PROJECT_ROOT=`pwd`
PROJECT_NAME=redhat-access-plugin-sat5
SPEC_FILE_NAME=redhat-access-plugin-sat5.spec

#pull out the version form the specfile
VERSION=`./get_rpm_version.sh`
echo "Version: ${VERSION}"

if [ -e !"./$SPEC_FILE_NAME" ]; then
  echo "Script must be executed from sat5-telemetry project root."
  exit 1
fi

echo "Removing existing RPMS directory..."
rm -rf ${PROJECT_ROOT}/RPMS
echo "Done."

echo "Making tmp directory..."
RPM_WORK_DIR=.rpm-work/$PROJECT_NAME-$VERSION
mkdir -p $RPM_WORK_DIR
echo "Done."

echo "Building angular project..."
cd $PROJECT_ROOT/source/gui
npm install
bower install
grunt build
echo "Done."

echo "Building proxy server..."
cd $PROJECT_ROOT/source/proxy/smartproxy
mvn install
echo "Done."

echo "Creating source tar..."
cd $PROJECT_ROOT/source
cp rh-insights-sat5.conf ../$RPM_WORK_DIR
cp gui/dist/scripts/insights.js ../$RPM_WORK_DIR
cp gui/dist/styles/insights.css ../$RPM_WORK_DIR
cp proxy/smartproxy/target/redhat_access.war ../$RPM_WORK_DIR
cp -r jsp ../$RPM_WORK_DIR
cp -r resources ../$RPM_WORK_DIR
cd $PROJECT_ROOT/.rpm-work
tar -cf ~/rpmbuild/SOURCES/$PROJECT_NAME.tar.gz $PROJECT_NAME-$VERSION
echo "Done."

echo "Copying spec file to ~/rpmbuild..."
cd $PROJECT_ROOT
cp $SPEC_FILE_NAME ~/rpmbuild/SPECS/$SPEC_FILE_NAME
rm -rf .rpm-work
echo "Done."

echo "Running rpmbuild..."
cd ~/rpmbuild/BUILD
rpmbuild --clean --rmsource --rmspec -ba ../SPECS/$SPEC_FILE_NAME
echo "Done."

echo "Copying RPMs..."
cp -r ../RPMS ${PROJECT_ROOT}/.tmp/RPMS
cp -r ../SRPMS ${PROJECT_ROOT}/.tmp/SRPMS
echo "Done."

echo "Signing RPM..."
rpm --resign ${PROJECT_ROOT}/.tmp/RPMS/RPMS/noarch/redhat-access-plugin-sat5-$VERSION-1.el6.noarch.rpm
echo "Done."
