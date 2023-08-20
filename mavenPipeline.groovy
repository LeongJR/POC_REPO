pipeline {

    agent any

    tools { maven "Maven 3.9.3"
            jdk "${JAVA_VERSION}"
          }

    stages {
        stage("SCM Checkout"){
            steps{
                echo "Checking Out SCM"
                echo "Repository Selected : ${Repository} "
                gitCloneCheckOut()
            }
        }

        stage("Project Build and Unit Tests"){
            steps{
                echo "Commencing Build"
                mavenBuild()

            }
        }
        
        stage("Sonar scan"){
            steps{
                echo "Commencing Sonar Scan"
                sonarScan()
            }
        } 

        stage("Deploy"){
            steps{
            deploy adapters: [tomcat9(credentialsId: 'Tomcat', path: '', url: 'http://localhost:8090')], contextPath: "${Repository}", war: '**/*.war'
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
       mvn -f pom.xml clean install
       """
}

def sonarScan() {

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
