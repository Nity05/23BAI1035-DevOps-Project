pipeline {
    agent any

    tools {
        maven 'Maven-3.9.16'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven Project') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t hr-portal:latest .'
            }
        }

        stage('Stop Existing Container') {
            steps {
                bat '''
                docker stop hr-portal 2>nul
                docker rm hr-portal 2>nul
                exit /b 0
                '''
            }
        }

        stage('Run Docker Container') {
            steps {
                bat 'docker run -d --name hr-portal -p 8080:8080 hr-portal:latest'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat 'kubectl apply -f k8s\\deployment.yaml'
                bat 'kubectl apply -f k8s\\service.yaml'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed.'
        }

        always {
            bat 'kubectl get pods'
            bat 'kubectl get svc'
        }
    }
}