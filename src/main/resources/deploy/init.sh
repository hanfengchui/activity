#!/bin/bash
echo "Execute init script"

ansible --version

#Output ansible execution results
if [ $? -ne 0 ];
  then
    echo "init.sh fail"
  else
    echo  "init.sh ssucces"
fi
