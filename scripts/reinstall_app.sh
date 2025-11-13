#!/bin/bash

# Script para reinstalar completamente la app en el dispositivo/emulador
# Esto asegura que todos los cambios de código y recursos se vean reflejados

set -e

echo "🧹 Limpiando build anterior..."
./gradlew clean

echo "🔨 Compilando nueva versión..."
./gradlew :app:assemblePhoneDebug --no-daemon

echo "📱 Desinstalando versión anterior..."
adb uninstall com.comprartir.mobile || echo "App no estaba instalada previamente"

echo "📲 Instalando nueva versión..."
adb install -r app/build/outputs/apk/phone/debug/app-phone-debug.apk

echo "✅ App reinstalada exitosamente!"
echo ""
echo "🔍 Para ver los logs de debugging:"
echo "   adb logcat | grep -E 'ListsScreen|GetHomeListsUseCase|DefaultShoppingListsRepository'"
