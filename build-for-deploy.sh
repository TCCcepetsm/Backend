#!/bin/bash

echo "=== Build do Backend para Deploy ==="
echo "Iniciando build da aplicação Spring Boot..."

# Limpar e compilar o projeto
echo "1. Limpando projeto anterior..."
mvn clean

echo "2. Compilando e empacotando aplicação..."
mvn package -DskipTests

echo "3. Verificando se o JAR foi criado..."
if [ -f "target/*.jar" ]; then
    echo "✅ Build concluído com sucesso!"
    echo "📦 Arquivo JAR criado em: target/"
    ls -la target/*.jar
    echo ""
    echo "🚀 Para fazer deploy:"
    echo "   1. Use o Dockerfile na raiz do projeto"
    echo "   2. A aplicação usará automaticamente o perfil de produção"
    echo "   3. Configure a variável PORT se necessário (padrão: 8080)"
    echo ""
    echo "🔗 Configurações do banco:"
    echo "   - URL: jdbc:postgresql://db.dfrgseyqzocsqyuspiwn.supabase.co:5432/postgres"
    echo "   - Usuário: postgres"
    echo "   - SSL: habilitado"
else
    echo "❌ Erro no build. Verifique os logs acima."
    exit 1
fi

