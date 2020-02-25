node('windows') {
	def buildVersion = "1.0.0.${env.BUILD_NUMBER}"
	def artifactoryPublishRepo = 'Gradel_master'

	dir('jenkins') {
		try {
      

			stage('Checkout') {
				git url: 'https://github.com/harsharameshh/gradel_simple_master.git'
			}

			stage('Build') {
				withEnv( "BUILD_VERSION=${buildVersion}"]) {
					sh "./gradlew clean assemble"
				}
			}

			stage('Run Tests') {
				withEnv( "BUILD_VERSION=${buildVersion}"]) {
					try {
						sh "./gradlew build"
					}
					finally {
						step([$class: 'JUnitResultArchiver', testResults: '**/test-results/**/*.xml'])
					}
				}
			}

			stage('Sonar scan') {
				withEnv( "BUILD_VERSION=${buildVersion}", "GRADLE_OPTS=-Dsonar.host.url=${env.SONAR_URL} -Dsonar.login=${env.SONAR_CRED_ID}"]) {
					sh "./gradlew sonarqube"
				}
			}

			stage('Publish to Artifactory') {
				withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'devopspipeline_artifactory_credential_id', usernameVariable: 'artifactoryUsername', passwordVariable: 'artifactoryPassword']]) {
					withEnv(["JAVA_HOME=${jdkHome}", "BUILD_VERSION=${buildVersion}", "GRADLE_OPTS=-Dartifactory.publish.repo.user=${env.artifactoryUsername} -Dartifactory.publish.repo=${artifactoryPublishRepo} -Dartifactory.publish.repo.pwd=${env.artifactoryPassword}"]) {
						sh "./gradlew artifactoryPublish"
					}
				}
			}

			stage('Notify') {
				notify 'Success', 'was successful', buildVersion, false
			}
		}
		catch(err) {
			notify 'Failure', 'failed', buildVersion, true
			throw err
		}
	}
}

def notify(status, bodyMessage, buildVersion, attachBuildLog) {
	emailext subject: "${env.JOB_NAME} $buildVersion Build $status", body: "The ${env.JOB_NAME} $buildVersion build $bodyMessage", to: 'Dhwajad.Kulkarni@aig.com', mimeType: 'text/html', attachLog: attachBuildLog
}
