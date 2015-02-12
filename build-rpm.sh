#!/bin/bash
VERSION=$1
PROJECT_ROOT=`pwd`

if [ -z "$VERSION" ]; then
  echo "Usage: build.sh <version>";
  echo "Example: build.sh 0.1.0";
  exit 1
else
  echo "Version: ${VERSION}"
fi

if [ -e !"./sat5-telemetry.spec" ]; then
  echo "Script must be executed from sat5-telemetry project root."
  exit 1
fi

echo "Removing existing RPMS directory..."
rm -rf ${PROJECT_ROOT}/RPMS

echo "Making tmp directory..."
mkdir -p tmp/sat5-telemetry-${VERSION}

echo "Creating source tar..."
cd source
cp rh-telemetry-proxy-sat5.6.conf ../tmp/sat5-telemetry-${VERSION}
cp gui/dist/scripts/scripts.js ../tmp/sat5-telemetry-${VERSION}
cd ../tmp
tar -cf ~/rpmbuild/SOURCES/sat5-telemetry-${VERSION}.tar.gz sat5-telemetry-${VERSION}

echo "Copying spec file to ~/rpmbuild..."
cd $PROJECT_ROOT
cp sat5-telemetry.spec ~/rpmbuild/SPECS/sat5-telemetry.spec
rm -rf tmp

echo "Running rpmbuild..."
cd ~/rpmbuild/BUILD
rpmbuild --clean --rmsource --rmspec -ba ../SPECS/sat5-telemetry.spec

echo "Copying RPMs..."
cp -r ../RPMS ${PROJECT_ROOT}/RPMS
cp -r ../SRPMS ${PROJECT_ROOT}/SRPMS

#sign the rpm
