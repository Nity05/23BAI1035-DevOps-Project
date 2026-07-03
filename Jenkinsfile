pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
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

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t hr-portal:latest .'
            }
        }

        stage('Run Docker Container') {
            steps {
                bat 'docker stop hr-portal || exit 0'
                bat 'docker rm hr-portal || exit 0'
                bat 'docker run -d --name hr-portal -p 8080:8080 hr-portal:latest'
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
    }
}