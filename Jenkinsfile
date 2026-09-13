pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Code checked out from repository'
            }
        }

        stage('Build') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/namespace/namespace.yml'
                sh 'kubectl apply -f k8s/configmap/configmap.yml'
                sh 'kubectl apply -f k8s/services/'
                sh 'kubectl rollout restart deployment -n codereview'
            }
        }

        stage('Verify') {
            steps {
                sh 'kubectl get pods -n codereview'
            }
        }
    }
}