pipeline {
    agent any
    
    parameters {
        string(name: 'repoPath', defaultValue: '', description: 'Path to the cloned repository')
        string(name: 'chartPath', defaultValue: '', description: 'Path to the chart folder')
    }

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    echo "Клонирую репозиторй из ${params.repoPath}"
                    if (params.repoPath.empty) {
                            error "Не указан путь до репозитория с чартами. Укажите параметр repoPath."
                        }
                    checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: "${params.repoPath}"]], branches: [[name: '*/main']]]
                }
            }
        }

        stage('Restore Chart Dependencies') {
            steps {
                script {
                    println ("Восстановление зависимостей")
                    def pomFile = new File('pom.xml')
                    def pomXml = new XmlSlurper().parse(pomFile)
                    def newDependency = """
    <dependency>
        <groupId>ru.sbrf.example.shared_library</groupId>
        <artifactId>lib</artifactId>
        <version>1.0</version>
    </dependency>
"""
                    pomXml.dependencies.appendNode(newDependency)
                    def updatedXml = groovy.xml.XmlUtil.serialize(pomXml)
                    pomFile.text = updatedXml
                    println("Зависимость успешно добавлена в файл pom.xml.")
                }
            }
        }

        stage('Archive Charts Folder') {
            steps {
                script {
                    echo "Архивация папки с подготовленными чартами"
                    if (params.chartPath.empty) {
                        error "Не указан путь до папки с чартами. Укажите параметр chartPath."
                    }
                    archiveArtifacts artifacts: "${params.chartPath}/*", allowEmptyArchive: true
                }
            }
        }
    }
}