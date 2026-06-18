pipeline {
  agent any

  environment {
    IMAGE_NAME = 'rohithvarma73/couriertrackingsystem'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker build -t $IMAGE_NAME:latest .'
      }
    }

    stage('Docker Login') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials-id', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
        }
      }
    }

    stage('Push Image') {
      steps {
        sh 'docker push $IMAGE_NAME:latest'
      }
    }

    stage('Deploy to Minikube') {
      steps {
        sh 'kubectl apply -f deploy/mysql.yaml'
        sh 'kubectl apply -f deploy/deployment.yaml'
        sh 'kubectl apply -f deploy/service.yaml'
      }
    }
  }
}