def call() {

    pipeline {

        agent any

        stages {

            stage('Clone') {
                steps {
                    echo "Cloning Redis Ansible project..."
                    checkout scm
                }
            }

            stage('Load Configuration') {
                steps {
                    script {
                        echo "Loading configuration..."

                        def config = readProperties file: 'config/prod.conf'

                        env.SLACK_CHANNEL_NAME = config.SLACK_CHANNEL_NAME
                        env.ENVIRONMENT = config.ENVIRONMENT
                        env.CODE_BASE_PATH = config.CODE_BASE_PATH
                        env.ACTION_MESSAGE = config.ACTION_MESSAGE
                        env.KEEP_APPROVAL_STAGE = config.KEEP_APPROVAL_STAGE

                        echo "Environment: ${env.ENVIRONMENT}"
                        echo "Code Base Path: ${env.CODE_BASE_PATH}"
                        echo "Approval Stage: ${env.KEEP_APPROVAL_STAGE}"
                        echo "Slack Channel: ${env.SLACK_CHANNEL_NAME}"
                    }
                }
            }

            stage('User Approval') {
                when {
                    expression {
                        env.KEEP_APPROVAL_STAGE == 'true'
                    }
                }

                steps {
                    timeout(time: 10, unit: 'MINUTES') {
                        input(
                            message: "Approve Redis deployment to ${env.ENVIRONMENT}?",
                            ok: "Approve"
                        )
                    }
                }
            }

            stage('Playbook Execution') {
                steps {
                    echo "Executing Redis Ansible playbook..."

                    withCredentials([
                        sshUserPrivateKey(
                            credentialsId: 'redis-ec2-ssh',
                            keyFileVariable: 'SSH_KEY',
                            usernameVariable: 'SSH_USER'
                        )
                    ]) {
                        sh """
                            cd ${env.CODE_BASE_PATH}

                            ansible-playbook playbook.yml \
                              --private-key \$SSH_KEY \
                              -u \$SSH_USER
                        """
                    }
                }
            }
        }

        post {

            success {
                echo "Redis deployment successful"

                slackSend(
                    channel: env.SLACK_CHANNEL_NAME,
                    message: "${env.ACTION_MESSAGE} - SUCCESS"
                )
            }

            failure {
                echo "Redis deployment failed"

                slackSend(
                    channel: env.SLACK_CHANNEL_NAME,
                    message: "${env.ACTION_MESSAGE} - FAILED"
                )
            }
        }
    }
}