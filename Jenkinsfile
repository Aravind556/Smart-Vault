pipeline {
  agent any

  environment {
    BACKEND_DIR = 'backend'
    IMAGE_NAME = 'smartvault-backend'

    // For AWS:
    // AWS_DEFAULT_REGION = credentials('aws-region')
    // SMARTVAULT_KMS_KEY_ID = credentials('smartvault-kms-key-id')
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps {
        dir(env.BACKEND_DIR) {
          sh 'mvn -B test'
        }
      }
    }

    stage('Docker Build') {
      steps {
        dir(env.BACKEND_DIR) {
          sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
        }
      }
    }

    stage('Terraform (optional)') {
      when { expression { fileExists('infra/terraform/aws') } }
      steps {
        echo 'Run terraform init/plan/apply here (credentials required).'
      }
    }

    stage('Deploy to Kubernetes (optional)') {
      steps {
        echo 'Run kubectl apply -f infra/k8s here (kubeconfig required).'
      }
    }
  }
}
