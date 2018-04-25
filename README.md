# Setting up a CI/CD pipeline with Maven and Jenkins on Azure

## Setting up Jenkins

- Create a Jenkins Virtual Machine from the Azure Marketplace: https://azuremarketplace.microsoft.com/en-us/marketplace/apps/azure-oss.jenkins

- SSH and establish an SSH tunnel into the Jenkins VM to set it up, replacing the placeholders below with the apropriate values

  ```sh
  ssh -L 127.0.0.1:8080:localhost:8080 <username>@<jenkins vm name>.<region>.cloudapp.azure.com
  ```

- Install Docker on the Jenkins VM

  ```sh
  sudo apt-get install docker.io
  ```

- Grant the `jenkins` user access to run Docker by adding it to the `docker` group

  ```sh
  sudo usermod -aG docker jenkins
  ```


- Install kubectl on the Jenkins VM

    ```sh
    sudo su
    apt-get update && apt-get install -y apt-transport-https
    curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -
    cat <<EOF >/etc/apt/sources.list.d/kubernetes.list
    deb http://apt.kubernetes.io/ kubernetes-xenial main
    EOF
    apt-get update
    apt-get install -y kubectl
    ```

- Copy your local Kubernetes config file to the Jenkins VM at /var/lib/jenkins/config

    ```sh
    ssh -o "StrictHostKeyChecking no"  <username>@<jenkins vm name>.<region>.cloudapp.azure.com sudo chmod 777 /var/lib/jenkins
    yes | scp ~/.kube/config <username>@<jenkins vm name>.<region>.cloudapp.azure.com:/var/lib/jenkins/config
    ssh -o "StrictHostKeyChecking no" <username>@<jenkins vm name>.<region>.cloudapp.azure.com sudo chmod 777 /var/lib/jenkins/config
    ```

    for example

    ```sh
    ssh -o "StrictHostKeyChecking no" azureuser@jenkins-sb.westeurope.cloudapp.azure.com sudo chmod 777 /var/lib/jenkins
    yes | scp ~/.kube/config azureuser@jenkins-sb.westeurope.cloudapp.azure.com:/var/lib/jenkins/config
    ssh -o "StrictHostKeyChecking no" azureuser@jenkins-sb.westeurope.cloudapp.azure.com sudo chmod 777 /var/lib/jenkins/config
    ```

- Restart Docker

   ```sh
   sudo systemctl restart docker.service
   ```

- Restart Jenkins

   ```sh
   sudo systemctl restart jenkins.service
   ```

## Configure Jenkins

- While the SSH tunnel is running, you should be able to access Jenkins at http://localhost:8080

- Once you are in and you're logged in, click on **Manage Jenkins**, then click on **Global Tool Configuration**

### Configure Maven

- Add Maven tool and use auto installation from Apache

- Give it a name of `M3`

### Configure Azure Container Registry credentials

- Click on **Credentials > System**, then click on **Global Credentials**

- Click on **Add Credentials** and create a **Username and password** credential. Fill your Azure Container Registry username for the username, and the Azure Container Registry password as the password. Save it as `acr-credentials`

### Install the Pipeline Maven Integration plugin

- Click on **Manage Jenkins > Manage Plugins**, click on the **Available** tab and search for `Pipeline Maven Integration Plugin`. Install without restarting.

## Setup the pipeline

- Click on **New Item** and create a new **Pipeline**

- Under Pipeline, choose **Pipeline script from SCM**, and choose **Git** as the SCM.

- Configure the **Repository URL** to point to the Git repository

- Set the Jenkins script path to `services/java-vote-service-redis/Jenkinsfile`

## Checkout pom.xml and Jenkinsfile

In the `services/java-vote-service-redis` folder, have a look at the `pom.xml` file which tells Maven how to build the project and `Jenkinsfile` which is the pipeline that Jenkins will follow.

## Checkout the Kubernetes deployment files

In the `services/java-vote-service-redis/kubernetes` folder, have a look at the `yaml` files and note the placeholders starting and ending with `XX`. Those placeholders are replaced by the Jenkins pipeline.

Also note that this configuration was done for a Kubernetes 1.9.6 cluster. You may need to update the apiVersions accordingly, depending on your cluster version. Refer to http://kubernetes.io.

In the `services/java-vote-service-redis/kubernetes/deployment.yaml`, make note of the `imagePullSecret` and make sure that your Kubernetes cluster has such secret configured for your namespaces corresponding to your Azure Container Registry credentials. Follow the documentation here to set the secret up: https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/

You can create a secret as follows

```sh
kubectl create secret docker-registry acr-secret --docker-server=<your-registry-server> --docker-username=<acr-username> --docker-password=<your-pword> --docker-email=<your-email>
```

## Trigger the build

In Jenkins, trigger a build and observe the pipeline, when the build is done, you should find a new image pushed in your Azure Container Registry, tagged with the current build number.