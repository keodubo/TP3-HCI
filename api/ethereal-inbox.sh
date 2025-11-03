#!/bin/bash
# Script para acceder a la bandeja de Ethereal Email

echo "================================================"
echo "  Ethereal Email - Bandeja de Entrada"
echo "================================================"
echo ""
echo "Tus credenciales de Ethereal:"
echo "  Email:    berry.kuphal@ethereal.email"
echo "  Password: cwufHpuQweX2TbDm6C"
echo ""
echo "Para ver los emails de desarrollo:"
echo "  1. Abrí: https://ethereal.email/login"
echo "  2. Ingresá las credenciales de arriba"
echo "  3. Verás todos los emails enviados (verificación, reset password, etc.)"
echo ""
echo "O directamente abrí este enlace:"
echo "  https://ethereal.email/messages"
echo ""
echo "================================================"
echo ""

# Intentar abrir el navegador automáticamente
if command -v open &> /dev/null; then
    echo "¿Querés abrir Ethereal en el navegador ahora? (y/n)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        open "https://ethereal.email/login"
        echo "✅ Navegador abierto!"
    fi
fi

