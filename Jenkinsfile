pipeline {
  agent any

  options {
    skipDefaultCheckout(true)
  }

  tools {
    jdk 'Java17'
    maven 'Maven'
  }

  environment {
    IMAGE_NAME = 'couriertrackingsystem:1.0'
  }

  stages {
    stage('Clean Workspace') {
      steps {
        deleteDir()
      }
    }

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build JAR') {
      steps {
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker build -t $IMAGE_NAME .'
      }
    }

    stage('Deploy to Minikube') {
      steps {
        sh 'kubectl apply -f deploy/mysql.yaml'
        sh 'kubectl apply -f deploy/deployment.yaml'
        sh 'kubectl apply -f deploy/service.yaml'
        sh 'kubectl rollout status deployment/couriertrackingsystem --timeout=180s'
      }
    }
  }

  post {
    always {
      echo 'Pipeline finished'
    }
    failure {
      echo 'Pipeline failed'
    }
  }
}