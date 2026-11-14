// Pipeline CI/CD Cashflow: Build & Test -> Jacoco 100% -> Trivy Scan -> Package -> Publish -> Deploy.
// Sesuaikan nilai di blok environment dan ID credentials Jenkins dengan lingkungan Anda.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME         = 'ifs25025-p5-pbo2026-springboot-cashflow'
        REGISTRY_HOST    = 'registry.example.com'              // ganti: hostname container registry
        REGISTRY_NAMESPACE = 'ifs25025'                        // ganti: namespace/repository registry
        IMAGE            = "${REGISTRY_HOST}/${REGISTRY_NAMESPACE}/${APP_NAME}"
        IMAGE_TAG        = "${env.BUILD_NUMBER}"
        DEPLOY_HOST = 'deploy@server.example.com'              // ganti: user@host server tujuan
    }

    stages {
        stage('Build & Test') {
            steps {
                // Tes memakai profil test (H2), bukan PostgreSQL asli.
                sh 'chmod +x mvnw && ./mvnw -B clean verify -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // Aturan coverage 100% (view/auth dikecualikan) dicek di fase verify oleh jacoco:check.
                // Stage ini memastikan laporan tersedia dan menyimpannya sebagai artefak.
                sh 'test -f target/site/jacoco/index.html'
                archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: false
            }
        }

        stage('Trivy Scan') {
            steps {
                // Pindai dependensi dan konfigurasi; build gagal bila ada temuan HIGH/CRITICAL.
                sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL --no-progress .'
            }
        }

        stage('Package') {
            steps {
                // Profil production wajib agar frontend Vaadin ikut dibundel ke dalam JAR.
                sh './mvnw -B clean package -Pproduction -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Publish') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'registry-credentials',
                        usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASS')]) {
                    sh '''
                        docker build -t "$IMAGE:$IMAGE_TAG" -t "$IMAGE:latest" .
                        echo "$REGISTRY_PASS" | docker login "$REGISTRY_HOST" -u "$REGISTRY_USER" --password-stdin
                        docker push "$IMAGE:$IMAGE_TAG"
                        docker push "$IMAGE:latest"
                    '''
                }
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                sshagent(credentials: ['deploy-ssh-key']) {
                    // File .env produksi (DB_*, ROOT_*, UPLOAD_DIR, COOKIE_SECRET) disimpan di server tujuan.
                    sh '''
                        ssh -o StrictHostKeyChecking=no "$DEPLOY_HOST" "
                            docker pull $IMAGE:$IMAGE_TAG &&
                            (docker rm -f $APP_NAME || true) &&
                            docker run -d --name $APP_NAME --restart unless-stopped \
                                --env-file /opt/$APP_NAME/.env \
                                -v /opt/$APP_NAME/uploads:/app/uploads \
                                -p 8080:8080 $IMAGE:$IMAGE_TAG
                        "
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo 'Pipeline gagal: periksa log stage yang berwarna merah.'
        }
    }
}
