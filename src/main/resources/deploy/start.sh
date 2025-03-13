#!/bin/bash
echo "©\©\©\©\Execute start.sh script start©\©\©\©\"
echo "deploy_home:$deploy_home"
echo "platform_name:$platform_name"
echo "project_name:$project_name"
#deploy_home=/home/vmuser/yundao
#platform_name=bug
#project_name=jscsc-service-component-diagnosis-yl

#Judge whether the variable is empty
if  [ ! -n "$deploy_home" ];then
  echo "1+IS NULL+$deploy_home"
else
  echo "2+NOT NULL+$deploy_home"
  echo "2+NOT NULL+$platform_name"
  echo "2+NOT NULL+$project_name"
  # ANS_WORK=$deploy_home/$project_name/BOOT-INF/classes/deploy/ansible/playbooks  ANSIBLE_CONFIG=${ANS_WORK}/ansible.cfg \
  ansible-playbook $deploy_home/$project_name/BOOT-INF/classes/deploy/ansible/playbooks/playbook_deploy.yml  -i $deploy_home/$project_name/BOOT-INF/classes/deploy/ansible/inventory/ --vault-id vault-jscsc@~/.ssh/jscsc_rsa  -e platform_name=$platform_name  -e project_name=$project_name    -e deploy_home=$deploy_home
fi


#Output ansible execution results
if [ $? -ne 0 ];
  then
    echo "start.sh fail"
  else
    echo  "start.sh succes"
fi

echo "©\©\©\©\Execute start.sh script end©\©\©\©\"