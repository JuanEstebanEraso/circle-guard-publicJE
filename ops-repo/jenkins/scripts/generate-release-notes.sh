#!/bin/bash

# Este script genera automáticamente unas Release Notes basadas en los commits
# entre el último tag y el commit actual. Ideal para Change Management.

PROJECT_NAME=$1
VERSION=$2

if [ -z "$PROJECT_NAME" ] || [ -z "$VERSION" ]; then
    echo "Uso: ./generate-release-notes.sh <NombreProyecto> <Version>"
    exit 1
fi

FILE_NAME="ReleaseNotes-${VERSION}.md"

# Intenta obtener el tag anterior. Si no hay, usa el primer commit.
PREVIOUS_TAG=$(git describe --tags --abbrev=0 2>/dev/null)
if [ -z "$PREVIOUS_TAG" ]; then
    PREVIOUS_TAG=$(git rev-list --max-parents=0 HEAD)
fi

echo "# 🚀 Release Notes: $PROJECT_NAME - v$VERSION" > $FILE_NAME
echo "Fecha de release: $(date +'%Y-%m-%d')" >> $FILE_NAME
echo "" >> $FILE_NAME

echo "## 📋 Resumen de Cambios" >> $FILE_NAME
# Agrupa los commits por tipo usando convención (feat:, fix:, chore:)
echo "### ✨ Nuevas Características (Features)" >> $FILE_NAME
git log ${PREVIOUS_TAG}..HEAD --grep="^feat" --pretty=format:"- %s (%h) por %an" >> $FILE_NAME
echo "" >> $FILE_NAME

echo "### 🐛 Correcciones de Errores (Fixes)" >> $FILE_NAME
git log ${PREVIOUS_TAG}..HEAD --grep="^fix" --pretty=format:"- %s (%h) por %an" >> $FILE_NAME
echo "" >> $FILE_NAME

echo "### 🔧 Tareas de Mantenimiento (Chores)" >> $FILE_NAME
git log ${PREVIOUS_TAG}..HEAD --grep="^chore" --pretty=format:"- %s (%h) por %an" >> $FILE_NAME
echo "" >> $FILE_NAME

echo "---" >> $FILE_NAME
echo "*Generado automáticamente por el pipeline de CI/CD de CircleGuard*" >> $FILE_NAME

echo "✅ Release Notes generadas en $FILE_NAME"
