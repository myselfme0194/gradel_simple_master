#!groovy

node ('windows'){
  stage('checkout') {
    git 'https://github.com/harsharameshh/artifactory.git'
  }
  stage('build'){
     //  currentBuild.displayName = "1.10"
      sh  " ./gradlew clean assemble"
  }
       stage('SonarQube analysis') {
          withSonarQubeEnv() {
      	 //  sh " ./gradlew sonarqube -Dsonar.projectKey=harsha_aig -Dsonar.host.url=http://34.219.184.2:9000  -Dsonar.login=admin -Dsonar.password=admin"
          }
      	}
      	stage('run test'){
      sh  " ./gradlew build"
  }
      	stage('artifactory')
      	{
      	  
            sh "./gradlew artifactoryPublish  "  
             def server = Artifactory.server 'JFROG'
                 def uploadSpec = """{
                 "files": [{
                       "pattern": "path/",
                       "target": "path/"
                    }]
                 }"""
           

                 server.upload(uploadSpec) 
     
            
      	}
      }
