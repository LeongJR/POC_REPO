pipeline {

    agent any

    tools { maven "Maven 3.9.3" }

    stages {
        stage("SCM Checkout"){
            steps{
                echo "Checking Out SCM"
                echo "Repository Selected : ${Repository} "
                gitCloneCheckOut()
            }
        }

        stage("Project Build"){
            steps{
                echo "Commencing Build"
                mavenBuild()

            }
        }
       
        stage("Deploy"){
            steps{
            deploy adapters: [tomcat9(credentialsId: 'Tomcat', path: '', url: 'http://localhost:8090')], contextPath: "${Repository}", war: '**/*.war'
            }
        } 
        
        stage("Tests and Sonar scan"){
            steps{
                bat "mvn clean test"
                echo "Commencing Sonar Scan"
                sonarScan()
            }
        } 
        
    }

        post {
            always{
                cleanWs (cleanWhenFailure: false,
                        cleanWhenNotBuilt: false,
                        cleanWhenUnstable: false,
                        notFailBuild: true)
            }
        }
}


/* FUNCTIONS */
def gitCloneCheckOut() {

    checkout scmGit(branches: [[name: "${Branch}"]],
                    extensions: [],
                    userRemoteConfigs: [[credentialsId: '4e2b7a04-cf48-4c19-9039-31eb9693d8a7',
                                         url: "https://github.com/LeongJR/${Repository}"]])
}

def mavenBuild() {
    bat """
       echo 'Verify java version and compiler version'
       java --version 
       javac --version
       mvn -f pom.xml clean install -DskipTests=true
       """
}

def sonarScan() {
stage("Sonar Scan"){
    bat """
          mvn sonar:sonar \
         -Dsonar.projectKey="${Repository}" \
         -Dsonar.projectName="${Repository}" \
         -Dsonar.sources=src/main \
         -Dsonar.sourceEncoding=UTF-8 \
         -Dsonar.language=java \
         -Dsonar.test=src/test \
         -Dsonar.surefire.reportsPath=target/surefire-reports \
         -Dsonar.java.binaries=target/classes \
         """
      }
}
