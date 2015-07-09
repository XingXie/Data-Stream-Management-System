#!/bin/bash

# Xing 10.14.2010: Add default user and pwd

ROOTDIR=`dirname $0`
PWD=`pwd`

cd ${ROOTDIR}
VIS_HOME=`pwd`

# Delete older copies of visualizer.ini
if [ -e visualizer.ini ]; then
	rm -f visualizer.ini
fi

# Generate visualizer.ini

# default host
echo "default_host = localhost" > visualizer.ini

# default port
echo "default_port = 9000" >> visualizer.ini

# default user
echo "default_user = USER" >> visualizer.ini

# default pwd
echo "default_pwd = PASSWORD" >> visualizer.ini

echo "img_path = /stream/vis/view/img/" >> visualizer.ini
echo "imgListFile = ImageList.properties" >> visualizer.ini
echo "script_path = ${VIS_HOME}/scripts/" >> visualizer.ini
echo "demo_path = ${VIS_HOME}/demo/" >> visualizer.ini


cd ${PWD}
