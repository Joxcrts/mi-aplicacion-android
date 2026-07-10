# Reglas de ProGuard/R8 para la versión release.
# Room genera las implementaciones de los DAO en tiempo de compilación,
# por lo que no se necesitan reglas adicionales para el modelo de datos.

# Conservar información de líneas para depurar trazas de errores en producción.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
