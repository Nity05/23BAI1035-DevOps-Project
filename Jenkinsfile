pipeline {
    agent any

    tools {
        maven 'Maven-3.9.16'
    }

    environment {
        KUBECONFIG = 'C:\\Users\\Nithish\\.kube\\config'
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

        stage('Kubernetes Debug') {
            steps {
                bat 'echo USERPROFILE=%USERPROFILE%'
                bat 'echo KUBECONFIG=%KUBECONFIG%'
                bat 'kubectl config current-context'
                bat 'kubectl cluster-info'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat 'kubectl apply -f k8s\\deployment.yaml --validate=false'
                bat 'kubectl apply -f k8s\\service.yaml --validate=false'
                bat 'kubectl rollout restart deployment hr-portal'
            }
        }

        stage('Verify') {
            steps {
                bat 'kubectl get deployments'
                bat 'kubectl get pods'
                bat 'kubectl get svc'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}