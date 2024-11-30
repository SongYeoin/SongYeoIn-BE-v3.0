pipeline {
  agent any
   environment {
        BRANCH_NAME = env.BRANCH_NAME
        DEPLOY_BRANCH = "main"
        S3_BUCKET = "songyeoin-jenkins-ci-cd" // S3 버킷 이름
        APP_NAME = "songyeoin" // Elastic Beanstalk 애플리케이션 이름
        ENV_NAME = "songyeoin-env" // Elastic Beanstalk 환경 이름
      }
  stages {
    stage('build') {
      steps {
        sh './gradlew clean build'
      }
    }

    stage('upload') {
      steps {
        sh """
        aws s3 cp build/libs/*.jar s3://$S3_BUCKET/songyeoin-backend-${BUILD_TAG}.jar --region ap-northeast-2
        """
      }
    }

    stage('deploy') {
      when {
        branch DEPLOY_BRANCH
     }
      steps {
        echo "Deploying backend to EC2"
        sh """
        aws elasticbeanstalk create-application-version --region ap-northeast-2 --application-name $APP_NAME \
                  --version-label $BUILD_TAG \
                  --source-bundle S3Bucket="$S3_BUCKET",S3Key="songyeoin-backend-0.0.1-SNAPSHOT.jar"

                  aws elasticbeanstalk update-environment --region ap-northeast-2 --environment-name $ENV_NAME \
                  --version-label $BUILD_TAG
        """
      }
    }

  }
}