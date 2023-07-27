pipeline {

    agent any

    tools { maven "Maven 3.9.3"}

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

        stage("Sonar Scan"){
            steps{
                echo "Commencing Sonar Scan - To be implemented"
                //sonarScan()
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
    bat '''
       echo 'Verify java version and compiler version'
       java --version
       javac --version
       mvn -f pom.xml clean install

       '''
}