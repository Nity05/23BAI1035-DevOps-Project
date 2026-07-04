pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven-3.9.16'
    }

    environment {
        IMAGE_NAME = "hr-portal"
        IMAGE_TAG = "latest"
        KUBECONFIG = "C:\\Users\\Nithish\\.kube\\config"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t %IMAGE_NAME%:%IMAGE_TAG% .'
            }
        }

        stage('Deploy') {
            steps {
                bat 'kubectl apply -f k8s\\deployment.yaml'
                bat 'kubectl apply -f k8s\\service.yaml'
            }
        }
    }

    post {
        always {
            echo 'Pipeline Finished.'
        }

        success {
            echo 'BUILD SUCCESSFUL - HR Portal Deployed Successfully'
        }

        failure {
            echo 'BUILD FAILED - Check Jenkins Console Output'
        }
    }
}